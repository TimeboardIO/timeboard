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
import timeboard.core.internal.TemplateGenerator;
import timeboard.core.model.EmailSummaryModel;
import timeboard.core.model.Task;
import timeboard.core.model.User;
import timeboard.core.model.ValidatedTimesheet;
import timeboard.core.notification.model.EmailStructure;
import timeboard.core.notification.model.UserNotificationStructure;
import timeboard.core.notification.model.event.TaskEvent;
import timeboard.core.notification.model.event.TimeboardEvent;
import timeboard.core.notification.model.event.TimeboardEventType;
import timeboard.core.notification.model.event.TimesheetEvent;

import java.text.SimpleDateFormat;
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

    TemplateGenerator templateGenerator = new TemplateGenerator();

    @Activate
    public void activate(){

        TimeboardSubjects.TIMEBOARD_EVENTS
                .observeOn(Schedulers.from(Executors.newFixedThreadPool(10)))
                .buffer(90, TimeUnit.SECONDS)
                .map(timedEvents -> notificationEventToUserEvent(timedEvents))
                .flatMapIterable(l -> l).onErrorResumeNext(e -> {
                    System.out.println(e);
                 })
                .subscribe(struc ->this.emailService.sendMessage(generateMailFromEventList(struc)));
    }

    private EmailStructure generateMailFromEventList(UserNotificationStructure userNotificationStructures) {

        Map<String, Object> data  = new HashMap<>();

        List<ValidatedTimesheet> validatedTimesheets = new ArrayList<>();


        Map<Long, EmailSummaryModel> projects = new HashMap<>();
        String subject = "[Timeboard] Daily summary";

        for(TimeboardEvent event : userNotificationStructures.getNotificationEventList()){

            if(event instanceof TaskEvent){
                Task t = ((TaskEvent) event).getTask();
                if(((TaskEvent) event).getEventType() == TimeboardEventType.CREATE) projects.computeIfAbsent(t.getProject().getId(), e -> new EmailSummaryModel(t.getProject())).addCreatedTask((TaskEvent) event);
                if(((TaskEvent) event).getEventType() == TimeboardEventType.DELETE) projects.computeIfAbsent(t.getProject().getId(), e -> new EmailSummaryModel(t.getProject())).addDeletedTask((TaskEvent) event);
            } else if(event instanceof TimesheetEvent){
                validatedTimesheets.add(((TimesheetEvent) event).getTimesheet());
            }

        }
        data.put("projects", projects.values());
        data.put("validatedTimesheets", validatedTimesheets);

        String message = templateGenerator.getTemplateString("core-ui:layouts/mail.html", data);
                ArrayList<String> list = new ArrayList<String>();
                list.add(userNotificationStructures.getTargetUser().getEmail());
        return new EmailStructure(list, null, subject, message);
    }


    private List<UserNotificationStructure> notificationEventToUserEvent(List<TimeboardEvent> events) {
        HashMap<Long, UserNotificationStructure> dataList = new HashMap<Long, UserNotificationStructure>();

        for(TimeboardEvent event : events){
            for(User user : event.getUsersToNotify()){
                dataList.computeIfAbsent(user.getId(), t -> new UserNotificationStructure(user)).notify(event);
            }
            for(User user : event.getUsersToInform()){
                dataList.computeIfAbsent(user.getId(), t -> new UserNotificationStructure(user)).inform(event);
            }
        }
        return new ArrayList<>(dataList.values());
    }
    private String getDisplayFormatDate(Date date){
        return  new SimpleDateFormat("dd/MM/yyyy").format(date);
    }





}
