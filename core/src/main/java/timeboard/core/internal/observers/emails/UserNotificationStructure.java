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

import timeboard.core.api.events.TimeboardEvent;
import timeboard.core.model.Account;

import java.util.ArrayList;
import java.util.List;


public class UserNotificationStructure {

    private Account targetAccount;
    private List<TimeboardEvent> notificationEventList;
    private List<TimeboardEvent> informEventList;

    public UserNotificationStructure(final Account targetAccount) {
        this.targetAccount = targetAccount;
        notificationEventList = new ArrayList<>();
        informEventList = new ArrayList<>();

    }

    public void notify(final TimeboardEvent event) {
        notificationEventList.add(event);
    }

    public void inform(final TimeboardEvent event) {
        informEventList.add(event);
    }

    public Account getTargetAccount() {
        return targetAccount;
    }

    public List<TimeboardEvent> getNotificationEventList() {
        return notificationEventList;
    }

    public List<TimeboardEvent> getInformEventList() {
        return informEventList;
    }

}
