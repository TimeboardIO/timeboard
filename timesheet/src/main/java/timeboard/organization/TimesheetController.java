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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import timeboard.core.api.OrganizationService;
import timeboard.core.api.ProjectService;
import timeboard.core.api.TimesheetService;
import timeboard.core.api.UpdatedTaskResult;
import timeboard.core.api.exceptions.BusinessException;
import timeboard.core.model.*;
import timeboard.core.security.TimeboardAuthentication;

import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.*;


@Controller
@RequestMapping("/timesheet")
public class TimesheetController {

    private static final Logger LOGGER = LoggerFactory.getLogger(TimesheetController.class);

    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    @Autowired
    private ProjectService projectService;

    @Autowired
    private TimesheetService timesheetService;

    @Autowired
    private OrganizationService organizationService;

    @GetMapping
    protected String currentWeekTimesheet(
            final TimeboardAuthentication authentication, final Model model) throws Exception {
        final Calendar c = Calendar.getInstance();
        return this.fillAndDisplayTimesheetPage(authentication, c.get(Calendar.YEAR), c.get(Calendar.WEEK_OF_YEAR), model);
    }

    @GetMapping(value = "/data", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TimesheetWrapper> getTimesheetData(
            final TimeboardAuthentication authentication,
            final @RequestParam("week") int week,
            final @RequestParam("year") int year) throws BusinessException {

        final Account currentAccount = authentication.getDetails();

        final Calendar beginWorkDate = this.organizationService
                .findOrganizationMembership(authentication.getDetails(), authentication.getCurrentOrganization())
                .get().getCreationDate();

        final List<ProjectWrapper> projects = new ArrayList<>();
        final List<ImputationWrapper> imputations = new ArrayList<>();

        final Calendar c = firstDayOfWeek(week, year);
        final Date ds = findStartDate(c);
        final Date de = findEndDate(c);

        // Create days for current week
        final List<DateWrapper> days = createDaysForCurrentWeek(authentication, c, ds);

        //Get tasks for current week
        if (this.projectService != null) {
            this.projectService.listTasksByProject(
                    authentication.getCurrentOrganization(),
                    currentAccount,
                    ds,
                    de).stream().forEach(projectTasks -> {

                final List<TaskWrapper> tasks = new ArrayList<>();

                projectTasks.getTasks().stream().forEach(task -> {
                    tasks.add(new TaskWrapper(
                            task.getId(), task.getName(),
                            task.getComments(), task.getEffortSpent(),
                            task.getEffortLeft(), task.getOriginalEstimate(),
                            task.getRealEffort(), task.getStartDate(),
                            task.getEndDate(), task.getTaskStatus().name(),
                            task.getTaskType() != null ? task.getTaskType().getId() : 0)
                    );

                    days.forEach(dateWrapper -> {
                        final double i = task.findTaskImputationValueByDate(dateWrapper.date, currentAccount);
                        imputations.add(new ImputationWrapper(task.getId(), i, dateWrapper.date));
                    });
                });

                projects.add(new ProjectWrapper(
                        projectTasks.getProject().getId(),
                        projectTasks.getProject().getName(),
                        tasks));
            });

            //Default tasks
            final List<TaskWrapper> tasks = getDefaultTasks(currentAccount, authentication.getCurrentOrganization(), imputations, ds, de, days);

            projects.add(new ProjectWrapper(
                    (long) 0,
                    "Default Tasks",
                    tasks));
        }

        final TimesheetWrapper ts = new TimesheetWrapper(
                this.timesheetService.getTimesheetValidationStatus(
                        authentication.getCurrentOrganization(),
                        currentAccount,
                        year,
                        week - 1).orElse(null),
                this.timesheetService.getTimesheetValidationStatus(
                        authentication.getCurrentOrganization(),
                        currentAccount,
                        year,
                        week).orElse(null),
                year,
                week,
                beginWorkDate.get(Calendar.YEAR),
                beginWorkDate.get(Calendar.WEEK_OF_YEAR),
                days,
                projects,
                imputations);

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(ts);
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
        model.addAttribute("lastWeekSubmitted",
                this.timesheetService.getTimesheetValidationStatus(
                        authentication.getCurrentOrganization(),
                        authentication.getDetails(),
                        lastWeekYear,
                        lastWeek));

        model.addAttribute("taskTypes", this.organizationService.listTaskType(authentication.getCurrentOrganization()));

        model.addAttribute("projectList",
                this.projectService.listProjects(
                        authentication.getDetails(),
                        authentication.getCurrentOrganization()));

        return "timesheet.html";
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity updateDataFromTimesheet(
            final TimeboardAuthentication authentication,
            @RequestBody final UpdateRequest request) {

        try {
            final Account actor = authentication.getDetails();

            final Long taskID = request.task;
            final AbstractTask task = this.projectService.getTaskByID(actor, taskID);

            UpdatedTaskResult updatedTask = null;

            if (request.type.equals("imputation")) {
                final Date day = DATE_FORMAT.parse(request.day);
                updatedTask = this.projectService.updateTaskImputation(
                        authentication.getCurrentOrganization(), actor, task, day, request.imputation);
            }

            if (request.type.equals("effortLeft")) {
                //double effortLeft = Double.parseDouble(imputationStr);
                updatedTask = this.projectService.updateTaskEffortLeft(actor, (Task) task, request.imputation);
            }

            return ResponseEntity.ok(updatedTask);

        } catch (final Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @GetMapping("/submit")
    public ResponseEntity submitTimesheet(final TimeboardAuthentication authentication, final HttpServletRequest request) {

        final Account actor = authentication.getDetails();

        final int week = Integer.parseInt(request.getParameter("week"));
        final int year = Integer.parseInt(request.getParameter("year"));

        try {
            final Organization currentOrg = this.organizationService.getOrganizationByID(actor, authentication.getCurrentOrganization()).get();
            final SubmittedTimesheet submittedTimesheet =
                    this.timesheetService.submitTimesheet(
                            authentication.getCurrentOrganization(),
                            actor,
                            actor,
                            currentOrg,
                            year,
                            week);

            return ResponseEntity.ok(submittedTimesheet.getTimesheetStatus());
        } catch (final Exception e) { // TimesheetException
            LOGGER.error(e.getMessage(), e);
            return ResponseEntity.status(412).build();
        }
    }


    private List<TaskWrapper> getDefaultTasks(final Account currentAccount,
                                              final Long orgID,
                                              final List<ImputationWrapper> imputations,
                                              final Date ds,
                                              final Date de,
                                              final List<DateWrapper> days) throws BusinessException {

        final List<TaskWrapper> tasks = new ArrayList<>();


        this.organizationService.listDefaultTasks(orgID, ds, de).stream().forEach(task -> {
            tasks.add(new TaskWrapper(
                    task.getId(),
                    task.getName(), task.getComments(),
                    0, 0, 0, 0,
                    organizationService.getOrganizationByID(currentAccount, orgID).get().getCreatedDate().getTime(),
                    null,
                    TaskStatus.IN_PROGRESS.name(),
                    0L)
            );

            days.forEach(dateWrapper -> {
                final double i = task.findTaskImputationValueByDate(dateWrapper.date, currentAccount);
                imputations.add(new ImputationWrapper(task.getId(), i, dateWrapper.date));
            });

        });

        return tasks;
    }

    private List<DateWrapper> createDaysForCurrentWeek(
            final TimeboardAuthentication authentication, final Calendar c, final Date ds) throws BusinessException {

        final Calendar beginWorkDateForCurrentOrg = this.organizationService
                .findOrganizationMembership(authentication.getDetails(), authentication.getCurrentOrganization())
                .get().getCreationDate();


        final List<DateWrapper> days = new ArrayList<>();
        c.setTime(ds); //reset calendar to start date
        for (int i = 0; i < 7; i++) {
            if (c.getTime().getTime() >= beginWorkDateForCurrentOrg.getTime().getTime()) {
                final DateWrapper dw = new DateWrapper(
                        c.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.ENGLISH).substring(0, 3),
                        c.getTime()
                );
                days.add(dw);
            }
            c.add(Calendar.DAY_OF_YEAR, 1);

        }
        return days;
    }

    private Calendar firstDayOfWeek(final int week, final int year) {
        final Calendar c = Calendar.getInstance();
        c.set(Calendar.WEEK_OF_YEAR, week);
        c.set(Calendar.YEAR, year);
        c.setFirstDayOfWeek(Calendar.MONDAY);
        c.set(Calendar.HOUR_OF_DAY, 2);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c;
    }

    private Date findStartDate(final Calendar c) {
        c.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        return c.getTime();
    }

    private Date findEndDate(final Calendar c) {
        c.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        return c.getTime();
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

    public static class UpdateRequest implements Serializable {
        public String type;
        public String day;
        public long task;
        public double imputation;

        public UpdateRequest() {
        }

        ;
    }


    public static class TimesheetWrapper implements Serializable {

        private final ValidationStatus previousWeekSubmissionStatus;
        private final ValidationStatus currentWeekValidationStatus;
        private final int year;
        private final int week;
        private final boolean disablePrev;
        private final boolean disableNext;
        private final List<DateWrapper> days;
        private final List<ProjectWrapper> projects;
        private final List<ImputationWrapper> imputations;

        public TimesheetWrapper(
                final ValidationStatus previousWeekSubmissionStatus,
                final ValidationStatus currentWeekValidationStatus,
                final int year,
                final int week,
                final int beginWorkYear,
                final int beginWorkWeek,
                final List<DateWrapper> days,
                final List<ProjectWrapper> projects,
                final List<ImputationWrapper> imputationWrappers
        ) {


            final Calendar c = Calendar.getInstance();
            final int currentWeek = c.get(Calendar.WEEK_OF_YEAR);
            final int currentYear = c.get(Calendar.YEAR);

            this.previousWeekSubmissionStatus = previousWeekSubmissionStatus;
            this.currentWeekValidationStatus = currentWeekValidationStatus;
            this.year = year;
            this.week = week;
            this.days = days;
            this.disablePrev = year == beginWorkYear && week == beginWorkWeek;
            this.disableNext = year > currentYear || year == currentYear && week >= currentWeek;
            this.projects = projects;
            this.imputations = imputationWrappers;
        }

        public ValidationStatus getPreviousWeekSubmissionStatus() {
            return previousWeekSubmissionStatus;
        }

        public ValidationStatus getCurrentWeekValidationStatus() {
            return currentWeekValidationStatus;
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
                final String date = DATE_FORMAT.format(d.date);
                if (res.get(date) == null) {
                    res.put(date, new HashMap<>());
                }
                res.get(date).put(d.taskID, d.value);
            });

            return res;

        }



        public boolean isDisablePrev() {
            return disablePrev;
        }

        public boolean isDisableNext() {
            return disableNext;
        }
    }

    public static class DateWrapper implements Serializable {

        private final String day;
        private final Date date;

        public DateWrapper(final String day, final Date date) {
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

    public static class ProjectWrapper implements Serializable {

        private final Long projectID;
        private final String projectName;
        private final List<TaskWrapper> tasks;

        public ProjectWrapper(final Long projectID, final String projectName, final List<TaskWrapper> tasks) {
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

        public TaskWrapper(final Long taskID, final String taskName, final String taskComments,
                           final double effortSpent, final double effortLeft, final double originalEstimate, final double realEffort,
                           final Date startDate, final Date endDate, final String status, final Long typeID) {
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
            return startDate == null ? null : DATE_FORMAT.format(startDate);

        }

        public String getEndDate() {
            return endDate == null ? null : DATE_FORMAT.format(endDate);
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

        public ImputationWrapper(final Long taskID, final double value, final Date date) {
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
