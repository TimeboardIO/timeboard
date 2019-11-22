package timeboard.core.notification.model;

import timeboard.core.model.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NotificationEvent {

    private Object target;

    private NotificationEventType type;

    public List<User> getUsersToNotify() {
        return usersToNotify;
    }

    public List<User> getUsersToInform() {
        return usersToInform;
    }

    private List<User> usersToNotify;
    private List<User> usersToInform;

    public NotificationEvent(Object target, NotificationEventType type) {
        this.target = target;
        if (type == NotificationEventType.TASK_CREATION) {
            if (target instanceof Task) {
                Task task = (Task) target;
                this.constructUsersListFromTaskCreation(task);
            }
        } else if (type == NotificationEventType.TIMESHEET_VALIDATION) {
            if (target instanceof ValidatedTimesheet) {
                ValidatedTimesheet ts = (ValidatedTimesheet) target;
                this.constructUsersListFromTimesheetValidation(ts);

            }
        }
    }

    public Object getTarget() {
        return target;
    }

    public enum NotificationEventType {
        TASK_CREATION,
        TIMESHEET_VALIDATION;


    }

    private void constructUsersListFromTaskCreation(Task task){

        usersToNotify = new ArrayList<>();
        Project project = task.getProject();
        User actor = task.getLatestRevision().getRevisionActor();
        User assignedUser = task.getLatestRevision().getAssigned();

        project.getMembers()
                .stream()
                .filter(member -> member.getRole() == ProjectRole.OWNER)
                .forEach(member -> usersToNotify.add(member.getMember()));

        usersToInform = Arrays.asList(assignedUser, actor);

    }


    private void constructUsersListFromTimesheetValidation(ValidatedTimesheet ts){
        usersToNotify = new ArrayList<>(); //TODO
    }


}
