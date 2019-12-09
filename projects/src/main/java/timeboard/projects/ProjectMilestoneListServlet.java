package timeboard.projects;

/*-
 * #%L
 * projects
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
import timeboard.core.api.ProjectService;
import timeboard.core.api.exceptions.BusinessException;
import timeboard.core.model.Account;
import timeboard.core.model.Project;
import timeboard.core.ui.TimeboardServlet;
import timeboard.core.ui.ViewModel;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


@WebServlet(name = "ProjectMilestoneListServlet", urlPatterns = "/projects/milestones")
public class ProjectMilestoneListServlet extends TimeboardServlet {

    @Autowired
    public ProjectService projectService;


    @Override
    protected ClassLoader getTemplateResolutionClassLoader() {
        return ProjectMilestoneListServlet.class.getClassLoader();
    }

    @Override
    protected void handleGet(Account actor, HttpServletRequest request, HttpServletResponse response, ViewModel viewModel) throws ServletException, IOException, BusinessException {
        long id = Long.parseLong(request.getParameter("projectID"));
        Project project = this.projectService.getProjectByID(actor, id);

        viewModel.setTemplate("details_project_milestones.html");
        viewModel.getViewDatas().put("milestones", this.projectService.listProjectMilestones(actor, project));
        viewModel.getViewDatas().put("project", project);

    }

    @Override
    protected void handlePost(Account actor, HttpServletRequest request, HttpServletResponse response, ViewModel viewModel) throws ServletException, IOException, BusinessException {
        long id = Long.parseLong(request.getParameter("projectID"));
        Project project = this.projectService.getProjectByID(actor, id);

        viewModel.setTemplate("details_project_milestones.html");
        viewModel.getViewDatas().put("milestones", this.projectService.listProjectMilestones(actor, project));
        viewModel.getViewDatas().put("project", project);
    }
}
