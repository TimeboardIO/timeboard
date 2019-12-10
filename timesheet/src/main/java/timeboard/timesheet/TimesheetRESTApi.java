package timeboard.timesheet;

/*-
 * #%L
 * reporting
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
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;
import timeboard.core.api.ProjectService;
import timeboard.core.api.TimesheetService;
import timeboard.core.api.UpdatedTaskResult;
import timeboard.core.model.AbstractTask;
import timeboard.core.model.Account;
import timeboard.core.model.Task;
import timeboard.core.model.TaskStatus;
import timeboard.core.ui.UserInfo;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;


@Component
@RestController
@RequestMapping(value = "/api/timesheet", produces = MediaType.APPLICATION_JSON_VALUE)
public class TimesheetRESTApi {

    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Autowired
    private ProjectService projectService;

    @Autowired
    private TimesheetService timesheetService;

    @Autowired
    private UserInfo userInfo;

    @PostConstruct
    private void init() {
        System.out.println("Start Timesheet API !");
    }


    @GetMapping
    public ResponseEntity getTimesheetData(HttpServletRequest request, @RequestParam("week") int week, @RequestParam("year") int year ) throws JsonProcessingException {
        Account currentAccount = this.userInfo.getCurrentAccount();

        final List<ProjectWrapper> projects = new ArrayList<>();
        final List<ImputationWrapper> imputations = new ArrayList<>();


        boolean validated = false;

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

        // Create days for current week
        final List<DateWrapper> days = new ArrayList<>();
        c.setTime(ds); //reset calendar to start date
        for (int i = 0; i < 7; i++) {
            DateWrapper dw = new DateWrapper(
                    c.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.ENGLISH),
                    c.getTime()
            );
            days.add(dw);
            c.add(Calendar.DAY_OF_YEAR, 1);
        }

        //Get tasks for current week
        if (this.projectService != null) {
            this.projectService.listTasksByProject(currentAccount, ds, de).stream().forEach(projectTasks -> {
                List<TaskWrapper> tasks = new ArrayList<>();

                projectTasks.getTasks().stream().forEach(task -> {
                    tasks.add(new TaskWrapper(
                            task.getId(),
                            task.getName(),
                            task.getComments(),
                            task.getEffortSpent(),
                            task.getEffortLeft(),
                            task.getOriginalEstimate(),
                            task.getRealEffort(),
                            task.getStartDate(),
                            task.getEndDate(),
                            task.getTaskStatus().name(),
                            task.getTaskType() != null ? task.getTaskType().getId() : 0)
                    );

                    days.forEach(dateWrapper -> {
                        double i = task.findTaskImputationValueByDate(dateWrapper.date, currentAccount);
                        imputations.add(new ImputationWrapper(task.getId(), i, dateWrapper.date));
                    });
                });

                projects.add(new ProjectWrapper(
                        projectTasks.getProject().getId(),
                        projectTasks.getProject().getName(),
                        tasks));
            });

            //Default tasks
            List<TaskWrapper> tasks = new ArrayList<>();

            this.projectService.listDefaultTasks(ds, de).stream().forEach(task -> {
                tasks.add(new TaskWrapper(
                        task.getId(),
                        task.getName(), task.getComments(),
                        0, 0,0, 0,
                        task.getStartDate(),
                        task.getEndDate(), TaskStatus.IN_PROGRESS.name(), 0L)
                );

                days.forEach(dateWrapper -> {
                    double i = task.findTaskImputationValueByDate(dateWrapper.date, currentAccount);
                    imputations.add(new ImputationWrapper(task.getId(), i, dateWrapper.date));
                });

            });
            projects.add(new ProjectWrapper(
                    (long) 0,
                    "Default Tasks",
                    tasks));
        }

        if (this.timesheetService != null) {
            validated = this.timesheetService.isTimesheetValidated(currentAccount, year, week);
        }


        Timesheet ts = new Timesheet(validated, year, week, days, projects, imputations);

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(MAPPER.writeValueAsString(ts));
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity updateDataFromTimesheet(HttpServletRequest req, @RequestBody UpdateRequest request) throws JsonProcessingException {

        try {
            final Account actor = this.userInfo.getCurrentAccount();

           // String type = request.getParameter("type");

            Long taskID = request.task;//Long.parseLong(taskStr);
            AbstractTask task = this.projectService.getTaskByID(actor, taskID);

            UpdatedTaskResult updatedTask = null;

            if (request.type.equals("imputation")) {
                Date day = DATE_FORMAT.parse(request.day);
                //double imputation = Double.parseDouble(imputationStr);
                updatedTask = this.projectService.updateTaskImputation(actor, task, day, request.imputation);
            }

            if (request.type.equals("effortLeft")) {
                //double effortLeft = Double.parseDouble(imputationStr);
                updatedTask = this.projectService.updateTaskEffortLeft(actor, (Task) task, request.imputation);
            }

           return ResponseEntity.ok().body(MAPPER.writeValueAsString(updatedTask));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @GetMapping("/validate")
    public ResponseEntity validateTimesheet(HttpServletRequest request) {

        final Account actor = this.userInfo.getCurrentAccount();

        final int week = Integer.parseInt(request.getParameter("week"));
        final int year = Integer.parseInt(request.getParameter("year"));

        try{
            this.timesheetService.validateTimesheet(actor, actor, year, week);
            return ResponseEntity.status(201).build();
        }catch (Exception e){ // TimesheetException
            return ResponseEntity.status(412).build();
        }
    }


    private Date findStartDate(Calendar c, int week, int year) {
        c.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        return c.getTime();
    }

    private Date findEndDate(Calendar c, int week, int year) {
        c.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        return c.getTime();
    }

    public static class UpdateRequest implements Serializable {
        public String type;
        public String day;
        public long task;
        public double imputation;

        public UpdateRequest(){


        };
    }



        public static class Timesheet {

        private final boolean validated;
        private final int year;
        private final int week;
        private final List<DateWrapper> days;
        private final List<ProjectWrapper> projects;
        private final List<ImputationWrapper> imputations;

        public Timesheet(boolean validated, int year, int week, List<DateWrapper> days, List<ProjectWrapper> projects, List<ImputationWrapper> imputationWrappers) {
            this.validated = validated;
            this.year = year;
            this.week = week;
            this.days = days;
            this.projects = projects;
            this.imputations = imputationWrappers;
        }

        public boolean isValidated() {
            return validated;
        }

        public int getYear() {
            return year;
        }

        public int getWeek() {
            return week;
        }

        public List<DateWrapper> getDays() {
            return days;
        }

        public Map<Long, ProjectWrapper> getProjects() {
            final Map<Long, ProjectWrapper> res = new HashMap<>();
            this.projects.forEach(projectWrapper -> {
                res.put(projectWrapper.getProjectID(), projectWrapper);
            });
            return res;
        }

        public Map<String, Map<Long, Double>> getImputations() {

            final Map<String, Map<Long, Double>> res = new HashMap<>();

            this.imputations.stream().forEach(d -> {
                String date = DATE_FORMAT.format(d.date);
                if (res.get(date) == null) {
                    res.put(date, new HashMap<>());
                }
                res.get(date).put(d.taskID, d.value);
            });

            return res;

        }
    }

    public static class DateWrapper {

        private final String day;
        private final Date date;

        public DateWrapper(String day, Date date) {
            this.day = day;
            this.date = date;
        }

        public String getDay() {
            return day;
        }

        public String getDate() {
            return DATE_FORMAT.format(date);
        }
    }

    public static class ProjectWrapper {

        private final Long projectID;
        private final String projectName;
        private final List<TaskWrapper> tasks;

        public ProjectWrapper(Long projectID, String projectName, List<TaskWrapper> tasks) {
            this.projectID = projectID;
            this.projectName = projectName;
            this.tasks = tasks;
        }

        public Long getProjectID() {
            return projectID;
        }

        public String getProjectName() {
            return projectName;
        }

        public Map<Long, TaskWrapper> getTasks() {
            final Map<Long, TaskWrapper> res = new HashMap<>();

            this.tasks.forEach(taskWrapper -> {
                res.put(taskWrapper.getTaskID(), taskWrapper);
            });

            return res;
        }
    }

    public static class TaskWrapper {

        private final Long taskID;
        private final Long typeID;
        private final String taskName;
        private final String taskComments;
        private final String status;
        private final double effortSpent;
        private final double effortLeft;
        private final double realEffort;
        private final double originalEstimate;
        private final Date startDate;
        private final Date endDate;

        public TaskWrapper(Long taskID, String taskName, String taskComments,
                           double effortSpent, double effortLeft, double originalEstimate, double realEffort,
                           Date startDate, Date endDate, String status, Long typeID) {
            this.taskID = taskID;
            this.taskName = taskName;
            this.taskComments = taskComments;
            this.typeID = typeID;
            this.effortSpent = effortSpent;
            this.effortLeft = effortLeft;
            this.originalEstimate = originalEstimate;
            this.realEffort = realEffort;
            this.startDate = startDate;
            this.endDate = endDate;
            this.status = status;
        }

        public String getStartDate() {
            return DATE_FORMAT.format(startDate);
        }

        public String getEndDate() {
            return DATE_FORMAT.format(endDate);
        }

        public double getEffortLeft() {
            return effortLeft;
        }

        public String getTaskComments() {
            return taskComments;
        }

        public Long getTypeID() {
            return typeID;
        }

        public double getRealEffort() {
            return realEffort;
        }

        public double getOriginalEstimate() {
            return originalEstimate;
        }

        public double getEffortSpent() {
            return effortSpent;
        }

        public Long getTaskID() {
            return taskID;
        }

        public String getStatus() {
            return status;
        }

        public String getTaskName() {
            return taskName;
        }
    }

    public static class ImputationWrapper {
        private final Long taskID;
        private final double value;
        private final Date date;

        public ImputationWrapper(Long taskID, double value, Date date) {
            this.taskID = taskID;
            this.value = value;
            this.date = date;
        }

        public Long getTaskID() {
            return taskID;
        }

        public double getValue() {
            return value;
        }

        public Date getDate() {
            return date;
        }
    }
}
