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

import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.util.Collection;

public class CalendarTest {

    public static void main(String[] args) {

        String url = "/home/nicolas/dev/Timeboard/autres/nicolas.lefloch.info@gmail.com.ics";
       // StringReader sin = new StringReader(myCalendarString);

        CalendarBuilder builder = new CalendarBuilder();

        try{
            FileInputStream fin = new FileInputStream(url);
            Calendar cal = builder.build(fin);
            for (Object o : cal.getComponents(Component.VEVENT)) {
                Component c = (Component)o;
                if(c.getProperty(Property.SUMMARY).getValue().matches(".*Thales.*")){
                    System.out.println(c.getProperty(Property.SUMMARY).getValue());
                   // c.getProperty(Property.RRULE);
                }
                try{
                    Property p = c.getProperty(Property.DTSTART);
                    SimpleDateFormat sdf;
                    if(p.getValue().length() == 16) sdf = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'");
                    else sdf = new SimpleDateFormat("yyyyMMdd");
                    sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
                    java.util.Date jd = sdf.parse(p.getValue());
                    System.out.println(jd);

                }catch( Exception e){
                    //TODO handle this
                    e.printStackTrace();
                }
            }
            java.util.Calendar today = java.util.Calendar.getInstance();
            today.set(java.util.Calendar.HOUR_OF_DAY, 0);
            today.clear(java.util.Calendar.MINUTE);
            today.clear(java.util.Calendar.SECOND);

            // create a period starting now with a duration of one (1) day..
            Period period = new Period(new DateTime(today.getTime()), new Dur(1, 0, 0, 0));
            Filter filter = new Filter(new PeriodRule(period));

            Collection<Component> eventsToday = filter.filter(cal.getComponents(Component.VEVENT));
            for ( Component c : eventsToday){
                System.out.println("TODAY: "+c.getProperty(Property.SUMMARY).getValue());
            }

        }catch( Exception e){
            //TODO handle this
                e.printStackTrace();
        }

    }
}
