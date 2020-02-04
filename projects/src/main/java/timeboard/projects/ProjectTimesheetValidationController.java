package timeboard.projects;

/*-
 * #%L
 * projects
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

import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import timeboard.core.api.EmailService;
import timeboard.core.api.ProjectService;
import timeboard.core.api.TimesheetService;
import timeboard.core.api.exceptions.BusinessException;
import timeboard.core.internal.observers.emails.EmailStructure;
import timeboard.core.model.*;
import timeboard.core.security.TimeboardAuthentication;

import javax.mail.MessagingException;
import java.io.Serializable;
import java.util.Calendar;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/projects/{projectID}/timesheets")
public class ProjectTimesheetValidationController {

    @Autowired
    public ProjectService projectService;

    @Autowired
    public TimesheetService timesheetService;

    @Autowired
    public EmailService emailService;

    private static long absoluteWeekNumber(SubmittedTimesheet t) {
        return absoluteWeekNumber((int) t.getYear(), (int) t.getWeek());
    }

    private static long absoluteWeekNumber(TimesheetWeekWrapper t) {
        return absoluteWeekNumber((int) t.getYear(), (int) t.getWeek());
    }

    private static long absoluteWeekNumber(int year, int week) {
        return (long) (year * 53) + week;
    }

    private static long absoluteWeekNumber(Calendar c) {
        return absoluteWeekNumber(c.get(Calendar.YEAR), c.get(Calendar.WEEK_OF_YEAR));
    }

    @GetMapping
    protected String timesheetValidationApp(TimeboardAuthentication authentication,
                                            @PathVariable Long projectID, Model model) throws BusinessException {

        final Account actor = authentication.getDetails();
        final Project project = this.projectService.getProjectByID(actor, authentication.getCurrentOrganization(), projectID);

        model.addAttribute("project", project);

        return "project_timesheet_validation.html";
    }


    @GetMapping(value = "/sendReminderMail/{targetUser}")
    public ResponseEntity sendReminderMail(
            final TimeboardAuthentication authentication, final Model model, @PathVariable Account targetUser) throws MessagingException {

        final Account actor = authentication.getDetails();

       final HashMap<String, Object> data =  new HashMap<>();

        data.put("message", "Test");



        this.getFirstTimesheetToSubmit();
        final EmailStructure structure = new EmailStructure(targetUser.getEmail(), actor.getEmail(), "Reminder", data, "mail/reminder.html");

       this.emailService.sendMessage(structure);

       return ResponseEntity.ok().build();

    }

    private void getFirstTimesheetToSubmit() {


    }


    @GetMapping(value = "/listProjectMembersTimesheets", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<Long, UserWrapper>> list(TimeboardAuthentication authentication,
                                                       @PathVariable Long projectID) throws BusinessException {
        final Account actor = authentication.getDetails();
        final Project project = this.projectService.getProjectByIdWithAllMembers(actor, projectID);

        final Map<Account, List<SubmittedTimesheet>> timesheetsFromProject =
                this.timesheetService.getProjectTimesheetByAccounts(
                        authentication.getCurrentOrganization(),
                        actor,
                        project);

        // add member with no submitted week
        project.getMembers().stream()
                .map(ProjectMembership::getMember)
                .forEach(m ->
                        timesheetsFromProject.computeIfAbsent(m, t -> new ArrayList<SubmittedTimesheet>()));

        final Map<Long, UserWrapper> newMap = timesheetsFromProject.entrySet().stream()

                .collect(Collectors.toMap(
                        e -> e.getKey().getId(),
                        e -> new UserWrapper(e.getKey(), e.getValue(), fillTimesheetWeeks(e.getKey(), e.getValue()))));

        return ResponseEntity.ok(newMap);
    }

    private List<TimesheetWeekWrapper> fillTimesheetWeeks(Account a, List<SubmittedTimesheet> submittedTimesheets) {

        if (!submittedTimesheets.isEmpty()) {
            //user already have submitted at least one week
            final Optional<SubmittedTimesheet> lastValidatedSubmittedTimesheet = submittedTimesheets
                    .stream()
                    .filter(st -> st.getTimesheetStatus().equals(ValidationStatus.VALIDATED))
                    .max(Comparator.comparingLong(ProjectTimesheetValidationController::absoluteWeekNumber));

            final Optional<SubmittedTimesheet> lastSubmittedTimesheet = submittedTimesheets
                    .stream()
                    .max(Comparator.comparingLong(ProjectTimesheetValidationController::absoluteWeekNumber));

            // user have at least one non validated week.
            final SubmittedTimesheet t = lastValidatedSubmittedTimesheet.orElseGet(lastSubmittedTimesheet::get);

            return generateSubmittedTimesheets((int) t.getYear(), (int) t.getWeek(),
                    submittedTimesheets);

        } else {
            // user NEVER submitted a single week
            final Calendar current = Calendar.getInstance();

            current.setTime(a.getAccountCreationTime());

            return generateSubmittedTimesheets(current.get(Calendar.YEAR),
                    current.get(Calendar.WEEK_OF_YEAR), submittedTimesheets);

        }

    }

    List<TimesheetWeekWrapper> generateSubmittedTimesheets(int firstYear, int firstWeek, List<SubmittedTimesheet> submittedTimesheets) {

        final List<TimesheetWeekWrapper> returnList = new LinkedList<>();
        final long todayAbsoluteWeekNumber = absoluteWeekNumber(Calendar.getInstance());
        final Calendar current = Calendar.getInstance();
        current.set(Calendar.WEEK_OF_YEAR, firstWeek);
        current.set(Calendar.YEAR, firstYear);
        final long weekNumber = todayAbsoluteWeekNumber - absoluteWeekNumber(firstYear, firstWeek);
        if (weekNumber <= 1) { //Min two weeks
            current.add(Calendar.WEEK_OF_YEAR, (int) (-1 + weekNumber));
        }
        while (absoluteWeekNumber(current.get(Calendar.YEAR), current.get(Calendar.WEEK_OF_YEAR)) <= todayAbsoluteWeekNumber) {
            final int currentWeek = current.get(Calendar.WEEK_OF_YEAR);
            final int currentYear = current.get(Calendar.YEAR);
            final Optional<TimesheetWeekWrapper> existingWeek = submittedTimesheets
                    .stream()
                    .filter(t -> t.getWeek() == currentWeek && t.getYear() == currentYear)
                    .map(t -> new TimesheetWeekWrapper(t, true))
                    .findFirst();

            returnList.add(existingWeek.orElseGet(() -> new TimesheetWeekWrapper(currentYear, currentWeek, false, false)));
            current.add(Calendar.WEEK_OF_YEAR, 1);
        }

        return returnList;
    }

    private Calendar calendarFromWeek(long year, long week) {
        final Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR, (int) year);
        c.set(Calendar.WEEK_OF_YEAR, (int) week);
        return c;
    }

    public static class TimesheetWeekWrapper {

        private Long id;
        private int year;
        private int week;
        private boolean isValidated;
        private boolean isSubmitted;

        public TimesheetWeekWrapper(SubmittedTimesheet submittedTimesheet, boolean submitted) {
            this.id = submittedTimesheet.getId();
            this.year = (int) submittedTimesheet.getYear();
            this.week = (int) submittedTimesheet.getWeek();
            this.isValidated = submittedTimesheet.getTimesheetStatus().equals(ValidationStatus.VALIDATED);
            this.isSubmitted = submitted;
        }

        public TimesheetWeekWrapper(int year, int week, boolean validated, boolean submitted) {
            this.year = year;
            this.week = week;
            this.isValidated = validated;
            this.isSubmitted = submitted;
        }

        public int getYear() {
            return year;
        }

        public int getWeek() {
            return week;
        }

        public Long getId() {
            return id;
        }

        public boolean isValidated() {
            return isValidated;
        }

        public boolean isSubmitted() {
            return isSubmitted;
        }

    }

    public class UserWrapper implements Serializable {

        private Long id;
        private String name;

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        private Date lastSubmittedDate;
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        private Date lastApprovedDate;

        private String statusColor;
        private List<TimesheetWeekWrapper> weeks;

        public UserWrapper(Account account, List<SubmittedTimesheet> rawList, List<TimesheetWeekWrapper> weeks) {
            this.id = account.getId();
            this.name = account.getScreenName();

            if (weeks.stream().anyMatch(e -> !e.isValidated() && e.isSubmitted())) {
                this.statusColor = "red";
            } else if (weeks.stream().anyMatch(e -> !e.isValidated())) {
                this.statusColor = "yellow";
            } else {
                this.statusColor = "green";
            }
            this.weeks = weeks;
            this.lastSubmittedDate = weeks.stream()
                    .filter(TimesheetWeekWrapper::isSubmitted)
                    .min(Comparator.comparingLong(ProjectTimesheetValidationController::absoluteWeekNumber))
                    .map(t -> calendarFromWeek(t.getYear(), t.getWeek()).getTime()).orElseGet(() -> null);
            this.lastApprovedDate = rawList.stream()
                    .filter(st -> st.getTimesheetStatus().equals(ValidationStatus.VALIDATED))
                    .min(Comparator.comparingLong(ProjectTimesheetValidationController::absoluteWeekNumber))
                    .map(t -> calendarFromWeek(t.getYear(), t.getWeek()).getTime()).orElseGet(() -> null);
        }

        public Long getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public Date getLastSubmittedDate() {
            return lastSubmittedDate;
        }

        public Date getLastApprovedDate() {
            return lastApprovedDate;
        }

        public String getStatus() {
            return statusColor;
        }

        public List<TimesheetWeekWrapper> getWeeks() {
            return weeks;
        }

        public String getStatusColor() {
            return statusColor;
        }

    }

}
