package timeboard.core.notification.model;

import timeboard.core.model.User;

import java.util.List;

public class UserNotificationStructure {

    User targetUser;

    public UserNotificationStructure(User targetUser) {
        this.targetUser = targetUser;
    }

    List<NotificationEvent> notificationEventList;
    List<NotificationEvent> informEventList;

    public void notify(NotificationEvent event){
        notificationEventList.add(event);
    }

    public void inform(NotificationEvent event){
        informEventList.add(event);

    }
}
