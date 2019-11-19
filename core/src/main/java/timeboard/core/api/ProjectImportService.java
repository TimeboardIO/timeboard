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

import timeboard.core.api.exceptions.BusinessException;
import timeboard.core.model.Task;
import timeboard.core.model.TaskRevision;
import timeboard.core.model.User;

import java.util.Date;
import java.util.List;

public interface ProjectImportService {

    String getServiceName();

    List<String> getRequiredUserFields();


    List<RemoteTask> getRemoteTasks(User currentUser, long projectID) throws BusinessException;

    public static class RemoteTask{

        private Long ID;
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

        public Long getID() {
            return ID;
        }

        public void setID(Long id) {
            this.ID = id;
        }

        public void setTitle(String summary) {
            this.title = summary;
        }

        public String getTitle() {
            return title;
        }

        public String getUserName() {
            return userName;
        }

        public void setUserName(String userName) {
            this.userName = userName;
        }

        public void setOrigin(String o) {
            this.origin = o;
        }

        public void setStartDate(Date date) {
            this.startDate = date;
        }

        public String getOrigin() {
            return origin;
        }

        public Date getStartDate() {
            return startDate;
        }

        public Long getLocalUserID() {
            return localUserID;
        }

        public void setLocalUserID(Long localUserID) {
            this.localUserID = localUserID;
        }

        public String getComments() {
            return comments;
        }

        public void setComments(String comments) {
            this.comments = comments;
        }

        public Date getStopDate() {
            return stopDate;
        }

        public void setStopDate(Date stopDate) {
            this.stopDate = stopDate;
        }
    }
}
