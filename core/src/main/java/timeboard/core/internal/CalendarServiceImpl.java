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

import net.fortuna.ical4j.data.CalendarBuilder;

import net.fortuna.ical4j.model.*;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.parameter.Value;
import net.fortuna.ical4j.model.property.Description;
import net.fortuna.ical4j.model.property.RRule;
import net.fortuna.ical4j.model.property.Summary;
import net.fortuna.ical4j.model.property.Uid;

import org.apache.aries.jpa.template.JpaTemplate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceScope;
import org.osgi.service.log.LogService;
import timeboard.core.api.CalendarService;
import timeboard.core.api.ProjectService;
import timeboard.core.api.exceptions.BusinessException;
import timeboard.core.model.DefaultTask;
import timeboard.core.model.User;

import javax.persistence.TypedQuery;
import java.io.FileInputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Calendar;
import java.util.Date;

@org.osgi.service.component.annotations.Component(
        service = CalendarService.class
)
public class CalendarServiceImpl implements CalendarService {

    @Reference(target = "(osgi.unit.name=timeboard-pu)", scope = ReferenceScope.BUNDLE)
    private JpaTemplate jpa;

    @Reference
    private ProjectService projectService;

    @Reference
    private LogService logService;

    private static final String CALENDAR_ORIGIN_KEY = "calendar" ;

    @Override
    public boolean importCalendarFromICS(User actor, String name, String url) throws BusinessException {
        net.fortuna.ical4j.model.Calendar parsedCalendar;
        CalendarBuilder builder = new CalendarBuilder();
        try {
            FileInputStream fin = new FileInputStream(url);
             parsedCalendar = builder.build(fin);
        }catch (Exception e){
            // parsing exception
            return false;
        }
        /* -- Calendar -- */
        String calendarName = name;
        if(name == null){
            calendarName = (parsedCalendar.getProperty(Property.PRODID) != null ? parsedCalendar.getProperty(Property.PRODID).getValue() : null);
        }
        String calendarRemoteId = parsedCalendar.getProperty(Property.PRODID).getValue();
        calendarRemoteId += "/"+ calendarName;
        timeboard.core.model.Calendar timeboardCalendar = this.createOrUpdateCalendar(calendarName, calendarRemoteId);

        /* -- Events -- */
        Map<String, List<DefaultTask>> calendarEvents = this.findAllEventInCalendar(timeboardCalendar);

        for (Object o : parsedCalendar.getComponents(Component.VEVENT)) {

            VEvent parsedEvent = (VEvent) o;
            Uid uid = parsedEvent.getUid();
            DefaultTask timeboardEvent = null;
            List<DefaultTask> existingEventsWithSameId = calendarEvents.get(uid.getValue());

            if(existingEventsWithSameId.isEmpty()){
                // no existing events, so create it
                timeboardEvent = new DefaultTask();
                this.icsToTimeboard(parsedEvent, timeboardEvent);
                timeboardEvent.setRemotePath(calendarRemoteId);
                this.projectService.createDefaultTask(timeboardEvent);

            } else if(existingEventsWithSameId.size() == 1){
                //1 existing event, so update it
                timeboardEvent = existingEventsWithSameId.get(0);
                this.icsToTimeboard(parsedEvent, timeboardEvent);
                timeboardEvent.setRemotePath(calendarRemoteId);
                this.projectService.updateDefaultTask(timeboardEvent);
            }

            Property rRule = parsedEvent.getProperty(Property.RRULE);
            if(rRule != null){
                this.createRecurringEvents(timeboardEvent, (RRule) rRule, existingEventsWithSameId);
            }
        }

        // only deleted tasks are remaining
        for(List<DefaultTask> toDeleteList : calendarEvents.values()){
            for(DefaultTask toDelete : toDeleteList) {
                this.projectService.deleteTaskByID(actor, toDelete.getId());
            }
        }

        this.logService.log(LogService.LOG_INFO, "Import successful ");

        return true;
    }

    private void icsToTimeboard(VEvent icsEvent, DefaultTask timeboardEvent) {

        Uid uid = icsEvent.getUid();

        if(uid != null) timeboardEvent.setRemoteId(uid.getValue());

        Summary summary = icsEvent.getSummary();
        if(summary != null) timeboardEvent.setName(summary.getValue());
        else timeboardEvent.setName("");

        Description description = icsEvent.getDescription();
        if(description != null) timeboardEvent.setComments(description.getValue());
        else timeboardEvent.setComments("");

        timeboardEvent.setStartDate(this.getJavaDateFromProperty(icsEvent.getStartDate()));
        timeboardEvent.setEndDate(this.getJavaDateFromProperty(icsEvent.getEndDate()));
        if(timeboardEvent.getEndDate() == null) timeboardEvent.setEndDate(timeboardEvent.getStartDate()); //if no end date then set it to start date

        timeboardEvent.setOrigin(CALENDAR_ORIGIN_KEY);

    }

