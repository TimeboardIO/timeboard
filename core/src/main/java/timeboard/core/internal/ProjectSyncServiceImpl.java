package timeboard.core.internal;

/*-
 * #%L
 * core
 * %%
 * Copyright (C) 2019 Timeboard
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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.concurrent.DelegatingSecurityContextCallable;
import org.springframework.stereotype.Component;
import timeboard.core.api.*;
import timeboard.core.api.exceptions.BusinessException;
import timeboard.core.async.AsyncJobService;
import timeboard.core.model.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Component
public class ProjectSyncServiceImpl implements ProjectSyncService {

    @Autowired
    private AsyncJobService asyncJobService;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private UserService userService;

    @Autowired
    @Qualifier("JiraImportPlugin")
    private ProjectImportService projectImportService;

    @Override
    public void syncWithJIRA(Account org, Account actor, Project project, JIRACrendentials jiraCrendentials) {

        this.asyncJobService.runAsyncJob(org, actor, String.format("Sync project %s with JIRA", project.getName()), new DelegatingSecurityContextCallable(() -> {

            ThreadLocalStorage.setCurrentOrganizationID(org.getId());

            project.getAttributes().put("jira.username", new ProjectAttributValue(jiraCrendentials.getUsername()));
            project.getAttributes().put("jira.password", new ProjectAttributValue(jiraCrendentials.getPassword()));
            project.getAttributes().put("jira.project", new ProjectAttributValue(jiraCrendentials.getProject()));

            final List<ProjectImportService.RemoteTask> remoteTasks =
                    this.projectImportService.getRemoteTasks(actor, project);

            remoteTasks.stream()
                    .forEach(task -> mergeAssignee(userService, this.projectImportService.getServiceName(), task));

            final List<ProjectImportService.RemoteTask> newTasks = new ArrayList<>();
            for (ProjectImportService.RemoteTask task1 : remoteTasks) {
                if (isNewTask(actor, project.getId(), task1)) {
                    newTasks.add(task1);
                }
            }

            final List<ProjectImportService.RemoteTask> updatedTasks = new ArrayList<>();
            for (ProjectImportService.RemoteTask task1 : remoteTasks) {
                if (isUpdated(actor, project.getId(), task1)) {
                    updatedTasks.add(task1);
                }
            }


            this.createTasks(actor, project, newTasks);

            for (ProjectImportService.RemoteTask remoteTask : updatedTasks) {
                Task taskToUpdate = (Task) projectService.getTaskByID(actor, remoteTask.getId());
                taskToUpdate.setName(remoteTask.getTitle());
                projectService.updateTask(actor, taskToUpdate);
            }


            return String.format("Sync %s tasks from Jira", remoteTasks.size());
        }));

    }


    private void createTasks(Account actor, Project project, List<ProjectImportService.RemoteTask> newTasks) {
        newTasks.forEach(task -> {
                    String taskName = task.getTitle();
                    if (taskName.length() >= 100) {
                        taskName = taskName.substring(0, 99);
                    }
                    String taskComment = task.getComments();
                    Date startDate = task.getStartDate();
                    Date endDate = task.getStopDate();
                    double originaEstimate = 0;
                    Long taskTypeID = null;
                    Account assignedAccountID = this.userService.findUserByID(task.getLocalUserID());
                    String origin = task.getOrigin();
                    String remotePath = null;
                    Long remoteId = task.getId();
                    Milestone milestone = null;
                    projectService.createTask(actor, project, taskName, taskComment,
                            startDate, endDate, originaEstimate, taskTypeID, assignedAccountID, origin,
                            remotePath, String.valueOf(remoteId), milestone);
                }
        );
    }



    private boolean isUpdated(Account actor, long projectID, ProjectImportService.RemoteTask task) throws BusinessException {
        return !this.isNewTask(actor, projectID, task);
    }

    private boolean isNewTask(Account actor, long projectID, ProjectImportService.RemoteTask task) throws BusinessException {
        AbstractTask existingTask = this.projectService.getTaskByID(actor, task.getId());
        return existingTask == null;
    }

    private void mergeAssignee(UserService userService, String externalID, ProjectImportService.RemoteTask task) {
        final Account remoteAccount = userService.findUserByExternalID(externalID, task.getUserName());
        if (remoteAccount != null) {
            task.setLocalUserID(remoteAccount.getId());
        }
    }

}
