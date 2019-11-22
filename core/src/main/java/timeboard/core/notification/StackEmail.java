package timeboard.core.notification;

import io.reactivex.schedulers.Schedulers;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import timeboard.core.api.EmailService;
import timeboard.core.api.TimeboardSubjects;
import timeboard.core.model.User;
import timeboard.core.notification.model.NotificationEvent;
import timeboard.core.notification.model.EmailStructure;
import timeboard.core.notification.model.UserNotificationStructure;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Component(
        service = StackEmail.class,
        immediate = true
)
public class StackEmail {

    @Reference
    EmailService emailService;

    @Activate
    public void activate(){

        TimeboardSubjects.CREATE_TASK
                .observeOn(Schedulers.from(Executors.newFixedThreadPool(10)))
                .buffer(10, TimeUnit.SECONDS)
                .map(timedEvents -> notificationEventToUserEvent(timedEvents))
                .flatMap(userNotificationStructures -> return  )
                .subscribe(userNotificationStructure ->this.emailService.sendMessage(generateMailFromEventList(userNotificationStructure)));
    }

    private EmailStructure generateMailFromEventList(Collection<UserNotificationStructure> userNotificationStructures) {
        for(UserNotificationStructure userStructure : userNotificationStructures){

        }


            String subject = "Mail de création d'une tâche";
            String message = "Bonjour,\n"
                + actor.getFirstName() + " " + actor.getName() + " a ajouté une tâche au " + this.getDisplayFormatDate(new Date()) + "\n"
                +"Nom de la tâche : " + newTaskDB.getName() + "\n"
                +"Date de début : " + this.getDisplayFormatDate(newTaskDB.getStartDate()) + "\n"
                +"Date de fin : " + this.getDisplayFormatDate(newTaskDB.getEndDate()) + "\n"
                +"Estimation initiale : " + newTaskDB.getEstimateWork() + "\n"
                +"Projet : " + project.getName() + "\n";

        return new EmailStructure(to, cc, subject, message);

    }

    private List<UserNotificationStructure> notificationEventToUserEvent(List<NotificationEvent> events) {
        HashMap<User, UserNotificationStructure> dataList = new HashMap<User, UserNotificationStructure>();

        for(NotificationEvent event : events){
            for(User user : event.getUsersToNotify()){
                dataList.computeIfAbsent(user, t -> new UserNotificationStructure(user)).notify(event);
            }
            for(User user : event.getUsersToInform()){
                dataList.computeIfAbsent(user, t -> new UserNotificationStructure(user)).inform(event);
            }
        }
        return new ArrayList<>(dataList.values());
    }

}
