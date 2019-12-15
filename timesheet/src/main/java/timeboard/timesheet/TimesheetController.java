package timeboard.timesheet;

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
import timeboard.core.api.ProjectService;
import timeboard.core.api.ProjectTasks;
import timeboard.core.api.TimesheetService;
import timeboard.core.api.UpdatedTaskResult;
import timeboard.core.model.AbstractTask;
import timeboard.core.model.Account;
import timeboard.core.model.Task;
import timeboard.core.ui.UserInfo;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;



@Controller
@RequestMapping("/timesheet")
public class TimesheetController {

    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Autowired
    private  ProjectService projectService;

    @Autowired
    private  TimesheetService timesheetService;

    @Autowired
    private UserInfo userInfo;

    private int findLastWeekYear(Calendar c, int week, int year) {
        c.set(Calendar.YEAR, year);
        c.set(Calendar.WEEK_OF_YEAR, week);
        c.roll(Calendar.WEEK_OF_YEAR, -1); // remove 1 week
        return c.get(Calendar.YEAR);
    }

    private int findLastWeek(Calendar c, int week, int year) {
        c.set(Calendar.YEAR, year);
        c.set(Calendar.WEEK_OF_YEAR, week);
        c.roll(Calendar.WEEK_OF_YEAR, -1); // remove 1 week
        return c.get(Calendar.WEEK_OF_YEAR);
    }

    private Date findStartDate(Calendar c, int week, int year) {
        c.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        return c.getTime();
    }

    private Date findEndDate(Calendar c, int week, int year) {
        c.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        return c.getTime();
    }

    @GetMapping
    protected String currentWeekTimesheet(Model model) throws Exception {
        Calendar c = Calendar.getInstance();
        return this.handleGet(c.get(Calendar.YEAR), c.get(Calendar.WEEK_OF_YEAR), model);
    }

    @GetMapping("/{year}/{week}")
    protected String handleGet(@PathVariable("year") int year, @PathVariable("week") int week, Model model) throws Exception {
        final List<ProjectTasks> tasksByProject = new ArrayList<>();

        final Calendar c = Calendar.getInstance();
        c.set(Calendar.WEEK_OF_YEAR, week);
        c.set(Calendar.YEAR, year);
        c.setFirstDayOfWeek(Calendar.MONDAY);
        c.set(Calendar.HOUR_OF_DAY, 2);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);

        final Date ds = findStartDate(c, week, year);
        final Date de = findEndDate(c, week, year);
        final int lastWeek = findLastWeek(c, week, year);
        final int lastWeekYear = findLastWeekYear(c, week, year);
        final boolean lastWeekValidated = this.timesheetService.isTimesheetValidated(this.userInfo.getCurrentAccount(), lastWeekYear, lastWeek);

        model.addAttribute("week", week);
        model.addAttribute("year", year);
        model.addAttribute("lastWeekValidated", lastWeekValidated);

        model.addAttribute("taskTypes", this.projectService.listTaskType());
        model.addAttribute("projectList", this.projectService.listProjects(this.userInfo.getCurrentAccount()));

        return "timesheet.html";
    }

    @PostMapping
    protected void doPost(HttpServletRequest request, HttpServletResponse response) {

        try {
            final Account actor = this.userInfo.getCurrentAccount();
            String type = request.getParameter("type");
            Long taskID = Long.parseLong(request.getParameter("task"));
            AbstractTask task = this.projectService.getTaskByID(actor, taskID);

            UpdatedTaskResult updatedTask = null;

            if (type.equals("imputation")) {
                Date day = DATE_FORMAT.parse(request.getParameter("day"));
                double imputation = Double.parseDouble(request.getParameter("imputation"));
                updatedTask = this.projectService.updateTaskImputation(actor, task, day, imputation);
            }

            if (type.equals("effortLeft")) {
                double effortLeft = Double.parseDouble(request.getParameter("imputation"));
                updatedTask = this.projectService.updateTaskEffortLeft(actor, (Task) task, effortLeft);
            }


            response.setContentType("application/json");
            MAPPER.writeValue(response.getWriter(), updatedTask);

        } catch (Exception e) {
            response.setStatus(500);
        }

    }


    private class DateWrapper {

        private String day;
        private Date date;

        public String getDay() {
            return day;
        }

        public void setDay(String day) {
            this.day = day;
        }

        public Date getDate() {
            return date;
        }

        public void setDate(Date date) {
            this.date = date;
        }
    }
}
