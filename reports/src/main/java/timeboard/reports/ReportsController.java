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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
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
import java.util.List;
import java.util.stream.Collectors;


@Controller
@RequestMapping("/reports")
public class ReportsController {

    private static final ObjectMapper MAPPER = new ObjectMapper();

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
        model.addAttribute("report", new Report());
        model.addAttribute("action", "create");

        return "create_report.html";
    }

    @PostMapping("/create")
    protected String handlePost(@ModelAttribute Report report){
        final Account actor = this.userInfo.getCurrentAccount();
        Long organizationID = userInfo.getCurrentOrganizationID();
        Account organization = userService.findUserByID(organizationID);

        String projectFilter = report.getFilterProject();

        this.reportService.createReport(
                actor,
                report.getName(),
                organization,
                report.getType(),
                projectFilter
        );
        return "redirect:/reports";
    }

    @GetMapping("/delete/{reportID}")
    protected String deleteReport(@PathVariable long reportID) throws ServletException, IOException, BusinessException {
        this.reportService.deleteReportByID(this.userInfo.getCurrentAccount(), reportID);
        return "redirect:/reports";
    }

    @GetMapping("/edit/{reportID}")
    protected String editReport(@PathVariable long reportID, Model model) throws ServletException, IOException {
        model.addAttribute("allReportTypes", ReportType.values());
        model.addAttribute("reportID", reportID);
        model.addAttribute("action", "edit");
        model.addAttribute("report", this.reportService.getReportByID(this.userInfo.getCurrentAccount(), reportID));
        return "create_report.html";
    }

    @PostMapping("/edit/{reportID}")
    protected String handlePost(@PathVariable long reportID, @ModelAttribute  Report report) {
        final Account actor = this.userInfo.getCurrentAccount();
        Long organizationID = userInfo.getCurrentOrganizationID();
        Account organization = userService.findUserByID(organizationID);

        Report updatedReport = this.reportService.getReportByID(organization, reportID);
        updatedReport.setName(report.getName());
        updatedReport.setType(ReportType.PROJECT_KPI);
        updatedReport.setFilterProject(report.getFilterProject());

        this.reportService.updateReport(actor, updatedReport);

        return "redirect:/reports";
    }

    @GetMapping("/view/{reportID}")
    protected String viewReport(@PathVariable long reportID, Model model) throws ServletException, IOException {
        model.addAttribute("reportID", reportID);
        model.addAttribute("reportType", this.reportService.getReportByID(this.userInfo.getCurrentAccount(), reportID).getType());
        model.addAttribute("report", this.reportService.getReportByID(this.userInfo.getCurrentAccount(), reportID));
        return "view_report.html";
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
