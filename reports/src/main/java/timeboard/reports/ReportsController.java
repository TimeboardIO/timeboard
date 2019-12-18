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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import timeboard.core.api.ProjectService;
import timeboard.core.api.ReportService;
import timeboard.core.api.UserService;
import timeboard.core.api.exceptions.BusinessException;
import timeboard.core.model.*;
import timeboard.core.ui.UserInfo;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@Controller
@RequestMapping("/reports")
public class ReportsController {

    @Autowired
    private ReportService reportService;

    @Autowired
    private UserService userService;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private UserInfo userInfo;

    @GetMapping
    protected String handleGet(HttpServletRequest request, HttpServletResponse response, Model model) throws Exception {
        return "reports.html";
   }

    @GetMapping(value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
    protected ResponseEntity<List<ReportDecorator>> reportList(Model model) {
        final Account actor = this.userInfo.getCurrentAccount();
        final List<ReportDecorator> reports = this.reportService.listReports(actor)
                .stream()
                .map(report -> new ReportDecorator(report))
                .collect(Collectors.toList());
        return  ResponseEntity.ok(reports);
    }

    @GetMapping("/create")
    protected String createReport(Model model) throws ServletException, IOException {
        model.addAttribute("allReportTypes", ReportType.values());
        return "create_report.html";
    }

    @PostMapping("/create")
    protected String handlePost(HttpServletRequest request, HttpServletResponse response, Model model) throws ServletException, IOException, BusinessException {
        final Account actor = this.userInfo.getCurrentAccount();
        Long organizationID = userInfo.getCurrentOrganizationID();
        Account organization = userService.findUserByID(organizationID);

        ExpressionParser expressionParser = new SpelExpressionParser();
        //TODO To fix it
        //Expression expression = expressionParser.parseExpression(filterProjects);
        //TODO To delete
        Expression expression = expressionParser.parseExpression("tagKey == \"CUSTOMER\" && (tagValue == \"Demo\" || tagValue == \"Test\")");
        List<Project> listProjectsConcerned = this.projectService.listProjects(organization)
                .stream()
                .filter(p -> p.getTags()
                        .stream()
                        .map(t -> expression.getValue(t, Boolean.class) != null ? expression.getValue(t, Boolean.class) : Boolean.FALSE)
                        .reduce(false, (aBoolean, aBoolean2) -> aBoolean || aBoolean2)
                ).collect(Collectors.toList());

        this.reportService.createReport(
                actor,
                request.getParameter("reportName"),
                organization,
                listProjectsConcerned,
                ReportType.valueOf(request.getParameter("reportType"))
        );
        return "redirect:/reports";
    }

    @GetMapping("/delete/{reportID}")
    protected String deleteReport(@PathVariable long reportID) throws ServletException, IOException, BusinessException {
        this.reportService.deleteReportByID(this.userInfo.getCurrentAccount(), reportID);
        return "redirect:/reports";
    }



    private class ReportDecorator {

        private final Report report;

        public ReportDecorator(Report report) {
            this.report = report;
        }

        public long getID(){
            return this.report.getId();
        }

        public String getName(){
            return this.report.getName();
        }

    }

}
