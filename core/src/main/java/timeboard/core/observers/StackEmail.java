package timeboard.core.observers;

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

import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import timeboard.core.api.EmailService;
import timeboard.core.api.TimeboardSubjects;
import timeboard.core.model.User;
import timeboard.core.notification.model.EmailStructure;
import timeboard.core.notification.model.UserNotificationStructure;
import timeboard.core.notification.model.event.TaskEvent;
import timeboard.core.notification.model.event.TimeboardEvent;

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

        TimeboardSubjects.TASK_EVENTS
                .observeOn(Schedulers.from(Executors.newFixedThreadPool(10)))
                .buffer(10, TimeUnit.SECONDS)
                .map(timedEvents -> notificationEventToUserEvent(timedEvents))
                .flatMapIterable(l -> l)
                .subscribe(struc ->System.out.println(generateMailFromEventList(struc).getMessage()));
               // .subscribe(userNotificationStructure ->this.emailService.sendMessage(generateMailFromEventList(userNotificationStructure)));
    }

    private EmailStructure generateMailFromEventList(UserNotificationStructure userNotificationStructures) {

        String message = "";
        for(TimeboardEvent taskEvents : userNotificationStructures.getNotificationEventList()){
            message+= taskEvents.getEventDate();
        }
/*

            String subject = "Mail de création d'une tâche";
            String message = "Bonjour,\n"
                + actor.getFirstName() + " " + actor.getName() + " a ajouté une tâche au " + this.getDisplayFormatDate(new Date()) + "\n"
                +"Nom de la tâche : " + newTaskDB.getName() + "\n"
                +"Date de début : " + this.getDisplayFormatDate(newTaskDB.getStartDate()) + "\n"
                +"Date de fin : " + this.getDisplayFormatDate(newTaskDB.getEndDate()) + "\n"
                +"Estimation initiale : " + newTaskDB.getEstimateWork() + "\n"
                +"Projet : " + project.getName() + "\n";

        return new EmailStructure(to, cc, subject, message);*/
        return new EmailStructure(null, null, null, message);
    }

    private List<UserNotificationStructure> notificationEventToUserEvent(List<TaskEvent> events) {
        HashMap<User, UserNotificationStructure> dataList = new HashMap<User, UserNotificationStructure>();

        for(TaskEvent event : events){
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
