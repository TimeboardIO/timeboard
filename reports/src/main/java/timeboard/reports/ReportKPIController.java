package timeboard.reports;

/*-
 * #%L
 * reports
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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import timeboard.core.api.ProjectDashboard;
import timeboard.core.api.ProjectService;
import timeboard.core.api.ReportService;
import timeboard.core.api.exceptions.BusinessException;
import timeboard.core.model.Account;
import timeboard.core.model.Project;
import timeboard.core.model.Report;
import timeboard.core.ui.UserInfo;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;


@Controller
@RequestMapping("/data-chart/report-kpi")
public class ReportKPIController {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Autowired
    private ReportService reportService;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private UserInfo userInfo;

    @GetMapping("/{reportID}")
    protected ResponseEntity getDataChart(@PathVariable long reportID, Model model) throws BusinessException, IOException {
        Account actor = this.userInfo.getCurrentAccount();
        Report report = this.reportService.getReportByID(actor, reportID);
        List<Project> allProjects = this.projectService.listProjects(actor);
        List<Project> listOfProjectsFiltered = allProjects;

        if(report.getFilterProject() != null && !report.getFilterProject().equals("")) {
            ExpressionParser expressionParser = new SpelExpressionParser();
            Expression expression = expressionParser.parseExpression(report.getFilterProject());

            listOfProjectsFiltered = allProjects
                    .stream()
                    .filter(p -> p.getTags()
                            .stream()
                            .map(t -> expression.getValue(t, Boolean.class) != null ? expression.getValue(t, Boolean.class) : Boolean.FALSE)
                            .reduce(false, (aBoolean, aBoolean2) -> aBoolean || aBoolean2)
                    ).collect(Collectors.toList());
        }

        AtomicReference<Double> originalEstimate = new AtomicReference<>(0.0);
        AtomicReference<Double> effortLeft = new AtomicReference<>(0.0);
        AtomicReference<Double> effortSpent = new AtomicReference<>(0.0);
        AtomicReference<Double> quotation = new AtomicReference<>(0.0);

        listOfProjectsFiltered.forEach(project -> {
            try {
                ProjectDashboard currentProjectDashboard = this.projectService.projectDashboard(actor, project);
                originalEstimate.updateAndGet(v -> (v + currentProjectDashboard.getOriginalEstimate()));
                effortLeft.updateAndGet(v -> (v + currentProjectDashboard.getEffortLeft()));
                effortSpent.updateAndGet(v -> (v + currentProjectDashboard.getEffortSpent()));
                quotation.updateAndGet(v -> (v + currentProjectDashboard.getQuotation()));
            } catch (BusinessException e) {
                e.printStackTrace();
            }
        });

        final ProjectDashboard dashboardTotal = new ProjectDashboard(quotation.get(), originalEstimate.get(), effortLeft.get(), effortSpent.get());

        return ResponseEntity.status(HttpStatus.OK).body(dashboardTotal);
    }



}
