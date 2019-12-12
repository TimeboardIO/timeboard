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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import timeboard.core.api.ProjectService;
import timeboard.core.api.exceptions.BusinessException;
import timeboard.core.model.Account;
import timeboard.core.model.Project;
import timeboard.core.ui.UserInfo;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


@Controller
@RequestMapping("/org/{orgID}/projects")
public class ProjectsController {


    @Autowired
    private ProjectService projectService;

    @Autowired
    private UserInfo userInfo;

    @GetMapping
    protected String handleGet(Model model) {
        final Account actor = this.userInfo.getCurrentAccount();
        model.addAttribute("projects", this.projectService.listProjects(actor));
        return "projects.html";
    }

    @PostMapping("/create")
    protected String handlePost(@PathVariable Long orgID, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, BusinessException {
        final Account actor = this.userInfo.getCurrentAccount();
        this.projectService.createProject(actor, request.getParameter("projectName"));
        return "redirect:/org/" + orgID + "/projects";
    }

    @GetMapping("/create")
    protected String createFrom() throws ServletException, IOException {
        return "create_project.html";
    }

    @GetMapping("/{projectID}/delete")
    protected String deleteProject(@PathVariable Long orgID, @PathVariable long projectID) throws ServletException, IOException, BusinessException {
        final Project project = this.projectService.getProjectByID(this.userInfo.getCurrentAccount(), projectID);
        this.projectService.archiveProjectByID(this.userInfo.getCurrentAccount(), project);
        return "redirect:/org/" + orgID + "/projects";
    }


}