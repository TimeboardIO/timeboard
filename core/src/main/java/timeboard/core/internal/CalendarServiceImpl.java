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
import net.fortuna.ical4j.filter.Filter;
import net.fortuna.ical4j.filter.PeriodRule;
import net.fortuna.ical4j.model.*;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.Description;
import net.fortuna.ical4j.model.property.RRule;
import net.fortuna.ical4j.model.property.Summary;
import net.fortuna.ical4j.model.property.Uid;
import org.apache.aries.jpa.template.JpaTemplate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceScope;
import timeboard.core.api.CalendarService;
import timeboard.core.api.ProjectService;
import timeboard.core.api.exceptions.BusinessException;
import timeboard.core.model.DefaultTask;

import java.io.FileInputStream;
import java.text.SimpleDateFormat;

@org.osgi.service.component.annotations.Component(
        service = CalendarService.class
)
public class CalendarServiceImpl implements CalendarService {

    @Reference(target = "(osgi.unit.name=timeboard-pu)", scope = ReferenceScope.BUNDLE)
    private JpaTemplate jpa;

    @Reference
    private ProjectService projectService;

    private final static String CALENDAR_ORIGIN_KEY = "calendar" ;

    @Override
    public boolean importCalendarFromICS(String url) throws BusinessException {
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

            for (Object o : cal.getComponents(Component.VEVENT)) {
                VEvent event = (VEvent) o;
                DefaultTask dataEvent = new DefaultTask();

                dataEvent.setStartDate(this.getJavaDateFromProperty(event.getStartDate()));
                dataEvent.setEndDate(this.getJavaDateFromProperty(event.getEndDate()));

                dataEvent.setOrigin(CALENDAR_ORIGIN_KEY);
                dataEvent.setRemotePath(prodId);

                Uid uid = event.getUid();
                if(uid != null) dataEvent.setRemoteId(uid.getValue());

                Summary summary = event.getSummary();
                if(summary != null) dataEvent.setName(summary.getValue());

                Description description = event.getDescription();
                if(description != null)dataEvent.setName(description.getValue());

                this.projectService.createdDefaultTask(dataEvent);

                /*Property rRule = event.getProperty(Property.RRULE);
                 if(rRule != null){
                        this.createRecurringEvents(dataEvent, (RRule) rRule);
                 }*/

            }



        return true;
    }

    private void createRecurringEvents(DefaultTask dataEvent, RRule rule) {
        rule.getRecur();

    }


    public timeboard.core.model.Calendar createCalendar(String name, String remoteId){
        return this.jpa.txExpr(entityManager -> {
            timeboard.core.model.Calendar calendar = new timeboard.core.model.Calendar();

            calendar.setName(name);
            calendar.setProdID(remoteId);

            return calendar;
        });
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
