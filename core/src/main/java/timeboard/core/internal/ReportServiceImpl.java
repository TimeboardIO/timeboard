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

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.stereotype.Component;
import timeboard.core.api.*;
import timeboard.core.model.*;


@Component
@Transactional
public class ReportServiceImpl implements ReportService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReportServiceImpl.class);

    @Autowired
    private EntityManager em;

    @Autowired
    private ProjectService projectService;

    @Override
    @Transactional
    public Report createReport(Account owner, String reportName, Account organization,
                               ReportType type, String filterProject) {
        Account ownerAccount = this.em.find(Account.class, owner.getId());
        Report newReport = new Report();
        newReport.setName(reportName);
        newReport.setType(type);
        newReport.setFilterProject(filterProject);
        em.persist(newReport);

        LOGGER.info("Report " + reportName + " created by user " + owner.getId());
        return newReport;
    }

    @Override
    public List<Report> listReports(Account owner) {
        TypedQuery<Report> query = em.createQuery("select r from Report r", Report.class);
        return query.getResultList();
    }

    @Override
    public Report updateReport(Account owner, Report report) {
        em.merge(report);
        em.flush();
        return report;
    }

    @Override
    public Report getReportByID(Account actor, Long reportId) {
        Report data = em.createQuery("select r from Report r where r.id = :reportId", Report.class)
                .setParameter("reportId", reportId)
                .getSingleResult();
        return data;
    }

    @Override
    public void deleteReportByID(Account actor, Long reportId) {
        Report report = em.find(Report.class, reportId);
        em.remove(report);
        em.flush();

        LOGGER.info("Report " + reportId + " deleted by " + actor.getName());
    }

    @Override
    public List<ProjectWrapper> findProjects(Account actor, List<String> expressions){

        final ExpressionParser expressionParser = new SpelExpressionParser();

        final List<Expression> spelExp = expressions
                .stream().map(filter -> expressionParser.parseExpression(filter))
                .collect(Collectors.toList());

        final List<ProjectWrapper> listProjectsConcerned = this.projectService.listProjects(actor)
                .stream()
                .map(project -> wrapProjectTags(project))
                .filter(projectWrapper -> {

                    final Boolean match = spelExp.stream()
                            .map(exp -> applyFilterOnProject(exp, projectWrapper))
                            .allMatch(aBoolean -> aBoolean == true);

                    return match;

                }).collect(Collectors.toList());

        return listProjectsConcerned;

    }

    @Override
    public List<ProjectWrapper> findProjects(Account actor, Report report) {
        return this.findProjects(actor, Arrays.asList(report.getFilterProject().split("\n")));
    }

    private boolean applyFilterOnProject(final Expression exp, final ProjectWrapper projectWrapper) {
        return projectWrapper.getProjectTags()
                .stream()
                .map(tagWrapper -> exp.getValue(tagWrapper, Boolean.class))
                .anyMatch(aBoolean -> aBoolean == true);
    }

    private ProjectWrapper wrapProjectTags(Project project) {
        List<TagWrapper> tags = project.getTags()
                .stream()
                .map(tag -> new TagWrapper(tag.getTagKey(), tag.getTagValue()))
                .collect(Collectors.toList());
        return new ProjectWrapper(project);
    }


}
