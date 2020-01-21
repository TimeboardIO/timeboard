package timeboard.projects;

import org.springframework.beans.factory.annotation.Autowired;
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

    @GetMapping
    protected String timesheetValidationApp(TimeboardAuthentication authentication,
                              @PathVariable Long projectID, Model model) throws BusinessException {

        final Account actor = authentication.getDetails();
        final Project project = this.projectService.getProjectByID(actor, authentication.getCurrentOrganization(), projectID);


        Map<Account, List<SubmittedTimesheet>> timesheetsFromProject = this.timesheetService.getTimesheetsFromProject(actor, project);

        // add member with no submitted week
        project.getMembers().stream()
                .map(ProjectMembership::getMember)
                .forEach(m ->
                        timesheetsFromProject.computeIfAbsent(m, t -> new ArrayList<SubmittedTimesheet>()) );

        final Map<String, List<SubmittedTimesheet>> newMap = timesheetsFromProject.entrySet().stream()
                .collect(Collectors.toMap(e -> e.getKey().getScreenName(), e -> fillSubmittedTimesheets(e.getKey(), e.getValue())));

        model.addAttribute("project", project);
        model.addAttribute("timesheets", newMap);

        return "project_timesheet_validation.html";
    }

    private List<SubmittedTimesheet> fillSubmittedTimesheets(Account a, List<SubmittedTimesheet> submittedTimesheets) {

        if(!submittedTimesheets.isEmpty()) {
            //user already have submitted at least one week
            Optional<SubmittedTimesheet> lastNonValidatedSubmittedTimesheet = submittedTimesheets
                    .stream()
                    .filter(t -> !t.isValidated())
                    .min(Comparator.comparingLong(this::absoluteWeekNumber));

            if (lastNonValidatedSubmittedTimesheet.isPresent() ) {
                // user have at least one non validated week.
                SubmittedTimesheet t = lastNonValidatedSubmittedTimesheet.get();

                return generateSubmittedTimesheets(a,(int) t.getYear(),(int) t.getWeek(), submittedTimesheets);
            } else {
                // user have all his weeks validated so return 2 first weeks
                return submittedTimesheets
                        .stream()
                        .sorted(Comparator.comparingLong(this::absoluteWeekNumber))
                        .limit(2)
                        .collect(Collectors.toList());
            }
        } else {
            // user NEVER submitted a single week
            Calendar current = Calendar.getInstance();

            current.setTime(a.getAccountCreationTime());

            return generateSubmittedTimesheets(a,current.get(Calendar.YEAR),
                    current.get(Calendar.WEEK_OF_YEAR), submittedTimesheets);

        }

    }


    List<SubmittedTimesheet> generateSubmittedTimesheets(Account a, int firstYear, int firstWeek, List<SubmittedTimesheet> submittedTimesheets) {

        List<SubmittedTimesheet> returnList = new LinkedList<>();
        long todayAbsoluteWeekNumber = absoluteWeekNumber(Calendar.getInstance());



        Calendar current = Calendar.getInstance();
        current.set(Calendar.WEEK_OF_YEAR, firstWeek);
        current.set(Calendar.YEAR, firstYear);
        while (absoluteWeekNumber(current.get(Calendar.YEAR), current.get(Calendar.WEEK_OF_YEAR)) < todayAbsoluteWeekNumber) {
            int currentWeek = current.get(Calendar.WEEK_OF_YEAR);
            int currentYear = current.get(Calendar.YEAR);
            Optional<SubmittedTimesheet> existingWeek = submittedTimesheets
                    .stream()
                    .filter(t -> t.getWeek() == currentWeek && t.getYear() == currentYear)
                    .findFirst();

            returnList.add(existingWeek.orElseGet(() -> generateWeek(a, currentYear, currentWeek)));
            current.add(Calendar.WEEK_OF_YEAR, 1);
        }

        return returnList;
    }

    private long absoluteWeekNumber(SubmittedTimesheet t) {
        return absoluteWeekNumber((int) t.getYear(), (int) t.getWeek());
    }

    private long absoluteWeekNumber(int year, int week) {
        return (year * 53) + week;
    }

    private long absoluteWeekNumber(Calendar c) {
        return absoluteWeekNumber(c.get(Calendar.YEAR), c.get(Calendar.WEEK_OF_YEAR));
    }

    private SubmittedTimesheet generateWeek(Account a, int year, int week) {
        SubmittedTimesheet t = new SubmittedTimesheet();
        t.setAccount(a);
        t.setWeek(week);
        t.setYear(year);
        t.setValidated(false);
        return t;
    }

}
