package timeboard.organization;

/*-
 * #%L
 * kanban-project-plugin
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
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import timeboard.core.api.OrganizationService;
import timeboard.core.api.ProjectService;
import timeboard.core.api.TimesheetService;
import timeboard.core.api.UpdatedTaskResult;
import timeboard.core.model.AbstractTask;
import timeboard.core.model.Account;
import timeboard.core.model.Task;
import timeboard.core.security.TimeboardAuthentication;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


@Controller
@RequestMapping("/timesheet")
public class TimesheetController {

    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Autowired
    private ProjectService projectService;

    @Autowired
    private TimesheetService timesheetService;

    @Autowired
    private OrganizationService organizationService;

    @GetMapping
    protected String currentWeekTimesheet(final TimeboardAuthentication authentication, final Model model) throws Exception {
        final Calendar c = Calendar.getInstance();
        return this.fillAndDisplayTimesheetPage(authentication, c.get(Calendar.YEAR), c.get(Calendar.WEEK_OF_YEAR), model);
    }

    @GetMapping("/{year}/{week}")
    protected String fillAndDisplayTimesheetPage(
            final TimeboardAuthentication authentication,
            @PathVariable("year") final int year,
            @PathVariable("week") final int week,
            final Model model) throws Exception {



        final Calendar beginWorkDateForCurrentOrg = this.organizationService
                .findOrganizationMembership(authentication.getDetails(), authentication.getCurrentOrganization())
                .get().getCreationDate();

        final Calendar c = beginWorkDateForCurrentOrg;


        c.set(Calendar.WEEK_OF_YEAR, week);
        c.set(Calendar.YEAR, year);
        c.set(Calendar.HOUR_OF_DAY, 2);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);

        final int lastWeek = this.findLastWeek(c, week, year);
        final int lastWeekYear = this.findLastWeekYear(c, week, year);

        model.addAttribute("week", week);
        model.addAttribute("year", year);
        model.addAttribute("actorID", authentication.getDetails().getId());
        model.addAttribute("lastWeekSubmitted", this.timesheetService.isTimesheetSubmitted(authentication.getDetails(), lastWeekYear, lastWeek));
        model.addAttribute("taskTypes", this.organizationService.listTaskType(authentication.getCurrentOrganization()));

        model.addAttribute("projectList",
                this.projectService.listProjects(
                        authentication.getDetails(), authentication.getCurrentOrganization()));

        return "timesheet.html";
    }

    @PostMapping
    protected void doPost(final TimeboardAuthentication authentication,
                          final HttpServletRequest request, final HttpServletResponse response) {

        try {
            final Account actor = authentication.getDetails();
            final String type = request.getParameter("type");
            final Long taskID = Long.parseLong(request.getParameter("task"));
            final AbstractTask task = this.projectService.getTaskByID(actor, taskID);

            UpdatedTaskResult updatedTask = null;

            if (type.equals("imputation")) {
                final Date day = DATE_FORMAT.parse(request.getParameter("day"));
                final double imputation = Double.parseDouble(request.getParameter("imputation"));
                updatedTask = this.projectService.updateTaskImputation(actor, task, day, imputation);
            }

            if (type.equals("effortLeft")) {
                final double effortLeft = Double.parseDouble(request.getParameter("imputation"));
                updatedTask = this.projectService.updateTaskEffortLeft(actor, (Task) task, effortLeft);
            }


            response.setContentType("application/json");
            MAPPER.writeValue(response.getWriter(), updatedTask);

        } catch (final Exception e) {
            response.setStatus(500);
        }

    }

    private int findLastWeekYear(final Calendar c, final int week, final int year) {
        c.set(Calendar.YEAR, year);
        c.set(Calendar.WEEK_OF_YEAR, week);
        c.roll(Calendar.WEEK_OF_YEAR, -1); // remove 1 week
        if (c.get(Calendar.WEEK_OF_YEAR) > week) {
            c.roll(Calendar.YEAR, -1);  // remove one year
        }
        return c.get(Calendar.YEAR);
    }

    private int findLastWeek(final Calendar c, final int week, final int year) {
        c.set(Calendar.YEAR, year);
        c.set(Calendar.WEEK_OF_YEAR, week);
        c.roll(Calendar.WEEK_OF_YEAR, -1); // remove 1 week
        return c.get(Calendar.WEEK_OF_YEAR);
    }

    private class DateWrapper {

        private String day;
        private Date date;

        public String getDay() {
            return day;
        }

        public void setDay(final String day) {
            this.day = day;
        }

        public Date getDate() {
            return date;
        }

        public void setDate(final Date date) {
            this.date = date;
        }
    }
}
