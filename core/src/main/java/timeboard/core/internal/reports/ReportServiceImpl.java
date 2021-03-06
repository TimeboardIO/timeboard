package timeboard.core.internal.reports;

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

import edu.emory.mathcs.backport.java.util.Collections;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.stereotype.Component;
import timeboard.core.api.ProjectService;
import timeboard.core.api.ReportService;
import timeboard.core.model.Account;
import timeboard.core.model.Organization;
import timeboard.core.model.Project;
import timeboard.core.model.Report;
import timeboard.core.security.TimeboardAuthentication;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Component
@Transactional
public class ReportServiceImpl implements ReportService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReportServiceImpl.class);

    @Autowired
    private EntityManager em;

    @Autowired
    private ProjectService projectService;

    @Autowired(required = false)
    private List<ReportHandler> reportHandlers;

    @Autowired
    private Scheduler scheduler;

    @PostConstruct
    public void initQuartzJobsForHandlers() {
        this.reportHandlers
                .stream()
                .forEach(reportHandler -> {
                    try {
                        final JobDetail details = JobBuilder.newJob()
                                .withIdentity(reportHandler.handlerJobJey())
                                .ofType(ReportJob.class)
                                .requestRecovery(false)
                                .storeDurably(true)
                                .build();
                        this.scheduler.addJob(details, true);
                    } catch (SchedulerException e) {
                        LOGGER.error(e.getMessage());
                    }
                });

    }

    @Override
    @Transactional
    public Report createReport(final Organization org, final Account owner, final String reportName,
                               final String handlerID, final String filterProject) throws SchedulerException {


        final Report newReport = new Report();
        newReport.setName(reportName);
        newReport.setHandlerID(handlerID);
        newReport.setFilterProject(filterProject);

        final Optional<ReportHandler> reportHandler = this.getReportHandler(newReport);
        if (reportHandler.isPresent()) {
            newReport.setHandlerID(reportHandler.get().handlerID());
            em.persist(newReport);

            if (reportHandler.get().isAsyncHandler()) {

                final JobDataMap jobDataMap = new JobDataMap();
                jobDataMap.put("reportID", newReport.getId());
                jobDataMap.put("actorID", owner.getId());
                jobDataMap.put("orgID", org.getId());


                final Trigger trigger = TriggerBuilder.newTrigger()
                        .withIdentity("myTrigger")
                        .forJob(reportHandler.get().handlerJobJey())
                        .withSchedule(CronScheduleBuilder.cronSchedule("0 0/5 * 1/1 * ? *"))
                        .usingJobData(jobDataMap)
                        .build();

                this.scheduler.scheduleJob(trigger);

                newReport.setAsyncTriggerKeyName(trigger.getKey().getName());
            }
            LOGGER.info("Report " + reportName + " created by user " + owner.getId());
        }


        return newReport;
    }

    @Override
    public List<Report> listReports(final Organization org, final Account owner) {
        final TypedQuery<Report> query = em.createQuery(
                "select r from Report r where r.organizationID = :orgID",
                Report.class);
        query.setParameter("orgID", org.getId());

        return query.getResultList();
    }

    @Override
    public Report updateReport(final Account owner, final Report report) {
        em.merge(report);
        em.flush();
        return report;
    }

    @Override
    public Report getReportByID(final Account actor, final Long reportId) {
        final Report data = em.createQuery("select r from Report r where r.id = :reportId", Report.class)
                .setParameter("reportId", reportId)
                .getSingleResult();
        return data;
    }

    @Override
    public void deleteReportByID(final Account actor, final Long reportId) throws SchedulerException {
        final Report report = em.find(Report.class, reportId);

        if (report.getAsyncTriggerKeyName() != null) {
            final TriggerKey triggerKey = TriggerKey.triggerKey(report.getAsyncTriggerKeyName());

            if (null != this.scheduler.getTrigger(triggerKey)) {
                this.scheduler.unscheduleJob(triggerKey);
            }
        }
        em.remove(report);
        em.flush();

        LOGGER.info("Report " + reportId + " deleted by " + actor.getName());
    }

    @Override
    public List<ProjectWrapper> findProjects(final Account actor, final Organization org, final List<String> expressions) {

        final ExpressionParser expressionParser = new SpelExpressionParser();

        final List<Expression> spelExp = expressions
                .stream().map(expressionParser::parseExpression)
                .collect(Collectors.toList());

        return this.projectService.listProjects(actor, org)
                .stream()
                .map(this::wrapProjectTags)
                .filter(projectWrapper -> spelExp.stream()
                        .allMatch(exp -> applyFilterOnProject(exp, projectWrapper))).collect(Collectors.toList());
    }

    @Override
    public List<ProjectWrapper> findProjects(final Account actor, final Organization org, final Report report) {
        return this.findProjects(actor, org, Arrays.asList(report.getFilterProject().split("\n")));
    }

    @Override
    public Optional<ReportHandler> getReportHandler(final Report report) {
        return this.reportHandlers.stream()
                .filter(rc ->
                {
                    return rc.handlerID().equals(report.getHandlerID());
                }).findFirst();
    }

    @Override
    public List<ReportHandler> listReportHandlers() {
        return Collections.unmodifiableList(this.reportHandlers);
    }

    @Override
    public void executeAsyncReport(final TimeboardAuthentication auth, final Report report) throws SchedulerException {

        final Optional<ReportHandler> handler = this.getReportHandler(report);
        if (handler.isPresent()) {
            final JobDataMap jobDataMap = new JobDataMap();
            jobDataMap.put("reportID", report.getId());
            jobDataMap.put("actorID", auth.getDetails().getId());
            jobDataMap.put("orgID", auth.getCurrentOrganization().getId());

            this.scheduler.triggerJob(handler.get().handlerJobJey(), jobDataMap);

        }

    }


    private boolean applyFilterOnProject(final Expression exp, final ProjectWrapper projectWrapper) {
        return projectWrapper.getProjectTags()
                .stream()
                .map(tagWrapper -> exp.getValue(tagWrapper, Boolean.class))
                .anyMatch(aBoolean -> aBoolean == true);
    }

    private ProjectWrapper wrapProjectTags(final Project project) {
        return new ProjectWrapper(project);
    }


}
