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

import org.quartz.SchedulerException;
import timeboard.core.internal.reports.ReportHandler;
import timeboard.core.model.Account;
import timeboard.core.model.Organization;
import timeboard.core.model.Project;
import timeboard.core.model.Report;
import timeboard.core.security.TimeboardAuthentication;

import java.beans.Transient;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public interface ReportService {

    /**
     * Create a new report configuration
     *
     * @param organizationID relevant organization
     * @param owner          user that owner report
     * @param reportName     screen name used to identify report
     * @param handlerName    name of report handler used to compute report data
     * @param filterProject  SPEL
     * @return
     * @throws SchedulerException
     */
    Report createReport(
            final Organization organizationID,
            final Account owner,
            final String reportName,
            final String handlerName,
            final String filterProject) throws SchedulerException;

    /**
     * List all report for owner
     *
     * @param org   primary key {@link Organization} where looking for reports
     * @param owner an account that own reports
     * @return
     */
    List<Report> listReports(final Organization org, final Account owner);

    Report updateReport(Account actor, Report report);

    Report getReportByID(Account actor, Long reportId);

    void deleteReportByID(Account actor, Long reportId) throws SchedulerException;

    List<ProjectWrapper> findProjects(Account actor, Organization org, List<String> expressions);

    List<ProjectWrapper> findProjects(Account actor, Organization org, Report report);

    Optional<ReportHandler> getReportHandler(Report report);

    List<ReportHandler> listReportHandlers();

    void executeAsyncReport(TimeboardAuthentication auth, Report report) throws SchedulerException;


    class ProjectWrapper {

        private final List<TagWrapper> projectTags;
        private final Project project;

        public ProjectWrapper(final Project project) {
            this.project = project;
            this.projectTags = this.project.getTags()
                    .stream()
                    .map(t -> new TagWrapper(t.getTagKey(), t.getTagValue()))
                    .collect(Collectors.toList());
        }

        public Long getProjectID() {
            return this.project.getId();
        }

        public String getProjectName() {
            return this.project.getName();
        }

        public String getProjectComments() {
            return this.project.getComments();
        }

        public List<TagWrapper> getProjectTags() {
            return projectTags;
        }

        @Transient
        public Project getProject() {
            return this.project;
        }
    }

    class TagWrapper {

        private final String tagKey;
        private final String tagValue;

        public TagWrapper(final String tagKey, final String tagValue) {
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
