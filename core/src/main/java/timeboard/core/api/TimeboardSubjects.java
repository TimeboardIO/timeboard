package timeboard.core.api;

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
import io.reactivex.subjects.PublishSubject;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import timeboard.core.model.User;
import timeboard.core.notification.model.event.TaskEvent;
import timeboard.core.notification.model.event.TimeboardEvent;
import timeboard.core.notification.model.event.TimesheetEvent;

import java.util.Map;


@Component(
        service = TimeboardSubjects.class,
        immediate = true
)
public class TimeboardSubjects {

    public static PublishSubject<TaskEvent> TASK_EVENTS =  PublishSubject.create();
    public static PublishSubject<TimesheetEvent> TIMESHEET_EVENTS =  PublishSubject.create();
    public static Observable<TimeboardEvent> TIMEBOARD_EVENTS =  PublishSubject.create();

    @Activate
    void activate(){
        //Merge all Timeboard app events
         TIMEBOARD_EVENTS = TIMEBOARD_EVENTS.mergeWith(TASK_EVENTS);
         TIMEBOARD_EVENTS = TIMEBOARD_EVENTS.mergeWith(TIMESHEET_EVENTS);
    }

    //TODO keep this subject ?
    public static PublishSubject<Map<User, String>> GENERATE_PASSWORD = PublishSubject.create();

}
