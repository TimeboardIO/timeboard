package timeboard.core.async;

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
import timeboard.core.api.sync.RemoteTask;
import timeboard.core.model.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Component
public final class ProjectSyncJob implements Job {

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
            final List<ProjectSyncCredentialField> credentials =
                    (List<ProjectSyncCredentialField>) context.getMergedJobDataMap().get(CREDENTIELS);

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
