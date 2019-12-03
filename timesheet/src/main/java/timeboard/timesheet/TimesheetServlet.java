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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import javax.servlet.Servlet;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.checkerframework.checker.units.qual.C;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import timeboard.core.api.ProjectService;
import timeboard.core.api.ProjectTasks;
import timeboard.core.api.TimesheetService;
import timeboard.core.api.UpdatedTaskResult;
import timeboard.core.model.AbstractTask;
import timeboard.core.model.Task;
import timeboard.core.model.User;
import timeboard.core.ui.TimeboardServlet;
import timeboard.core.ui.ViewModel;



@WebServlet(name = "TimesheetServlet", urlPatterns = "/timesheet")
public class TimesheetServlet extends TimeboardServlet {

    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Autowired
    private  ProjectService projectService;

    @Autowired
    private  TimesheetService timesheetService;

    @Override
    protected ClassLoader getTemplateResolutionClassLoader() {
        return TimesheetServlet.class.getClassLoader();
    }


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

    @Override
    protected void handleGet(User actor, HttpServletRequest request, HttpServletResponse response, ViewModel viewModel) throws Exception {
        final List<ProjectTasks> tasksByProject = new ArrayList<>();
        final int week = Integer.parseInt(request.getParameter("week"));
        final int year = Integer.parseInt(request.getParameter("year"));

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
        final boolean lastWeekValidated = this.timesheetService.isTimesheetValidated(getActorFromRequestAttributes(request), lastWeekYear, lastWeek);

        viewModel.getViewDatas().put("week", week);
        viewModel.getViewDatas().put("year", year);
        viewModel.getViewDatas().put("lastWeekValidated", lastWeekValidated);
        viewModel.getViewDatas().put("userID", actor.getId());


        viewModel.getViewDatas().put("taskTypes", this.projectService.listTaskType());
        viewModel.getViewDatas().put("projectList", this.projectService.listProjects(getActorFromRequestAttributes(request)));


        viewModel.setTemplate("timesheet.html");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) {

        try {
            final User actor = getActorFromRequestAttributes(request);
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
