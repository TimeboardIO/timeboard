package timeboard.reports;

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
import org.springframework.web.bind.annotation.RequestMapping;
import timeboard.core.api.ProjectDashboard;
import timeboard.core.api.ProjectService;
import timeboard.core.api.exceptions.BusinessException;
import timeboard.core.model.Account;
import timeboard.core.model.Project;
import timeboard.core.model.TaskType;
import timeboard.core.security.TimeboardAuthentication;
import timeboard.projects.ProjectBaseController;

import java.util.Map;

import static timeboard.reports.ProjectDashboardController.PATH;


/**
 * Display project dashboard.
 */
@Controller
@RequestMapping("/projects/{project}" + PATH)
public class ProjectDashboardController extends ProjectBaseController {

    public static final String PATH = "/dashboard";

    @Autowired
    public ProjectService projectService;


    @GetMapping
    protected String handleGet(final TimeboardAuthentication authentication,
                               @PathVariable final Project project, final Model model) throws BusinessException {

        final Account actor = authentication.getDetails();
        final Map<TaskType, ProjectDashboard> dashboardsByType = this.projectService.projectDashboardByTaskType(actor, project);
        final ProjectDashboard dashboard = this.projectService.projectDashboard(actor, project);
        model.addAttribute("project", project);
        model.addAttribute("dashboard", dashboard);
        model.addAttribute("dashboardsByType", dashboardsByType);

        this.initModel(model, authentication, project);
        return "project_dashboard.html";
    }


}
