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
import org.springframework.stereotype.Component;
import timeboard.core.api.ProjectService;
import timeboard.core.api.UserService;
import timeboard.core.api.sync.ProjectSyncCredentialField;
import timeboard.core.api.sync.ProjectSyncPlugin;
import timeboard.core.api.sync.ProjectSyncService;
import timeboard.core.internal.async.ProjectSyncJob;
import timeboard.core.model.Account;
import timeboard.core.model.Project;

import java.util.Date;
import java.util.List;

@Component
public class ProjectSyncPluginImpl implements ProjectSyncService {

    @Autowired
    private ProjectService projectService;

    @Autowired
    private UserService userService;

    @Autowired
    private Scheduler scheduler;

    @Autowired
    private List<ProjectSyncPlugin> projectImportServiceList;

    @Override
    public void syncProjectTasksWithSchedule(final Long orgID,
                                             final Account actor,
                                             final Project project,
                                             final String serviceName,
                                             final List<ProjectSyncCredentialField> jiraCredentials,
                                             final CronScheduleBuilder cronScheduleBuilder) {


        final JobDetail jobDetails = buildJobDetails(serviceName, project);

        try {
            final JobDataMap data = new JobDataMap();
            data.put(ACCOUNT_ID, actor.getId());
            data.put(CREDENTIALS, jiraCredentials);
            data.put(ORG_ID, orgID);
            data.put(SERVICE_NAME, serviceName);
            data.put(PROJECT_ID, project.getId());

            this.scheduler.addJob(jobDetails, true);

            final Trigger trigger = TriggerBuilder.newTrigger()
                    .forJob(jobDetails)
                    .startAt(new Date())
                    .usingJobData(data)
                    .withSchedule(cronScheduleBuilder)
                    .build();

            this.scheduler.scheduleJob(trigger);


        } catch (final SchedulerException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void syncProjectTasks(final Long orgID,
                                 final Account actor,
                                 final Project project,
                                 final String serviceName,
                                 final List<ProjectSyncCredentialField> jiraCredentials) {


        final JobDetail jobDetails = buildJobDetails(serviceName, project);

        try {
            final JobDataMap data = new JobDataMap();
            data.put(ACCOUNT_ID, actor.getId());
            data.put(CREDENTIALS, jiraCredentials);
            data.put(ORG_ID, orgID);
            data.put(SERVICE_NAME, serviceName);
            data.put(PROJECT_ID, project.getId());

            this.scheduler.addJob(jobDetails, true);

            final Trigger trigger = TriggerBuilder.newTrigger()
                    .forJob(jobDetails)
                    .startAt(new Date())
                    .usingJobData(data)
                    .build();

            this.scheduler.scheduleJob(trigger);


        } catch (final SchedulerException e) {
            e.printStackTrace();
        }

    }

    private JobDetail buildJobDetails(final String serviceName, final Project project) {

        return JobBuilder.newJob()
                .withIdentity(new JobKey(project.getId().toString()))
                .withDescription(serviceName + " Job")
                .ofType(ProjectSyncJob.class)
                .requestRecovery(false)
                .storeDurably(true)
                .build();
    }

    @Override
    public List<ProjectSyncCredentialField> getServiceFields(final String serviceName) {
        final ProjectSyncPlugin syncService = this.projectImportServiceList.stream()
                .filter(projectSyncPlugin -> projectSyncPlugin.getServiceName().equals(serviceName))
                .findFirst().get();
        return syncService.getSyncCredentialFields();
    }


}
