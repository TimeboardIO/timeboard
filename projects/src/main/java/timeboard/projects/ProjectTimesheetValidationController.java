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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import timeboard.core.api.ProjectService;
import timeboard.core.api.TimesheetService;
import timeboard.core.api.UserService;
import timeboard.core.api.exceptions.BusinessException;
import timeboard.core.model.*;
import timeboard.core.security.TimeboardAuthentication;

import java.io.Serializable;
import java.util.Calendar;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/projects/{projectID}/timesheets")
public class ProjectTimesheetValidationController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProjectTimesheetValidationController.class);

    @Autowired
    public ProjectService projectService;

    @Autowired
    public TimesheetService timesheetService;

    @Autowired
    public UserService userService;

    @GetMapping
    protected String timesheetValidationApp(TimeboardAuthentication authentication,
                                            @PathVariable Long projectID, Model model) throws BusinessException {

        final Account actor = authentication.getDetails();
        final Project project = this.projectService.getProjectByID(actor, authentication.getCurrentOrganization(), projectID);

        model.addAttribute("project", project);

        return "project_timesheet_validation.html";
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

        final Pair<Integer, Integer> pair = getOlderWeekYearNotValidated(a, submittedTimesheets);

        return generateSubmittedTimesheets(pair.getFirst(), pair.getSecond(), submittedTimesheets);

    }

    private Pair<Integer, Integer> getOlderWeekYearNotValidated(Account a, List<SubmittedTimesheet> submittedTimesheets){

        if (!submittedTimesheets.isEmpty()) {
            //user already have submitted at least one week
            final Optional<SubmittedTimesheet> lastValidatedSubmittedTimesheet = submittedTimesheets
                    .stream()
                    .filter(st -> st.getTimesheetStatus().equals(ValidationStatus.VALIDATED))
                    .max(Comparator.comparingLong(timesheetService::absoluteWeekNumber));

            final Optional<SubmittedTimesheet> lastSubmittedTimesheet = submittedTimesheets
                    .stream()
                    .max(Comparator.comparingLong(timesheetService::absoluteWeekNumber));

            // user have at least one non validated week.
            final SubmittedTimesheet t = lastValidatedSubmittedTimesheet.orElseGet(lastSubmittedTimesheet::get);

           return Pair.of(t.getYear(), t.getWeek());

        } else {
            // user NEVER submitted a single week
            final Calendar current = Calendar.getInstance();

            current.setTime(a.getAccountCreationTime());
            return Pair.of(current.get(Calendar.YEAR), current.get(Calendar.WEEK_OF_YEAR));

        }
    }


    List<TimesheetWeekWrapper> generateSubmittedTimesheets(int firstYear, int firstWeek, List<SubmittedTimesheet> submittedTimesheets) {

        final List<TimesheetWeekWrapper> returnList = new LinkedList<>();
        final long todayAbsoluteWeekNumber = this.timesheetService.absoluteWeekNumber(Calendar.getInstance());
        final Calendar current = Calendar.getInstance();
        current.set(Calendar.WEEK_OF_YEAR, firstWeek);
        current.set(Calendar.YEAR, firstYear);
        current.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
        final long weekNumber = todayAbsoluteWeekNumber - this.timesheetService.absoluteWeekNumber(firstYear, firstWeek);
        if (weekNumber <= 1) { //Min two weeks
            current.add(Calendar.WEEK_OF_YEAR, (int) (-1 + weekNumber));
        }
        while (this.timesheetService.absoluteWeekNumber(current.get(Calendar.YEAR), current.get(Calendar.WEEK_OF_YEAR)) <= todayAbsoluteWeekNumber) {
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



    @PostMapping(value = "/forceValidation/{userSelectedID}/{selectedYear}/{selectedWeek}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity forceValidation(final TimeboardAuthentication authentication,
                                                        @PathVariable final Long projectID,
                                                        @PathVariable final int selectedYear,
                                                        @PathVariable final int selectedWeek,
                                                        @PathVariable final Long userSelectedID,
                                                        @RequestBody final ArrayList<TimesheetWeekWrapper> weeks) throws BusinessException {
        try {
            final Account target = this.userService.findUserByID(userSelectedID);

            final TimesheetWeekWrapper olderTimesheetWrapper =
                    weeks.stream()
                    .min(Comparator.comparingLong(t -> this.timesheetService.absoluteWeekNumber(t.getYear(), t.getWeek())))
                    .get();

            final long selectedAbsoluteWeekNumber = this.timesheetService.absoluteWeekNumber(selectedYear, selectedWeek);
            final long olderAbsoluteWeekNumber = this.timesheetService.absoluteWeekNumber(olderTimesheetWrapper.year, olderTimesheetWrapper.week);

            if(selectedAbsoluteWeekNumber >= olderAbsoluteWeekNumber) {
                this.timesheetService.forceValidationTimesheets(
                        authentication.getCurrentOrganization(),
                        authentication.getDetails(),
                        target,
                        selectedYear,
                        selectedWeek,
                        olderTimesheetWrapper.year,
                        olderTimesheetWrapper.week
                );
                return ResponseEntity.ok().build();
            }else{
                return ResponseEntity.badRequest().build();
            }
        } catch (Exception e){
            LOGGER.error(e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    public static class TimesheetWeekWrapper {

        private Long id;
        private int year;
        private int week;
        private boolean isValidated;
        private boolean isSubmitted;

        public TimesheetWeekWrapper(){}

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
                    .min(Comparator.comparingLong(t -> timesheetService.absoluteWeekNumber(t.getYear(), t.getWeek())))
                    .map(t -> calendarFromWeek(t.getYear(), t.getWeek()).getTime()).orElseGet(() -> null);
            this.lastApprovedDate = rawList.stream()
                    .filter(st -> st.getTimesheetStatus().equals(ValidationStatus.VALIDATED))
                    .min(Comparator.comparingLong(timesheetService::absoluteWeekNumber))
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
