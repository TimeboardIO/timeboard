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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import timeboard.core.api.ProjectService;
import timeboard.core.api.ReportService;
import timeboard.core.api.ThreadLocalStorage;
import timeboard.core.api.UserService;
import timeboard.core.model.Account;
import timeboard.core.model.Report;
import timeboard.core.model.ReportType;
import timeboard.core.security.TimeboardAuthentication;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.Arrays;
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


    @GetMapping
    protected String handleGet() {
        return "reports.html";
    }

    @GetMapping(value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
    protected ResponseEntity<List<ReportDecorator>> reportList(TimeboardAuthentication authentication, Model model) {
        final Account actor = authentication.getDetails();
        final List<ReportDecorator> reports = this.reportService.listReports(actor)
                .stream()
                .map(report -> new ReportDecorator(report))
                .collect(Collectors.toList());
        return ResponseEntity.ok(reports);
    }

    @GetMapping("/create")
    protected String createReport(Model model) throws ServletException, IOException {
        model.addAttribute("allReportTypes", ReportType.values());
        model.addAttribute("report", new Report());
        model.addAttribute("action", "create");


        return "create_report.html";
    }

    @PostMapping("/create")
    protected String handlePost(TimeboardAuthentication authentication,
                                @ModelAttribute Report report, RedirectAttributes attributes) {

        final Account actor = authentication.getDetails();
        Long organizationID = ThreadLocalStorage.getCurrentOrgId();
        Account organization = userService.findUserByID(organizationID);

        String projectFilter = report.getFilterProject();

        this.reportService.createReport(
                actor,
                report.getName(),
                organization,
                report.getType(),
                projectFilter
        );
        attributes.addFlashAttribute("success", "Report created successfully.");

        return "redirect:/reports";
    }

    @GetMapping("/delete/{reportID}")
    protected String deleteReport(final TimeboardAuthentication authentication,
                                  @PathVariable long reportID,
                                  RedirectAttributes attributes) {

        this.reportService.deleteReportByID(authentication.getDetails(), reportID);

        attributes.addFlashAttribute("success", "Report deleted successfully.");

        return "redirect:/reports";
    }

    @GetMapping("/edit/{reportID}")
    protected String editReport(final TimeboardAuthentication authentication,
                                @PathVariable long reportID, Model model) {
        model.addAttribute("allReportTypes", ReportType.values());
        model.addAttribute("reportID", reportID);
        model.addAttribute("action", "edit");
        model.addAttribute("report", this.reportService.getReportByID(authentication.getDetails(), reportID));
        return "create_report.html";
    }

    @PostMapping("/edit/{reportID}")
    protected String handlePost(final TimeboardAuthentication authentication,
                                @PathVariable long reportID,
                                @ModelAttribute Report report, RedirectAttributes attributes) {

        final Account actor = authentication.getDetails();
        Long organizationID = ThreadLocalStorage.getCurrentOrgId();
        Account organization = userService.findUserByID(organizationID);

        Report updatedReport = this.reportService.getReportByID(organization, reportID);
        updatedReport.setName(report.getName());
        updatedReport.setType(ReportType.PROJECT_KPI);
        updatedReport.setFilterProject(report.getFilterProject());

        this.reportService.updateReport(actor, updatedReport);
        attributes.addFlashAttribute("success", "Report updated successfully.");

        return "redirect:/reports";
    }

    @PostMapping(value = "/refreshProjectSelection", produces = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity refreshProjectSelection(final TimeboardAuthentication authentication,
                                                  @RequestBody MultiValueMap<String, String> filterProjectsMap)
            throws JsonProcessingException {
        final Account actor = authentication.getDetails();

        String filterProjects = filterProjectsMap.getFirst("filter");

        // If there is no filter, don't display all the projects
        if (filterProjects == null || filterProjects.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Impossible to display the projects. Give a filter.");
        }

        final String[] filters = filterProjects.split("\n");
        final List<ReportService.ProjectWrapper> projects = this.reportService
                .findProjects(actor, authentication.getCurrentOrganization(), Arrays.asList(filters));

        return ResponseEntity.status(HttpStatus.OK).body(MAPPER.writeValueAsString(projects));
    }

    @GetMapping("/view/{reportID}")
    protected String viewReport(final TimeboardAuthentication authentication,
                                @PathVariable long reportID, Model model) {
        model.addAttribute("reportID", reportID);
        ReportType type = this.reportService.getReportByID(authentication.getDetails(), reportID).getType();
        model.addAttribute("reportType", type);
        model.addAttribute("report", this.reportService.getReportByID(authentication.getDetails(), reportID));

        switch (type) {
            case PROJECT_KPI:
                return "view_report_kpi.html";
            default:
                return "";
        }
    }


    private class ReportDecorator {

        private final Report report;

        public ReportDecorator(Report report) {
            this.report = report;
        }

        public long getID() {
            return this.report.getId();
        }

        public String getName() {
            return this.report.getName();
        }

    }

}
