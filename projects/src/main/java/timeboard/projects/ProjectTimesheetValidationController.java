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
import timeboard.core.model.Account;
import timeboard.core.model.Project;
import timeboard.core.security.TimeboardAuthentication;

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

        model.addAttribute("project", project);

        return "project_timesheet_validation.html";
    }

}
