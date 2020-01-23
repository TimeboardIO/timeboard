package timeboard.core.api.sync;

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

import timeboard.core.model.Task;

import java.util.Date;

public class RemoteTask {

    private String id;
    private String title;
    private String userName;
    private String comments;
    private String origin;
    private Date startDate;
    private Date stopDate;
    private Long localUserID;

    public Task toTask() {
        return null;
    }

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(final String summary) {
        this.title = summary;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(final String userName) {
        this.userName = userName;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(final String o) {
        this.origin = o;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(final Date date) {
        this.startDate = date;
    }

    public Long getLocalUserID() {
        return localUserID;
    }

    public void setLocalUserID(final Long localUserID) {
        this.localUserID = localUserID;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(final String comments) {
        this.comments = comments;
    }

    public Date getStopDate() {
        return stopDate;
    }

    public void setStopDate(final Date stopDate) {
        this.stopDate = stopDate;
    }
}
