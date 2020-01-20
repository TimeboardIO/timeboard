package timeboard.core.api.events;

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

import timeboard.core.model.*;

import java.util.Date;

import static timeboard.core.api.events.TimeboardEventType.*;


public class VacationEvent extends TimeboardEvent {
    private VacationRequest request;
    private TimeboardEventType eventType;

    public VacationEvent(TimeboardEventType eventType, VacationRequest request) {
        
        super(new Date());
        this.eventType = eventType;
        this.request = request;

        this.constructUsersList();
    }

    public TimeboardEventType getEventType() {
        return eventType;
    }

    public void setEventType(TimeboardEventType eventType) {
        this.eventType = eventType;
    }

    public VacationRequest getRequest() {
        return request;
    }

    public void setRequest(VacationRequest request) {
        this.request = request;
    }

    private void constructUsersList() {
        Account applicantAccount = request.getApplicant();
        Account assignedAccount = request.getAssignee();

       if(eventType == CREATE || eventType == DELETE) {
           usersToNotify.add(assignedAccount);
           usersToInform.add(applicantAccount);
       } else {
           usersToNotify.add(applicantAccount);
           usersToInform.add(assignedAccount);
       }

    }

}
