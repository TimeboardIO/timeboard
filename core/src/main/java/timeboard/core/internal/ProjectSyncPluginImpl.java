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

import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import timeboard.core.TimeboardAuthentication;
import timeboard.core.api.OrganizationService;
import timeboard.core.api.ProjectService;
import timeboard.core.api.UserService;
import timeboard.core.api.exceptions.BusinessException;
import timeboard.core.api.sync.ProjectSyncCredentialField;
import timeboard.core.api.sync.ProjectSyncPlugin;
import timeboard.core.api.sync.ProjectSyncService;
import timeboard.core.api.sync.RemoteTask;
import timeboard.core.async.AsyncJobService;
import timeboard.core.model.*;

import java.util.*;

@Component
public class ProjectSyncPluginImpl implements ProjectSyncService {

    public static final String ORG_ID = "orgID";
    public static final String ACCOUNT_ID = "accountID";
    public static final String PROJECT_ID = "projectID";
    public static final String SERVICE_NAME = "serviceName";
    public static final String CREDENTIELS = "credentiels";
    @Autowired
    private AsyncJobService asyncJobService;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private UserService userService;

    @Autowired
    private Scheduler scheduler;

    @Autowired
    private List<ProjectSyncPlugin> projectImportServiceList;

    @Override
    public void syncProjectTasks(final Long orgID,
                                 final Account actor,
                                 final Project project,
                                 final String serviceName,
                                 final List<ProjectSyncCredentialField> jiraCrendentials) {


        final JobDetail jobDetails = new JobDetail() {

            private UUID key = UUID.randomUUID();

            @Override
            public JobKey getKey() {
                return new JobKey("timeboard-" + key.toString());
            }

            @Override
            public String getDescription() {
                return serviceName + " Job";
            }

            @Override
            public Class<? extends Job> getJobClass() {
                return ProjectSyncJob.class;
            }

            @Override
            public JobDataMap getJobDataMap() {
                return null;
            }

            @Override
            public boolean isDurable() {
                return true;
            }

            @Override
            public boolean isPersistJobDataAfterExecution() {
                return true;
            }

            @Override
            public boolean isConcurrentExectionDisallowed() {
                return true;
            }

            @Override
            public boolean requestsRecovery() {
                return true;
            }

            @Override
            public JobBuilder getJobBuilder() {
                return JobBuilder.newJob(getJobClass());
            }

            @Override
            public Object clone() {
                return this;
            }
        };

        try {
            final JobDataMap data = new JobDataMap();
            data.put(ACCOUNT_ID, actor.getId());
            data.put(CREDENTIELS, jiraCrendentials);
            data.put(ORG_ID, orgID);
            data.put(SERVICE_NAME, serviceName);
            data.put(PROJECT_ID, project.getId());

            this.scheduler.addJob(jobDetails, true);

            final Trigger trigger = TriggerBuilder.newTrigger()
                    .forJob(jobDetails)
                    .startAt(new Date())
                    .usingJobData(data)
                    .withSchedule(CronScheduleBuilder.cronSchedule("0 0/1 * 1/1 * ? *"))
                    .build();

            this.scheduler.scheduleJob(trigger);


        } catch (SchedulerException e) {
            e.printStackTrace();
        }

    }

    @Override
    public List<ProjectSyncCredentialField> getServiceFields(String serviceName) {
        final ProjectSyncPlugin syncService = this.projectImportServiceList.stream()
                .filter(projectSyncPlugin -> projectSyncPlugin.getServiceName().equals(serviceName))
                .findFirst().get();
        return syncService.getSyncCredentialFields();
    }


    @Component
    public static final class ProjectSyncJob implements Job {

        @Autowired
        private ProjectService projectService;

        @Autowired
        private List<ProjectSyncPlugin> projectImportServiceList;

        @Autowired
        private UserService userService;

        @Autowired
        private OrganizationService organizationService;


