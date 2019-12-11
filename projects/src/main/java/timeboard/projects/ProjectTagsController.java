package timeboard.projects;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;


@Controller
@RequestMapping("/projects/{projectID}/tags")
public class ProjectTagsController {
    
    @GetMapping
    public String display(){
        return "project_tags";
    }

}
