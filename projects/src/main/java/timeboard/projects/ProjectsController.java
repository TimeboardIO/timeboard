package timeboard.projects;

/*-
 * #%L
 * webui
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

import edu.emory.mathcs.backport.java.util.Collections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import timeboard.core.api.ProjectService;
import timeboard.core.api.exceptions.BusinessException;
import timeboard.core.model.Account;
import timeboard.core.model.Project;
import timeboard.core.security.TimeboardAuthentication;

import javax.persistence.PersistenceException;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;


@Controller
@RequestMapping("/projects")
public class ProjectsController {

    @Autowired
    private ProjectService projectService;


    @GetMapping
    protected String handleGet(final TimeboardAuthentication authentication, final Model model) {
        final Account actor = authentication.getDetails();
        List<Project> allActorProjects = this.projectService.listProjects(actor, authentication.getCurrentOrganization());
        Collections.reverse(allActorProjects);
        if (allActorProjects.size() > 4) {
            allActorProjects = allActorProjects.subList(0, 4);
        }
        model.addAttribute("projects", allActorProjects);
        model.addAttribute("currentOrg", authentication.getCurrentOrganization());

        return "projects.html";
    }

    @GetMapping(value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ProjectDecorator>> projectList(final TimeboardAuthentication authentication, final Model model) {
        final Account actor = authentication.getDetails();
        final List<ProjectDecorator> projects = this.projectService.listProjects(actor, authentication.getCurrentOrganization())
                .stream()
                .map(project -> new ProjectDecorator(project))
                .collect(Collectors.toList());
        return ResponseEntity.ok(projects);
    }

    @PostMapping("/create")
    protected String createProjectHandler(
            final TimeboardAuthentication authentication,
            final HttpServletRequest request,
            final RedirectAttributes attributes) throws BusinessException {
        try {
            this.projectService.createProject(
                    authentication.getCurrentOrganization(),
                    authentication.getDetails(),
                    request.getParameter("projectName"));

            attributes.addFlashAttribute("success", "Project created successfully.");
            return "redirect:/projects";
        } catch (final PersistenceException e) {
            attributes.addFlashAttribute("errorCreateProject", "The name \"" + request.getParameter("projectName")
                    + "\" is already used by another project in this organization");
            return "redirect:/projects/create";
        } catch (final Exception e) {
            attributes.addFlashAttribute("errorCreateProject", "Error while project's creation");
            return "redirect:/projects/create";
        }
    }

    @GetMapping("/create")
    protected String createFrom() {
        return "create_project.html";
    }

    @GetMapping("/{projectID}/delete")
    protected String deleteProject(final TimeboardAuthentication authentication,
                                   @PathVariable final long projectID, final RedirectAttributes attributes) throws BusinessException {

        final Project project = this.projectService.getProjectByID(authentication.getDetails(), authentication.getCurrentOrganization(), projectID);
        this.projectService.archiveProjectByID(authentication.getDetails(), project);
        attributes.addFlashAttribute("success", "Project deleted successfully.");

        return "redirect:/projects";
    }


    public class ProjectDecorator {

        private final Project project;

        public ProjectDecorator(final Project project) {
            this.project = project;
        }

        public long getID() {
            return this.project.getId();
        }

        public String getName() {
            return this.project.getName();
        }

        public String getColor() {
            return this.project.getColor();
        }

        public String getComments() {
            return this.project.getComments();
        }

        public String getMemberSize() {
            return this.project.getMembers().size() + "";
        }

    }
}