    private void createRecurringEvents(DefaultTask dataEvent, RRule rule, List<DefaultTask> existingEvents) throws BusinessException {
        Recur recur = rule.getRecur();

        // Today
        java.util.Calendar startDate = java.util.Calendar.getInstance();
        startDate.set(java.util.Calendar.HOUR_OF_DAY, 9);

        // Today + 1 year
        java.util.Calendar endDate = java.util.Calendar.getInstance();
        endDate.set(java.util.Calendar.HOUR_OF_DAY, 9);
        endDate.roll(java.util.Calendar.YEAR, 1);

        //Create recurring tasks from recurring rules
        DateList dates = recur.getDates(
            new net.fortuna.ical4j.model.Date(startDate.getTime()),
            new net.fortuna.ical4j.model.Date(endDate.getTime()),
            Value.DATE);

        Iterator<Date> it = dates.iterator();
        while(it.hasNext()) {
            Date icsDate = it.next();
            java.util.Date javaDate= new java.util.Date();
            javaDate.setTime(icsDate.getTime());

            DefaultTask taskToUpdate = this.getTaskByStartDate(existingEvents,javaDate);
            if(taskToUpdate != null){ //recurring task exist, so update it
                this.copyTask(dataEvent, taskToUpdate);
                taskToUpdate.setStartDate(javaDate);
                taskToUpdate.setEndDate(javaDate); //TODO start and end date are set equals is this functionally correct ?
                this.projectService.updateDefaultTask(taskToUpdate);
                existingEvents.remove(taskToUpdate); // remove to construct deleted orphan list
            }else{  //recurring task does not  exist, so create it
                DefaultTask newTask = this.cloneWithDate(dataEvent, icsDate, icsDate);  //TODO start and end date are set equals  is this functionally correct ?
                this.projectService.createDefaultTask(newTask);
            }
        }
    }

    private void copyTask(DefaultTask source, DefaultTask target) {
        // copy without dates
        target.setName(source.getName());
        target.setComments(source.getComments());
        target.setOrigin(source.getOrigin());
        target.setRemotePath(source.getRemotePath());
        target.setRemoteId(source.getRemoteId());
    }

    private DefaultTask getTaskByStartDate(List<DefaultTask> tasks, java.util.Date date) {
        DefaultTask result = null;
        for(DefaultTask t : tasks){
            if(t.getStartDate().equals(date)){
                result = t;
            }
        }
        return result;
    }

    public timeboard.core.model.Calendar createOrUpdateCalendar(String name, String remoteId){

        timeboard.core.model.Calendar calendar = null;

        try{
             calendar = this.jpa.txExpr(entityManager -> {
                TypedQuery<timeboard.core.model.Calendar> q = entityManager.createQuery("select c from Calendar c where c.remoteId = :remoteId", timeboard.core.model.Calendar.class);
                q.setParameter("remoteId", remoteId);
                return q.getSingleResult();
            });
        } finally {
            if(calendar == null) { // create
                timeboard.core.model.Calendar newCalendar = new timeboard.core.model.Calendar();
                newCalendar.setRemoteId(remoteId);
                newCalendar.setName(name);
                this.jpa.txExpr(entityManager -> {
                    entityManager.persist(newCalendar);
                    return newCalendar;
                });
            } else { // update
                final timeboard.core.model.Calendar toUpdateCalendar = calendar;
                calendar.setName(name);
                calendar.setRemoteId(remoteId);
                this.jpa.txExpr(entityManager -> {
                    entityManager.merge(toUpdateCalendar);
                    return toUpdateCalendar;
                });
            }
        }

        return calendar;
    }

    @Override
    public List<timeboard.core.model.Calendar> listCalendars() {
        return this.jpa.txExpr(entityManager -> {
            TypedQuery<timeboard.core.model.Calendar> q = entityManager.createQuery("select c from Calendar c", timeboard.core.model.Calendar.class);
            return q.getResultList();
        });
    }

    @Override
    public List<DefaultTask> findExistingEvents(String remotePath, String remoteId) {
        return this.jpa.txExpr(entityManager -> {
            TypedQuery<DefaultTask> q = entityManager.createQuery("select d from DefaultTask d where d.remotePath = :remotePath and d.remoteId = :remoteId", DefaultTask.class);
            q.setParameter("remotePath", remotePath);
            q.setParameter("remoteId", remoteId);
            q.setParameter("origin", CALENDAR_ORIGIN_KEY);
            return q.getResultList();
        });
    }

    @Override
    public Map<String, List<DefaultTask>> findAllEventInCalendar(timeboard.core.model.Calendar calendar) {
        List<DefaultTask> eventList =  this.jpa.txExpr(entityManager -> {
            TypedQuery<DefaultTask> q = entityManager.createQuery("select d from DefaultTask d where d.remotePath = :remotePath and d.origin = :origin", DefaultTask.class);
            q.setParameter("remotePath", calendar.getRemoteId());
            q.setParameter("origin", CALENDAR_ORIGIN_KEY);
            return q.getResultList();
        });

        Map<String, List<DefaultTask>> idToEventList = new HashMap();
        for(DefaultTask event : eventList){
            List<DefaultTask> currentList = idToEventList.get(event.getRemoteId());
            if(currentList == null){
                currentList = idToEventList.put(event.getRemoteId(), new ArrayList<>());
            }
            currentList.add(event);
        }

        return idToEventList;
    }


    private DefaultTask cloneWithDate(DefaultTask source, Date startDate, Date endDate){

        DefaultTask clone = (DefaultTask) source.clone();

        clone.setId(null);
        clone.setStartDate(startDate);
        clone.setEndDate(endDate);

        return clone;
    }

    private java.util.Date getJavaDateFromProperty(Property p){
        java.util.Date date = null;
        try{
            if(p != null){
                SimpleDateFormat sdf;
                if(p.getValue().length() == 16) sdf = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'");
                else sdf = new SimpleDateFormat("yyyyMMdd");
                sdf.setTimeZone(java.util.TimeZone.getTimeZone("GMT"));
                date = sdf.parse(p.getValue());
            }

        }catch(ParseException e){
            //TODO handle parse exception
        }
        return date;
    }

}
