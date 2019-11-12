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

import javax.persistence.TypedQuery;
import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;

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
    public boolean importCalendarFromICS(String name, String url) throws BusinessException {
        net.fortuna.ical4j.model.Calendar cal;
        CalendarBuilder builder = new CalendarBuilder();
            try {
                FileInputStream fin = new FileInputStream(url);
                 cal = builder.build(fin);
            }catch (Exception e){
                // parsing exception
                return false;
            }

            String prodId = cal.getProperty("PRODID").getValue();

            timeboard.core.model.Calendar dataCalendar = new timeboard.core.model.Calendar();
            dataCalendar.setProdID(prodId);
            dataCalendar.setName(prodId);


            this.createCalendar(name, prodId);


            for (Object o : cal.getComponents(Component.VEVENT)) {
                VEvent event = (VEvent) o;
                Uid uid = event.getUid();
                DefaultTask dataEvent = null;
                List<DefaultTask> existingEvents = this.findExistingEvents(prodId, uid.getValue());
                if(existingEvents.size() == 0){ // no existing events
                    dataEvent = new DefaultTask();
                    this.icsToTimeboard(event, dataEvent);
                    dataEvent.setRemotePath(prodId);
                    this.projectService.createDefaultTask(dataEvent);
                } else if(existingEvents.size() == 1){ //1 existing event
                    dataEvent =  existingEvents.get(0);
                    this.icsToTimeboard(event, dataEvent);
                    dataEvent.setRemotePath(prodId);
                    this.projectService.updateDefaultTask(dataEvent);
                }

                Property rRule = event.getProperty(Property.RRULE);
                 if(rRule != null){
                        this.createRecurringEvents(dataEvent, (RRule) rRule, existingEvents);
                 }


            }

        this.logService.log(LogService.LOG_INFO, "Import successful ");

        return true;
    }

    private void icsToTimeboard(VEvent event, DefaultTask dataEvent) {

        Uid uid = event.getUid();

        if(uid != null) dataEvent.setRemoteId(uid.getValue());

        Summary summary = event.getSummary();
        if(summary != null) dataEvent.setName(summary.getValue());
        else dataEvent.setName("");

        Description description = event.getDescription();
        if(description != null) dataEvent.setComments(description.getValue());
        else dataEvent.setComments("");


        dataEvent.setStartDate(this.getJavaDateFromProperty(event.getStartDate()));
        dataEvent.setEndDate(this.getJavaDateFromProperty(event.getEndDate()));
        if(dataEvent.getEndDate() == null) dataEvent.setEndDate(dataEvent.getStartDate()); //if no end date then set it to start date

        dataEvent.setOrigin(CALENDAR_ORIGIN_KEY);

    }

    private void createRecurringEvents(DefaultTask dataEvent, RRule rule, List<DefaultTask> existingEvents) {
        Recur recur = rule.getRecur();

        java.util.Calendar startDate = new GregorianCalendar();
        startDate.set(java.util.Calendar.MONTH, java.util.Calendar.NOVEMBER);
        startDate.set(java.util.Calendar.DAY_OF_MONTH, 1);
        startDate.set(java.util.Calendar.YEAR, 2019);
        startDate.set(java.util.Calendar.HOUR_OF_DAY, 9);
        startDate.set(java.util.Calendar.MINUTE, 0);
        startDate.set(java.util.Calendar.SECOND, 0);

        java.util.Calendar endDate = new GregorianCalendar();
        endDate.set(java.util.Calendar.MONTH, java.util.Calendar.NOVEMBER);
        endDate.set(java.util.Calendar.DAY_OF_MONTH, 31);
        endDate.set(java.util.Calendar.YEAR, 2019);
        endDate.set(java.util.Calendar.HOUR_OF_DAY, 9);
        endDate.set(java.util.Calendar.MINUTE, 0);
        endDate.set(java.util.Calendar.SECOND, 0);

        DateList dates = recur.getDates(
            new Date(startDate.getTime()),
            new Date(endDate.getTime()),
            Value.DATE);

        System.out.println(dates);

        Iterator<Date> it = dates.iterator();
        while(it.hasNext()) {
            Date d = it.next();
            DefaultTask newTask = this.cloneWithDate(dataEvent, d);
            try{
                this.projectService.createDefaultTask(newTask);
            }catch(Exception e){
                //hande todo
            }
        }
    }


    public timeboard.core.model.Calendar createCalendar(String name, String remoteId){
        return this.jpa.txExpr(entityManager -> {
            timeboard.core.model.Calendar calendar = new timeboard.core.model.Calendar();

            calendar.setName(name);
            calendar.setProdID(remoteId);

            entityManager.persist(calendar);
            return calendar;
        });
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
            return q.getResultList();
        });
    }


    private DefaultTask cloneWithDate(DefaultTask source, Date date){
        DefaultTask clone = (DefaultTask) source.clone();

        clone.setId(null);
        clone.setStartDate(date);
        clone.setEndDate(date);

        return clone;
    }

    private java.util.Date getJavaDateFromProperty(Property p){
        java.util.Date date = null;
        try{
            SimpleDateFormat sdf;
            if(p.getValue().length() == 16) sdf = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'");
            else sdf = new SimpleDateFormat("yyyyMMdd");
            sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
            date = sdf.parse(p.getValue());
        }catch( Exception e){
            //TODO handle this
        }
        return date;
    }

}
