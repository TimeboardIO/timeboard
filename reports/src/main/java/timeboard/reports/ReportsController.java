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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import timeboard.core.api.ReportService;
import timeboard.core.model.Account;
import timeboard.core.model.Report;
import timeboard.core.ui.UserInfo;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.stream.Collectors;


@Controller
@RequestMapping("/reports")
public class ReportsController {

    @Autowired
    private ReportService reportServices;

    @Autowired
    private UserInfo userInfo;

    @GetMapping
    protected String handleGet(HttpServletRequest request, HttpServletResponse response, Model model) throws Exception {
        return "reports.html";
   }

    @GetMapping(value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
    protected ResponseEntity<List<ReportDecorator>> reportList(Model model) {
        final Account actor = this.userInfo.getCurrentAccount();
        final List<ReportDecorator> reports = this.reportServices.listReports(actor)
                .stream()
                .map(report -> new ReportDecorator(report))
                .collect(Collectors.toList());
        return  ResponseEntity.ok(reports);
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
