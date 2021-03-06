package timeboard.core.internal.reports;

/*-
 * #%L
 * core
 * %%
 * Copyright (C) 2019 - 2020 Timeboard
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

import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import timeboard.core.api.AccountService;
import timeboard.core.api.OrganizationService;
import timeboard.core.api.ReportService;
import timeboard.core.model.Account;
import timeboard.core.model.Organization;
import timeboard.core.model.Report;
import timeboard.core.security.TimeboardAuthentication;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Optional;

@Component
@PersistJobDataAfterExecution
@DisallowConcurrentExecution
public final class ReportJob implements Job {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReportJob.class);

    @Autowired
    private ReportService reportService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private OrganizationService organizationService;

    @Override
    public void execute(final JobExecutionContext context) throws JobExecutionException {

        final Long reportID = context.getMergedJobDataMap().getLong("reportID");
        final Long actorID = context.getMergedJobDataMap().getLong("actorID");
        final long orgID = context.getMergedJobDataMap().getLong("orgID");

        final Account actor = this.accountService.findUserByID(actorID);

        final TimeboardAuthentication authentication = new TimeboardAuthentication(actor);
        final SecurityContext securityContext = new LocalSecurityContext(authentication);
        SecurityContextHolder.setContext(securityContext);

        final Optional<Organization> org = this.organizationService.getOrganizationByID(actor, orgID);
        authentication.setCurrentOrganization(org.get());

        final Report report = this.reportService.getReportByID(actor, reportID);
        final Optional<ReportHandler> reportHandler = this.reportService.getReportHandler(report);

        if (reportHandler.isPresent()) {
            final Serializable data = reportHandler.get().getReportModel(authentication, report);
            report.setLastAsyncJobTrigger(Calendar.getInstance());
            report.setData(data);
            this.reportService.updateReport(actor, report);
            LOGGER.info("Report " + reportID + " finished with " + data.toString());
        } else {
            context.setResult("Mission report handler : " + report.getHandlerID());
        }


    }

}
