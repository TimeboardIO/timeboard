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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import timeboard.core.api.*;
import timeboard.core.api.exceptions.BusinessException;
import timeboard.core.internal.observers.emails.EmailStructure;
import timeboard.core.model.*;
import timeboard.core.security.TimeboardAuthentication;
import org.springframework.web.servlet.LocaleResolver;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.*;


@Controller
@RequestMapping(TimesheetController.PATH)
public class TimesheetController {

    public static final String PATH = "/timesheet";
    private static final Logger LOGGER = LoggerFactory.getLogger(TimesheetController.class);
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    @Autowired
    private ProjectService projectService;

    @Autowired
    private TimesheetService timesheetService;

    @Autowired
    private OrganizationService organizationService;

    @Autowired
    public EmailService emailService;

    @Autowired
    private LocaleResolver localeResolver;

    @GetMapping
    protected String currentWeekTimesheet(
            final TimeboardAuthentication authentication, final Model model) throws Exception {
        final Calendar c = Calendar.getInstance();
        return this.fillAndDisplayTimesheetPage(authentication,
                authentication.getDetails(), c.get(Calendar.YEAR), c.get(Calendar.WEEK_OF_YEAR), model);
    }

    @GetMapping("/{user}")
    protected String currentWeekTimesheet(final TimeboardAuthentication authentication,
                                          @PathVariable("user") final Account user,
                                          final Model model) throws Exception {
        final Calendar c = Calendar.getInstance();
        return this.fillAndDisplayTimesheetPage(authentication, user, c.get(Calendar.YEAR), c.get(Calendar.WEEK_OF_YEAR), model);
    }

