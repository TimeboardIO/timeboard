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

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;
import timeboard.core.api.ProjectService;
import timeboard.core.model.Milestone;
import timeboard.core.model.Project;
import timeboard.core.model.Task;
import timeboard.core.ui.TimeboardServlet;
import timeboard.core.ui.ViewModel;
import timeboard.security.SecurityContext;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component(
        service = Servlet.class,
        scope = ServiceScope.PROTOTYPE,
        property = {
                "osgi.http.whiteboard.servlet.pattern=/projects/milestones/links-config",
                "osgi.http.whiteboard.context.select=(osgi.http.whiteboard.context.name=timeboard)"
        }
)
public class ProjectMilestoneLinksConfigServlet extends TimeboardServlet {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    @Reference
    public ProjectService projectService;


    @Override
    protected ClassLoader getTemplateResolutionClassLoader() {
        return ProjectMilestoneLinksConfigServlet.class.getClassLoader();
    }

    @Override
    protected void handleGet(HttpServletRequest request, HttpServletResponse response, ViewModel viewModel) throws ServletException, IOException {
        if (request.getParameter("milestoneID") != null) {
            long milestoneID = Long.parseLong(request.getParameter("milestoneID"));
            Milestone milestone = this.projectService.getMilestoneById(milestoneID);
            viewModel.getViewDatas().put("milestone", milestone);
            viewModel.getViewDatas().put("taskIdsByMilestone", this.projectService.listTaskIdsByMilestone(milestone));
        }

        long projectID = Long.parseLong(request.getParameter("projectID"));
        Project project = this.projectService.getProjectByID(SecurityContext.getCurrentUser(request), projectID);

        viewModel.setTemplate("projects:details_project_milestones_links_config.html");
        viewModel.getViewDatas().put("project", project);
        viewModel.getViewDatas().put("milestones", this.projectService.listProjectMilestones(project));
        viewModel.getViewDatas().put("allProjectTasks", this.projectService.listProjectTasks(project));

    }

    @Override
    protected void handlePost(HttpServletRequest request, HttpServletResponse response, ViewModel viewModel) throws ServletException, IOException {

        long projectID = Long.parseLong(request.getParameter("projectID"));
        Project project = this.projectService.getProjectByID(SecurityContext.getCurrentUser(request), projectID);
        Milestone currentMilestone = null;

        try {

            if (!getParameter(request, "milestoneID").get().isEmpty()) {
                Long milestoneID = Long.parseLong(request.getParameter("milestoneID"));
                currentMilestone = this.projectService.getMilestoneById(milestoneID);
                currentMilestone = addMilestoneToTask(currentMilestone, request);
               // currentMilestone = addTasksToMilestone(currentMilestone, request);
            }

            viewModel.getViewDatas().put("milestone", currentMilestone);


        } catch (Exception e) {
            viewModel.getErrors().add(e);
        } finally {
            viewModel.setTemplate("projects:details_project_milestones_links_config.html");

            viewModel.getViewDatas().put("project", project);
            viewModel.getViewDatas().put("milestones", this.projectService.listProjectMilestones(project));
            viewModel.getViewDatas().put("allProjectTasks", this.projectService.listProjectTasks(project));
            viewModel.getViewDatas().put("taskIdsByMilestone", this.projectService.listTaskIdsByMilestone(currentMilestone));
        }
    }

    //TODO: To Fix
    /*private Milestone addTasksToMilestone(Milestone currentMilestone, HttpServletRequest request) {
        String[] taskIds = request.getParameterValues("taskSelected");
        Set<Task> tasks = Arrays.stream(taskIds).map(id -> {
                return this.projectService.getTask(Long.valueOf(id));
        }).collect(Collectors.toSet());
        return this.projectService.addTasksToMilestone(currentMilestone, tasks);
    }*/


    //TODO: To Delete
    private Milestone addMilestoneToTask(Milestone currentMilestone, HttpServletRequest request) {
        Milestone newMilestone = currentMilestone;
        String[] taskIds = request.getParameterValues("taskSelected");
       for(int i = 0; i< taskIds.length; i++){
            newMilestone =  this.projectService.addMilestoneToTask(newMilestone, taskIds[i]);
        };
       return newMilestone;
    }


}
