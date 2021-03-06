package timeboard.core.internal.reports.handlers;

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
import org.springframework.stereotype.Component;
import org.springframework.ui.ConcurrentModel;
import org.springframework.ui.Model;
import timeboard.core.api.ReportService;
import timeboard.core.internal.reports.ReportHandler;
import timeboard.core.model.Account;
import timeboard.core.model.Report;
import timeboard.core.security.TimeboardAuthentication;

import java.io.Serializable;
import java.util.List;


@Component
public class GlobalRawDataExportController implements ReportHandler {

    @Autowired
    private ReportService reportService;

    @Override
    public Serializable getReportModel(
            final TimeboardAuthentication authentication,
            final Report report) {

        final Model model = new ConcurrentModel();
        final Account actor = authentication.getDetails();

        final List<ReportService.ProjectWrapper> listOfProjectsFiltered = this.reportService
                .findProjects(actor, authentication.getCurrentOrganization(), report);

        model.addAttribute("projets", listOfProjectsFiltered);

        return (Serializable) model.asMap();
    }


    @Override
    public String handlerID() {
        return this.getClass().getSimpleName();
    }

    @Override
    public String handlerLabel() {
        return "report.raw";
    }

    @Override
    public String handlerView() {
        return "global_raw_data_export.html";
    }

    @Override
    public Boolean isAsyncHandler() {
        return true;
    }
}
