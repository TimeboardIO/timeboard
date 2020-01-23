package timeboard.projects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import timeboard.core.api.ProjectService;
import timeboard.core.api.TimesheetService;
import timeboard.core.api.exceptions.BusinessException;
import timeboard.core.model.*;
import timeboard.core.security.TimeboardAuthentication;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Calendar;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/projects/{projectID}/timesheets")
public class ProjectTimesheetValidationController {

    @Autowired
    public ProjectService projectService;

    @Autowired
    public TimesheetService timesheetService;

    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");


    @GetMapping
    protected String timesheetValidationApp(TimeboardAuthentication authentication,
                              @PathVariable Long projectID, Model model) throws BusinessException {

        final Account actor = authentication.getDetails();
        final Project project = this.projectService.getProjectByID(actor, authentication.getCurrentOrganization(), projectID);

        model.addAttribute("project", project);

        return "project_timesheet_validation.html";
    }


    @GetMapping(value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<Long, UserWrapper>> list(TimeboardAuthentication authentication,
                                                                        @PathVariable Long projectID) throws BusinessException {
        final Account actor = authentication.getDetails();
        final Project project = this.projectService.getProjectByIdWithAllMembers(actor, projectID);

        Map<Account, List<SubmittedTimesheet>> timesheetsFromProject = this.timesheetService.getTimesheetsFromProject(actor, project);

        // add member with no submitted week
        project.getMembers().stream()
                .map(ProjectMembership::getMember)
                .forEach(m ->
                        timesheetsFromProject.computeIfAbsent(m, t -> new ArrayList<SubmittedTimesheet>()) );

        final Map<Long, UserWrapper> newMap = timesheetsFromProject.entrySet().stream()

                .collect(Collectors.toMap(
                        e -> e.getKey().getId(),
                        e -> new UserWrapper(e.getKey(), e.getValue(), fillSubmittedTimesheets(e.getKey(), e.getValue()))));

        return ResponseEntity.ok(newMap);
    }

    private List<TimesheetWeekWrapper> fillSubmittedTimesheets(Account a, List<SubmittedTimesheet> submittedTimesheets) {

        if(!submittedTimesheets.isEmpty()) {
            //user already have submitted at least one week
            Optional<SubmittedTimesheet> lastNonValidatedSubmittedTimesheet = submittedTimesheets
                    .stream()
                    .filter(t -> !t.isValidated())
                    .max(Comparator.comparingLong(ProjectTimesheetValidationController::absoluteWeekNumber));

            Optional<SubmittedTimesheet> lastSubmittedTimesheet = submittedTimesheets
                    .stream()
                    .max(Comparator.comparingLong(ProjectTimesheetValidationController::absoluteWeekNumber));

            if (lastNonValidatedSubmittedTimesheet.isPresent() || lastSubmittedTimesheet.isPresent()) {
                // user have at least one non validated week.
                SubmittedTimesheet t = lastNonValidatedSubmittedTimesheet.orElseGet(lastSubmittedTimesheet::get);

                return generateSubmittedTimesheets((int) t.getYear(),(int) t.getWeek(),
                        submittedTimesheets);
            } else {
                // user have all his weeks validated so return 2 first weeks
                return submittedTimesheets
                        .stream()
                        .map(t -> new TimesheetWeekWrapper(t, true))
                        .limit(2)
                        .collect(Collectors.toList());
            }
        } else {
            // user NEVER submitted a single week
            Calendar current = Calendar.getInstance();

            current.setTime(a.getAccountCreationTime());

            return generateSubmittedTimesheets(current.get(Calendar.YEAR),
                    current.get(Calendar.WEEK_OF_YEAR), submittedTimesheets);

        }

    }


    List<TimesheetWeekWrapper> generateSubmittedTimesheets(int firstYear, int firstWeek, List<SubmittedTimesheet> submittedTimesheets) {

        List<TimesheetWeekWrapper> returnList = new LinkedList<>();
        long todayAbsoluteWeekNumber = absoluteWeekNumber(Calendar.getInstance());

        Calendar current = Calendar.getInstance();
        current.set(Calendar.WEEK_OF_YEAR, firstWeek);
        current.set(Calendar.YEAR, firstYear);
        while (absoluteWeekNumber(current.get(Calendar.YEAR), current.get(Calendar.WEEK_OF_YEAR)) <= todayAbsoluteWeekNumber) {
            int currentWeek = current.get(Calendar.WEEK_OF_YEAR);
            int currentYear = current.get(Calendar.YEAR);
            Optional<TimesheetWeekWrapper> existingWeek = submittedTimesheets
                    .stream()
                    .filter(t -> t.getWeek() == currentWeek && t.getYear() == currentYear)
                    .map(t -> new TimesheetWeekWrapper(t, true))
                    .findFirst();

            returnList.add(existingWeek.orElseGet(() -> new TimesheetWeekWrapper(currentYear, currentWeek, false, false)));
            current.add(Calendar.WEEK_OF_YEAR, 1);
        }

        return returnList;
    }

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


    private Calendar calendarFromWeek (long year, long week) {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR, (int) year);
        c.set(Calendar.WEEK_OF_YEAR, (int) week);
        return c;
    }

    public class UserWrapper {
        private Long id;
        private String name;
        private String lastSubmittedDate;
        private String lastApprovedDate;
        private String status;
        private List<TimesheetWeekWrapper> weeks;

        public UserWrapper(Account account,  List<SubmittedTimesheet> rawList, List<TimesheetWeekWrapper> weeks) {
            this.id = account.getId();
            this.name = account.getScreenName();
            this.status = "";
            this.weeks = weeks;
            this.lastSubmittedDate = weeks.stream()
                    .filter(TimesheetWeekWrapper::isSubmitted)
                    .min(Comparator.comparingLong(ProjectTimesheetValidationController::absoluteWeekNumber))
                    .map(t -> DATE_FORMAT.format(calendarFromWeek(t.getYear(), t.getWeek()).getTime())).orElseGet(() -> "N/A");
            this.lastApprovedDate = rawList.stream()
                    .filter(SubmittedTimesheet::isValidated)
                    .min(Comparator.comparingLong(ProjectTimesheetValidationController::absoluteWeekNumber))
                    .map(t -> DATE_FORMAT.format(calendarFromWeek(t.getYear(), t.getWeek()).getTime())).orElseGet(() -> "N/A");
        }

        public Long getId() {
            return id;
        }
        public String getName() {
            return name;
        }
        public String getLastSubmittedDate() {
            return lastSubmittedDate;
        }
        public String getLastApprovedDate() {
            return lastApprovedDate;
        }
        public String getStatus() {
            return status;
        }
        public List<TimesheetWeekWrapper> getWeeks() {
            return weeks;
        }
    }


    public static class TimesheetWeekWrapper {

        private Long id;
        private int year;
        private int week;
        private boolean validated;
        private boolean submitted;

        public TimesheetWeekWrapper(SubmittedTimesheet submittedTimesheet, boolean submitted) {
            this.id = submittedTimesheet.getId();
            this.year = (int) submittedTimesheet.getYear();
            this.week = (int) submittedTimesheet.getWeek();
            this.validated = submittedTimesheet.isValidated();
            this.submitted = submitted;
        }

        public TimesheetWeekWrapper(int year, int week, boolean validated, boolean submitted) {
            this.year = year;
            this.week = week;
            this.validated = validated;
            this.submitted = submitted;
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
            return validated;
        }

        public boolean isSubmitted() {
            return submitted;
        }

    }

}