    @GetMapping(value = "/data", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TimesheetWrapper> getTimesheetData(
            final TimeboardAuthentication authentication,
            final @RequestParam("user") Account user,
            final @RequestParam("week") int week,
            final @RequestParam("year") int year) throws BusinessException {

        final Account currentAccount = user;

        final Calendar beginWorkDate = this.organizationService
                .findOrganizationMembership(currentAccount, authentication.getCurrentOrganization())
                .get().getCreationDate();

        final List<ProjectWrapper> projects = new ArrayList<>();
        final List<ImputationWrapper> imputations = new ArrayList<>();
        final Calendar c = firstDayOfWeek(week, year);
        final Calendar firstDayOfWeek = findStartDate(c);
        final Calendar lastDayOfWeek = findEndDate(c);

        // Create days for current week
        final List<DateWrapper> days = createDaysForCurrentWeek(
                authentication.getCurrentOrganization(), currentAccount, c,
                firstDayOfWeek.getTime());

        //Get tasks for current week
        if (this.projectService != null) {
            this.projectService.listTasksByProject(
                    authentication.getCurrentOrganization(),
                    currentAccount,
                    firstDayOfWeek.getTime(),
                    lastDayOfWeek.getTime()).stream().forEach(projectTasks -> {

                final List<TaskWrapper> tasks = new ArrayList<>();
                projectTasks.getTasks().forEach(task -> {
                    tasks.add(new TaskWrapper(task.getId(), task.getName(),
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
            final List<TaskWrapper> tasks = getDefaultTasks(currentAccount, authentication.getCurrentOrganization(),
                    imputations, firstDayOfWeek.getTime(), lastDayOfWeek.getTime(), days);
            projects.add(new ProjectWrapper(0L, "Default Tasks", tasks));
        }
        final boolean canValidate = this.projectService.isOwnerOfAnyUserProject(authentication.getDetails(), currentAccount);
        final Calendar creationDate = this.organizationService
                .findOrganizationMembership(currentAccount, authentication.getCurrentOrganization())
                .orElseThrow().getCreationDate();
        creationDate.set(Calendar.DAY_OF_WEEK, Calendar.WEDNESDAY);

        final boolean isFirstWeek =
                firstDayOfWeek.compareTo(creationDate) <= 0
                        && lastDayOfWeek.compareTo(creationDate) >= 0;

        final TimesheetWrapper ts = new TimesheetWrapper(
                isFirstWeek ? ValidationStatus.VALIDATED :
                this.timesheetService.getTimesheetValidationStatus(
                    authentication.getCurrentOrganization(),
                    currentAccount,
                    findPreviousWeekYear(c, week, year),
                    findPreviousWeek(c, week, year)).orElse(null),
                this.timesheetService.getTimesheetValidationStatus(
                    authentication.getCurrentOrganization(),
                    currentAccount,
                    year, week).orElse(null),
                year, week,
                beginWorkDate.get(Calendar.YEAR),
                beginWorkDate.get(Calendar.WEEK_OF_YEAR),
                days, projects, imputations, canValidate);

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(ts);
    }


    @GetMapping("/{year}/{week}")
    public String fillAndDisplayTimesheetPage(
            final TimeboardAuthentication authentication,
            @PathVariable("year") final int year,
            @PathVariable("week") final int week,
            final Model model) throws BusinessException {
        return this.fillAndDisplayTimesheetPage(authentication, authentication.getDetails(), year, week, model);
    }

    @GetMapping("/{user}/{year}/{week}")
    public String fillAndDisplayTimesheetPage(
            final TimeboardAuthentication authentication,
            @PathVariable("user") final Account user,
            @PathVariable("year") final int year,
            @PathVariable("week") final int week,
            final Model model) throws BusinessException {

        final Calendar beginWorkDateForCurrentOrg = this.organizationService
                .findOrganizationMembership(user, authentication.getCurrentOrganization())
                .get().getCreationDate();

        final Calendar c = beginWorkDateForCurrentOrg;


        c.set(Calendar.WEEK_OF_YEAR, week);
        c.set(Calendar.YEAR, year);
        c.set(Calendar.HOUR_OF_DAY, 2);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);

        final int lastWeek = this.findPreviousWeek(c, week, year);
        final int lastWeekYear = this.findPreviousWeekYear(c, week, year);

        model.addAttribute("week", week);
        model.addAttribute("year", year);
        model.addAttribute("userID", user.getId());
        model.addAttribute("userScreenName", user.getScreenName());
        model.addAttribute("actorID", authentication.getDetails().getId());
        model.addAttribute("lastWeekSubmitted",
                this.timesheetService.getTimesheetValidationStatus(
                        authentication.getCurrentOrganization(),
                        user,
                        lastWeekYear,
                        lastWeek));

        model.addAttribute("taskTypes", this.organizationService.listTaskType(authentication.getCurrentOrganization()));

        model.addAttribute("projectList",
                this.projectService.listProjects(
                        user, authentication.getCurrentOrganization()));

        return "timesheet.html";
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity updateDataFromTimesheet(
            final TimeboardAuthentication authentication,
            @RequestBody final UpdateRequest request) {

        try {

            if ((request.imputation * 100) % 5 != 0) { // Modulo with int and not double
                return ResponseEntity.badRequest().body("Your imputation value is not valid. The step is 0.05.");
            }


            final Account actor = authentication.getDetails();

            final Long taskID = request.task;
            final AbstractTask task = this.projectService.getTaskByID(actor, taskID);

            UpdatedTaskResult updatedTask = null;

            if (request.type.equals("imputation")) {
                final Date day = DATE_FORMAT.parse(request.day);
                updatedTask = this.timesheetService.updateTaskImputation(
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

    @GetMapping("/submit/{year}/{week}")
    public ResponseEntity submitTimesheet(final TimeboardAuthentication authentication,
                                          @PathVariable final int year,
                                          @PathVariable final int week) {

        final Account actor = authentication.getDetails();


        try {
            final Organization currentOrg = this.organizationService.getOrganizationByID(
                    actor, authentication.getCurrentOrganization().getId()).get();

            final SubmittedTimesheet submittedTimesheet =
                    this.timesheetService.submitTimesheet(
                            currentOrg,
                            actor,
                            year,
                            week);

            return ResponseEntity.ok(submittedTimesheet.getTimesheetStatus());
        } catch (final Exception e) { // TimesheetException
            LOGGER.error(e.getMessage(), e);
            return ResponseEntity.status(412).build();
        }
    }


    @PostMapping("/validate/{user}/{year}/{week}")
    public ResponseEntity validateTimesheet(final TimeboardAuthentication authentication,
                                            @PathVariable final Account user,
                                            @PathVariable final int year,
                                            @PathVariable final int week) {

        final Account actor = authentication.getDetails();

        if (!projectService.isOwnerOfAnyUserProject(actor, user)) {
            return ResponseEntity.badRequest().body("You have not enough right do do this.");
        }
        try {
            final Optional<SubmittedTimesheet> submittedTimesheet =
                    this.timesheetService.getSubmittedTimesheet(
                            authentication.getCurrentOrganization(),
                            user,
                            year,
                            week);

            if (submittedTimesheet.isPresent()) {
                final SubmittedTimesheet result =
                        this.timesheetService.validateTimesheet(
                                authentication.getCurrentOrganization(),
                                actor, submittedTimesheet.get());

                return ResponseEntity.ok(result.getTimesheetStatus());

            } else {
                return ResponseEntity.badRequest().body("Could not find this week. Was it submitted ?");
            }
        } catch (final Exception e) { // TimesheetException
            LOGGER.error(e.getMessage(), e);
            return ResponseEntity.badRequest().body("An error occurred when validating this week. ");
        }
    }


    @PostMapping("/reject/{user}/{year}/{week}")
    public ResponseEntity rejectTimesheet(final TimeboardAuthentication authentication,
                                          @PathVariable final Account user,
                                          @PathVariable final int year,
                                          @PathVariable final int week) {

        final Account actor = authentication.getDetails();
        final Optional<Organization> org = organizationService.getOrganizationByID(
                actor,
                authentication.getCurrentOrganization().getId());

        if (!projectService.isOwnerOfAnyUserProject(actor, user)) {
            return ResponseEntity.badRequest().body("You have not enough right do do this.");
        }
        try {
            final Optional<SubmittedTimesheet> submittedTimesheet =
                    this.timesheetService.getSubmittedTimesheet(
                            authentication.getCurrentOrganization(),
                            user,
                            year,
                            week);

            if (submittedTimesheet.isPresent()) {
                final SubmittedTimesheet result = this.timesheetService.rejectTimesheet(org.get(), actor, submittedTimesheet.get());
                return ResponseEntity.ok(result.getTimesheetStatus());

            } else {
                return ResponseEntity.badRequest().body("Could not find this week. Was it submitted ?");
            }
        } catch (final Exception e) { // TimesheetException
            LOGGER.error(e.getMessage(), e);
            return ResponseEntity.badRequest().body("An error occurred when rejecting this week. ");
        }
    }

    @DeleteMapping("/cancelTask/{taskID}")
    public ResponseEntity cancelTask(final TimeboardAuthentication authentication,
                                          @PathVariable final Long taskID) {
        try {
            this.projectService.deleteTaskByID(authentication.getDetails(), taskID);
            return ResponseEntity.ok().build();
        } catch (BusinessException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @PostMapping(value = "/sendReminderMail/{targetUser}")
    public ResponseEntity sendReminderMail(HttpServletRequest request,
                                           final TimeboardAuthentication authentication,
                                           @PathVariable Account targetUser)
            throws MessagingException, BusinessException {

        final Account actor = authentication.getDetails();

        final HashMap<String, Object> data =  new HashMap<>();

        final List<SubmittedTimesheet> list = this.timesheetService.getSubmittedTimesheets(
                authentication.getCurrentOrganization(), authentication.getDetails(), targetUser);

        final long todayAbsoluteWeekNumber = this.timesheetService.absoluteWeekNumber(Calendar.getInstance());

        final Optional<SubmittedTimesheet> max = list.stream()
                .filter(e -> !e.getTimesheetStatus().equals(ValidationStatus.REJECTED)) // ignore rejected
                .max(Comparator.comparingLong(timesheetService::absoluteWeekNumber));

        if (max.isPresent()) {
            final SubmittedTimesheet lastSubmittedTimesheet = max.get();
            final Calendar c = Calendar.getInstance();
            c.set(Calendar.WEEK_OF_YEAR,lastSubmittedTimesheet.getWeek());
            c.set(Calendar.YEAR,lastSubmittedTimesheet.getYear());
            c.add(Calendar.WEEK_OF_YEAR, 1);

            data.put("missingWeeksNumber", todayAbsoluteWeekNumber - this.timesheetService.absoluteWeekNumber(lastSubmittedTimesheet));
            data.put("weekToValidate", c.get(Calendar.WEEK_OF_YEAR));
            data.put("yearToValidate", c.get(Calendar.YEAR));
            data.put("link", request.getLocalName() +"/timesheet/"+ targetUser.getId()
                    +"/"+c.get(Calendar.YEAR)+"/"+c.get(Calendar.WEEK_OF_YEAR));

        } else {
            // never submitted a first week or it been rejected
            final Calendar beginWorkDate = this.organizationService
                    .findOrganizationMembership(actor, authentication.getCurrentOrganization()).get().getCreationDate();

            data.put("missingWeeksNumber", todayAbsoluteWeekNumber - this.timesheetService.absoluteWeekNumber(beginWorkDate));
            data.put("weekToValidate", beginWorkDate.get(Calendar.WEEK_OF_YEAR));
            data.put("yearToValidate", beginWorkDate.get(Calendar.YEAR));
            data.put("link", request.getLocalName() +"/timesheet/"+ targetUser.getId()+"/"
                    +beginWorkDate.get(Calendar.YEAR)+"/"+beginWorkDate.get(Calendar.WEEK_OF_YEAR));

        }

        data.put("targetID", targetUser.getId());
        data.put("targetScreenName", targetUser.getScreenName());
        final EmailStructure structure = new EmailStructure(targetUser.getEmail(), actor.getEmail(), "Reminder", data, "mail/reminder.html");


        this.emailService.sendMessage(structure, localeResolver.resolveLocale(request));


        return ResponseEntity.ok().build();

    }




    private List<TaskWrapper> getDefaultTasks(final Account currentAccount,
                                              final Organization org,
                                              final List<ImputationWrapper> imputations,
                                              final Date ds,
                                              final Date de,
                                              final List<DateWrapper> days) throws BusinessException {

        final List<TaskWrapper> tasks = new ArrayList<>();


        this.organizationService.listDefaultTasks(org, ds, de).stream().forEach(task -> {
            tasks.add(new TaskWrapper(
                    task.getId(),
                    task.getName(), task.getComments(),
                    0, 0, 0, 0,
                    organizationService.getOrganizationByID(currentAccount, org.getId()).get().getCreatedDate().getTime(),
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
            final Organization organization, final Account user, final Calendar c, final Date ds) throws BusinessException {

        final Calendar beginWorkDateForCurrentOrg = this.organizationService
                .findOrganizationMembership(user, organization)
                .get().getCreationDate();


        final List<DateWrapper> days = new ArrayList<>();
        c.setTime(ds); //reset calendar to start date
        for (int i = 0; i < 7; i++) {
            if (c.getTime().getTime() >= beginWorkDateForCurrentOrg.getTime().getTime()) {
                final DateWrapper dw = new DateWrapper(c.getTime());
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

    private Calendar findStartDate(final Calendar c) {
        final Calendar result = Calendar.getInstance();
        result.setTime(c.getTime());
        result.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        return result;
    }

    private Calendar findEndDate(final Calendar c) {
        final Calendar result = Calendar.getInstance();
        result.setTime(c.getTime());
        result.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        return result;
    }

    private int findPreviousWeekYear(final Calendar c, final int week, final int year) {
        c.set(Calendar.YEAR, year);
        c.set(Calendar.WEEK_OF_YEAR, week);
        c.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
        c.add(Calendar.WEEK_OF_YEAR, -1); // remove 1 week
        return c.get(Calendar.YEAR);
    }

    private int findPreviousWeek(final Calendar c, final int week, final int year) {
        c.set(Calendar.YEAR, year);
        c.set(Calendar.WEEK_OF_YEAR, week);
        c.add(Calendar.WEEK_OF_YEAR, -1); // remove 1 week
        return c.get(Calendar.WEEK_OF_YEAR);
    }

    public static class UpdateRequest implements Serializable {
        public String type;
        public String day;
        public long task;
        public double imputation;

        public UpdateRequest() {
        }
    }


    public static class TimesheetWrapper implements Serializable {

        private final ValidationStatus previousWeekValidationStatus;
        private final ValidationStatus currentWeekValidationStatus;
        private final int year;
        private final int week;
        private final boolean disablePrev;
        private final boolean disableNext;
        private final boolean canValidate;
        private final List<DateWrapper> days;
        private final List<ProjectWrapper> projects;
        private final List<ImputationWrapper> imputations;

        public TimesheetWrapper(
                final ValidationStatus previousWeekValidationStatus,
                final ValidationStatus currentWeekValidationStatus,
                final int year,
                final int week,
                final int beginWorkYear,
                final int beginWorkWeek,
                final List<DateWrapper> days,
                final List<ProjectWrapper> projects,
                final List<ImputationWrapper> imputationWrappers,
                final boolean canValidate
        ) {


            final Calendar c = Calendar.getInstance();
            final int currentWeek = c.get(Calendar.WEEK_OF_YEAR);
            final int currentYear = c.get(Calendar.YEAR);

            this.previousWeekValidationStatus = previousWeekValidationStatus;
            this.currentWeekValidationStatus = currentWeekValidationStatus;
            this.year = year;
            this.week = week;
            this.days = days;
            this.canValidate = canValidate;
            this.disablePrev = year == beginWorkYear && week == beginWorkWeek;
            this.disableNext = year > currentYear || year == currentYear && week >= currentWeek;
            this.projects = projects;
            this.imputations = imputationWrappers;
        }

        public ValidationStatus getPreviousWeekValidationStatus() {
            return previousWeekValidationStatus;
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

        public boolean isCanValidate() {
            return canValidate;
        }


        public Map<Long, ProjectWrapper> getProjects() {
            final Map<Long, ProjectWrapper> res = new HashMap<>();
            this.projects.forEach(projectWrapper -> res.put(projectWrapper.getProjectID(), projectWrapper));
            return res;
        }

        public Map<String, Map<Long, Double>> getImputations() {

            final Map<String, Map<Long, Double>> res = new HashMap<>();

            this.imputations.forEach(d -> {
                final String date = DATE_FORMAT.format(d.date);
                res.computeIfAbsent(date, k -> new HashMap<>());
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


        private final int dayNum;
        private final Date date;

        public DateWrapper(final Date date) {
            final Calendar c = Calendar.getInstance();
            this.date = date;
            c.setTime(date);
            this.dayNum = c.get(Calendar.DAY_OF_WEEK);
        }

        public int getDayNum() {
            return dayNum;
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

            this.tasks.forEach(taskWrapper -> res.put(taskWrapper.getTaskID(), taskWrapper));

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
