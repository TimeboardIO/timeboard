package timeboard.core.internal.observers.emails;

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

import io.reactivex.schedulers.Schedulers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import timeboard.core.api.EmailService;
import timeboard.core.api.TimeboardSubjects;
import timeboard.core.api.events.*;
import timeboard.core.model.Account;
import timeboard.core.model.SubmittedTimesheet;
import timeboard.core.model.Task;
import timeboard.core.model.ValidationStatus;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Component
public class SendSummaryEmail {

    @Autowired
    private EmailService emailService;

    @Value("${timeboard.mail.buffer.time}")
    private Long bufferTime;

    private TemplateGenerator templateGenerator = new TemplateGenerator();

    @PostConstruct
    public void activate() {
        TimeboardSubjects.TIMEBOARD_EVENTS // Listen for all timeboard app events
                .observeOn(Schedulers.from(Executors.newFixedThreadPool(10))) // Observe on 10 threads
                .buffer(bufferTime, TimeUnit.MINUTES) // Aggregate mails every "bufferTime" minutes
                .map(this::notificationEventToUserEvent) // Rebalance events by user to notify/inform
                .flatMapIterable(l -> l) // transform user list to single events
                .subscribe(struc -> this.emailService.sendMessage(generateMailFromEventList(struc))); //create and send individual summary
    }

    /**
     * Transform user with his notifications to email structure.
     * Work for task create/delete events and timesheet submission events
     *
     * @param userNotificationStructure structure user 1 -- * events to notify/inform
     * @return email ready structure
     */
    private EmailStructure generateMailFromEventList(final UserNotificationStructure userNotificationStructure) {

        final Map<String, Object> data = new HashMap<>();

        final List<SubmittedTimesheet> submittedTimesheets = new ArrayList<>();
        final List<VacationEvent> vacationEvents = new ArrayList<>();
        final Map<Long, ProjectEmailSummaryModel> projects = new HashMap<>();

        for (final TimeboardEvent event : userNotificationStructure.getNotificationEventList()) {
            if (event instanceof TaskEvent) {
                final Task t = ((TaskEvent) event).getTask();
                if (((TaskEvent) event).getEventType() == TimeboardEventType.CREATE) {
                    projects.computeIfAbsent(t.getProject().getId(), e ->
                            new ProjectEmailSummaryModel(t.getProject())).addCreatedTask((TaskEvent) event);
                }
                if (((TaskEvent) event).getEventType() == TimeboardEventType.DELETE) {
                    projects.computeIfAbsent(t.getProject().getId(), e ->
                            new ProjectEmailSummaryModel(t.getProject())).addDeletedTask((TaskEvent) event);
                }

            } else if (event instanceof TimesheetEvent) {
                submittedTimesheets.add(((TimesheetEvent) event).getTimesheet());
            } else if (event instanceof VacationEvent) {
                vacationEvents.add((VacationEvent) event);
            }

        }
        data.put("projects", projects.values());
        data.put("timesheetSubmitted", submittedTimesheets.stream()
                .filter(timesheet -> timesheet.getTimesheetStatus().equals(ValidationStatus.PENDING_VALIDATION)).toArray());
        data.put("timesheetRejected", submittedTimesheets.stream()
                .filter(timesheet -> timesheet.getTimesheetStatus().equals(ValidationStatus.REJECTED)).toArray());
        data.put("timesheetValidated", submittedTimesheets.stream()
                .filter(timesheet -> timesheet.getTimesheetStatus().equals(ValidationStatus.VALIDATED)).toArray());
        data.put("vacationEventsCreated", vacationEvents.stream().filter(e -> e.getEventType().equals(TimeboardEventType.CREATE)).toArray());
        data.put("vacationEventsApproved", vacationEvents.stream().filter(e -> e.getEventType().equals(TimeboardEventType.APPROVE)).toArray());
        data.put("vacationEventsDenied", vacationEvents.stream().filter(e -> e.getEventType().equals(TimeboardEventType.DENY)).toArray());
        data.put("vacationEventsDeleted", vacationEvents.stream().filter(e -> e.getEventType().equals(TimeboardEventType.DELETE)).toArray());

        final String message = templateGenerator.getTemplateString("mail/summary.html", data);
        final ArrayList<String> list = new ArrayList<>();
        list.add(userNotificationStructure.getTargetAccount().getEmail());
        final String subject = "[Timeboard] Daily summary";
        return new EmailStructure(list, null, subject, message);
    }


    /**
     * Rebalance events by user to notify/inform.
     *
     * @param events list of events
     * @return userNotificationStructure structure user 1 -- * events to notify/inform
     */
    private List<UserNotificationStructure> notificationEventToUserEvent(final List<TimeboardEvent> events) {
        final HashMap<Long, UserNotificationStructure> dataList = new HashMap<>();

        for (final TimeboardEvent event : events) {
            for (final Account account : event.getUsersToNotify()) {
                dataList.computeIfAbsent(account.getId(), t -> new UserNotificationStructure(account)).notify(event);
            }
            for (final Account account : event.getUsersToInform()) {
                dataList.computeIfAbsent(account.getId(), t -> new UserNotificationStructure(account)).inform(event);
            }
        }
        return new ArrayList<>(dataList.values());
    }


}
