package timeboard.core.internal.async;

/*-
 * #%L
 * core
 * %%
 * Copyright (C) 2019 - 2020 Timeboard
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */

import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import timeboard.core.api.ProjectService;
import timeboard.core.api.UserService;
import timeboard.core.api.exceptions.BusinessException;
import timeboard.core.api.sync.ProjectSyncCredentialField;
import timeboard.core.api.sync.ProjectSyncPlugin;
import timeboard.core.api.sync.ProjectSyncService;
import timeboard.core.api.sync.RemoteTask;
import timeboard.core.model.*;
import timeboard.core.security.TimeboardAuthentication;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Component
@PersistJobDataAfterExecution
@DisallowConcurrentExecution
public final class ProjectSyncJob implements Job {

    @Autowired
    private ProjectService projectService;

    @Autowired
    private List<ProjectSyncPlugin> projectImportServiceList;

    @Autowired
    private UserService userService;


    @Override
    public void execute(final JobExecutionContext context) throws JobExecutionException {
        try {

            final long orgID = context.getMergedJobDataMap().getLong(ProjectSyncService.ORG_ID);
            final long accountID = context.getMergedJobDataMap().getLong(ProjectSyncService.ACCOUNT_ID);
            final long projectID = context.getMergedJobDataMap().getLong(ProjectSyncService.PROJECT_ID);
            final String serviceName = context.getMergedJobDataMap().getString(ProjectSyncService.SERVICE_NAME);
            final List<ProjectSyncCredentialField> credentials =
                    (List<ProjectSyncCredentialField>) context.getMergedJobDataMap().get(ProjectSyncService.CREDENTIALS);

            final ProjectSyncPlugin syncService = this.projectImportServiceList.stream()
                    .filter(projectSyncPlugin -> projectSyncPlugin.getServiceName().equals(serviceName))
                    .findFirst().get();

            final Account actor = this.userService.findUserByID(accountID);

            SecurityContextHolder.getContext().setAuthentication(new TimeboardAuthentication(actor));

            final Project project = this.projectService.getProjectByID(actor, orgID, projectID);

            context.setResult(this.syncProjectTasks(orgID, actor, project, syncService, credentials));

        } catch (final Exception e) {
            context.setResult(e);
        }
    }

    private Object syncProjectTasks(
            final Long orgID,
            final Account actor,
            final Project project,
            final ProjectSyncPlugin syncService,
            final List<ProjectSyncCredentialField> jiraCrendentials) throws Exception {


        final List<RemoteTask> remoteTasks = syncService.getRemoteTasks(actor, jiraCrendentials);

        remoteTasks.forEach(task -> {
            try {
                mergeAssignee(userService, syncService.getServiceID(), task);
            } catch (final Exception e) {
                System.out.println(e);
            }
        });

        this.syncTasks(orgID, actor, project, remoteTasks);


        return String.format("Sync %s tasks from %s", remoteTasks.size(), syncService.getServiceName());
    }

    private void syncTasks(
            final Long orgID,
            final Account actor,
            final Project project,
            final List<RemoteTask> remoteTasks) throws BusinessException {

        final List<RemoteTask> newTasks = new ArrayList<>();
        for (final RemoteTask task1 : remoteTasks) {
            if (isNewTask(actor, task1)) {
                newTasks.add(task1);
            }
        }

        final List<RemoteTask> updatedTasks = new ArrayList<>();
        for (final RemoteTask task1 : remoteTasks) {
            if (isUpdated(actor, task1)) {
                updatedTasks.add(task1);
            }
        }


        this.createTasks(orgID, actor, project, newTasks);

        for (final RemoteTask remoteTask : updatedTasks) {
            final Optional<Task> taskToUpdate = projectService.getTaskByRemoteID(actor, remoteTask.getId());
            if (taskToUpdate.isPresent()) {
                taskToUpdate.get().setName(remoteTask.getTitle());
                projectService.updateTask(orgID,actor, taskToUpdate.get());
            }
        }
    }


    private void createTasks(
            final Long orgID, final Account actor, final Project project, final List<RemoteTask> newTasks) {
        newTasks.forEach(task -> {
                    String taskName = task.getTitle();
                    if (taskName.length() >= 100) {
                        taskName = taskName.substring(0, 99);
                    }
                    final String taskComment = task.getComments();
                    final Date startDate = task.getStartDate();
                    final Date endDate = task.getStopDate();
                    final double originaEstimate = 0;
                    final Long taskTypeID = null;
                    final Account assignedAccountID = this.userService.findUserByID(task.getLocalUserID());
                    final String origin = task.getOrigin();
                    final String remotePath = null;
                    final String remoteId = task.getId();
                    final Batch batch = null;
                    projectService.createTask(orgID, actor, project, taskName, taskComment,
                            startDate, endDate, originaEstimate, taskTypeID, assignedAccountID, origin,
                            remotePath, String.valueOf(remoteId), TaskStatus.IN_PROGRESS, batch);
                }
        );
    }


    private boolean isUpdated(final Account actor, final RemoteTask task) throws BusinessException {
        return !this.isNewTask(actor, task);
    }

    private boolean isNewTask(final Account actor, final RemoteTask task) throws BusinessException {
        final Optional<Task> existingTask = this.projectService.getTaskByRemoteID(actor, task.getId());
        return !existingTask.isPresent();
    }

    private void mergeAssignee(final UserService userService, final String externalID, final RemoteTask task) {
        final Account remoteAccount = userService.findUserByExternalID(externalID, task.getUserName());
        if (remoteAccount != null) {
            task.setLocalUserID(remoteAccount.getId());
        }
    }
}
