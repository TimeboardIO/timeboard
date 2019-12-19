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

import timeboard.core.internal.ReportServiceImpl;
import timeboard.core.model.Account;
import timeboard.core.model.Report;
import timeboard.core.model.ReportType;

import java.util.List;
import java.util.Set;

public interface ReportService {

    String ORIGIN_TIMEBOARD = "timeboard";

    Report createReport(Account owner, String reportName, Account organization, ReportType type, String filterProject);

    List<Report> listReports(Account owner);

    Report updateReport(Account owner, Report report);

    Report getReportByID(Account actor, Long reportId);

    void deleteReportByID(Account actor, Long reportId);

    Set<ProjectWrapper> findProjects(Account actor, List<String> expressions);


    class ProjectWrapper {

        private final Long projectID;
        private final String projectName;
        private final String projectComments;
        private final List<TagWrapper> projectTags;

        public ProjectWrapper(Long projectID, String projectName, String projectComments, List<TagWrapper> projectTags) {
            this.projectID = projectID;
            this.projectName = projectName;
            this.projectComments = projectComments;
            this.projectTags = projectTags;
        }

        public Long getProjectID() {
            return projectID;
        }

        public String getProjectName() {
            return projectName;
        }

        public String getProjectComments() {
            return projectComments;
        }

        public List<TagWrapper> getProjectTags() {
            return projectTags;
        }
    }

    class TagWrapper {

        private final String tagKey;
        private final String tagValue;

        public TagWrapper(String tagKey, String tagValue) {
            this.tagKey = tagKey;
            this.tagValue = tagValue;
        }

        public String getTagKey() {
            return tagKey;
        }

        public String getTagValue() {
            return tagValue;
        }
    }
}
