package timeboard.projects;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import timeboard.core.api.ProjectService;
import timeboard.core.api.exceptions.BusinessException;
import timeboard.core.model.Account;
import timeboard.core.model.Project;
import timeboard.core.ui.UserInfo;


@Controller
@RequestMapping("/projects/{projectID}/tags")
public class ProjectTagsController {

    @Autowired
    private ProjectService projectService;

    @Autowired
    private UserInfo userInfo;

    @GetMapping
    public String display(@PathVariable Long projectID, Model model) throws BusinessException {

        final Account actor = this.userInfo.getCurrentAccount();
        final Project project = this.projectService.getProjectByID(actor, projectID);

        model.addAttribute("project", project);

        return "project_tags";
    }

}