        @Override
        public void execute(JobExecutionContext context) throws JobExecutionException {
            try {

                final long orgID = context.getMergedJobDataMap().getLong(ORG_ID);
                final long accountID = context.getMergedJobDataMap().getLong(ACCOUNT_ID);
                final long projectID = context.getMergedJobDataMap().getLong(PROJECT_ID);
                final String serviceName = context.getMergedJobDataMap().getString(SERVICE_NAME);
                final List<ProjectSyncCredentialField> credentials = (List<ProjectSyncCredentialField>) context.getMergedJobDataMap().get(CREDENTIELS);

                final ProjectSyncPlugin syncService = this.projectImportServiceList.stream()
                        .filter(projectSyncPlugin -> projectSyncPlugin.getServiceName().equals(serviceName))
                        .findFirst().get();

                final Account actor = this.userService.findUserByID(accountID);

                SecurityContextHolder.getContext().setAuthentication(new TimeboardAuthentication(actor));

                final Optional<Organization> org = this.organizationService.getOrganizationByID(actor, orgID);
                final Project project = this.projectService.getProjectByID(actor, orgID, projectID);

                context.setResult(this.syncProjectTasks(org.get(), actor, project, syncService, credentials));

            } catch (Exception e) {
                context.setResult(e);
            }
        }

        private Object syncProjectTasks(Organization org,
                                        Account actor,
                                        Project project,
                                        ProjectSyncPlugin syncService,
                                        List<ProjectSyncCredentialField> jiraCrendentials) throws Exception {


            final List<RemoteTask> remoteTasks = syncService.getRemoteTasks(actor, jiraCrendentials);

            remoteTasks.stream()
                    .forEach(task -> {
                        try {
                            mergeAssignee(userService, syncService.getServiceName(), task);
                        } catch (Exception e) {

                        }
                    });

            this.syncTasks(actor, project, remoteTasks);


            return String.format("Sync %s tasks from %s", remoteTasks.size(), syncService.getServiceName());
        }

        private void syncTasks(final Account actor, final Project project, final List<RemoteTask> remoteTasks) throws BusinessException {
            final List<RemoteTask> newTasks = new ArrayList<>();
            for (RemoteTask task1 : remoteTasks) {
                if (isNewTask(actor, project.getId(), task1)) {
                    newTasks.add(task1);
                }
            }

            final List<RemoteTask> updatedTasks = new ArrayList<>();
            for (RemoteTask task1 : remoteTasks) {
                if (isUpdated(actor, project.getId(), task1)) {
                    updatedTasks.add(task1);
                }
            }


            this.createTasks(actor, project, newTasks);

            for (RemoteTask remoteTask : updatedTasks) {
                Optional<Task> taskToUpdate = projectService.getTaskByRemoteID(actor, remoteTask.getId());
                if (taskToUpdate.isPresent()) {
                    taskToUpdate.get().setName(remoteTask.getTitle());
                    projectService.updateTask(actor, taskToUpdate.get());
                }
            }
        }


        private void createTasks(Account actor, Project project, List<RemoteTask> newTasks) {
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
                        String remoteId = task.getId();
                        Batch batch = null;
                        projectService.createTask(actor, project, taskName, taskComment,
                                startDate, endDate, originaEstimate, taskTypeID, assignedAccountID, origin,
                                remotePath, String.valueOf(remoteId), TaskStatus.IN_PROGRESS, batch);
                    }
            );
        }


        private boolean isUpdated(Account actor, long projectID, RemoteTask task) throws BusinessException {
            return !this.isNewTask(actor, projectID, task);
        }

        private boolean isNewTask(Account actor, long projectID, RemoteTask task) throws BusinessException {
            final Optional<Task> existingTask = this.projectService.getTaskByRemoteID(actor, task.getId());
            return !existingTask.isPresent();
        }

        private void mergeAssignee(UserService userService, String externalID, RemoteTask task) {
            final Account remoteAccount = userService.findUserByExternalID(externalID, task.getUserName());
            if (remoteAccount != null) {
                task.setLocalUserID(remoteAccount.getId());
            }
        }
    }

}
