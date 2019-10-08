package kronops.projects;

/*-
 * #%L
 * projects
 * %%
 * Copyright (C) 2019 Kronops
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

import kronops.core.api.ProjectService;
import kronops.core.api.UserService;
import kronops.core.api.exceptions.BusinessException;
import kronops.core.model.Project;
import kronops.core.model.Task;
import kronops.core.ui.KronopsServlet;
import kronops.core.ui.ViewModel;
import kronops.security.SecurityContext;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;

@Component(
        service = Servlet.class,
        scope = ServiceScope.PROTOTYPE,
        property = {
                "osgi.http.whiteboard.servlet.pattern=/projects/tasks/config",
                "osgi.http.whiteboard.context.select=(osgi.http.whiteboard.context.name=kronops)"
        }
)
public class ProjectTaskConfigServlet extends KronopsServlet {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    @Reference
    public ProjectService projectService;

    @Reference
    public UserService userService;


    @Override
    protected ClassLoader getTemplateResolutionClassLoader() {
        return ProjectTaskConfigServlet.class.getClassLoader();
    }

    @Override
    protected void handleGet(HttpServletRequest request, HttpServletResponse response, ViewModel viewModel) throws ServletException, IOException {
        if (request.getParameter("taskID") != null) {
            // Update case
            long taskID = Long.parseLong(request.getParameter("taskID"));
            Task task = this.projectService.getTask(taskID);
            viewModel.getViewDatas().put("task", task);
        } else {
            // New task case
            viewModel.getViewDatas().put("task", new Task());
        }

        long projectID = Long.parseLong(request.getParameter("projectID"));
        Project project = this.projectService.getProjectByID(SecurityContext.getCurrentUser(request), projectID);

        viewModel.setTemplate("details_project_tasks_config.html");
        viewModel.getViewDatas().put("project", project);
        viewModel.getViewDatas().put("tasks", this.projectService.listProjectTasks(project));
        viewModel.getViewDatas().put("taskTypes", this.projectService.listTaskType());
    }

    @Override
    protected void handlePost(HttpServletRequest request, HttpServletResponse response, ViewModel viewModel) throws ServletException, IOException {

        long projectID = Long.parseLong(request.getParameter("projectID"));
        Project project = this.projectService.getProjectByID(SecurityContext.getCurrentUser(request), projectID);
        final Task currentTask;

        if (!getParameter(request, "taskID").get().isEmpty()) {
            Long taskID = Long.parseLong(request.getParameter("taskID"));
            currentTask = this.projectService.getTask(taskID);
        } else {
            currentTask = new Task();
        }

        try {

            if (!request.getParameter("taskAssigned").isEmpty()) {
                Long taskAssigned = Long.parseLong(request.getParameter("taskAssigned"));
                currentTask.setAssigned(this.userService.findUserByID(taskAssigned));
            }

            if (!request.getParameter("taskTypeID").isEmpty()) {
                Long taskTypeID = Long.parseLong(request.getParameter("taskTypeID"));
                currentTask.setTaskType(this.projectService.findTaskTypeByID(taskTypeID));
            }

            Date taskStartDate = DATE_FORMAT.parse(request.getParameter("taskStartDate"));
            Date taskEndDate = DATE_FORMAT.parse(request.getParameter("taskEndDate"));

            if(taskEndDate.before(taskStartDate)){
                viewModel.getErrors().add(new BusinessException("The end date must be after the start date"));
            }

            currentTask.setName(request.getParameter("taskName"));
            currentTask.setStartDate(taskStartDate);
            currentTask.setEndDate(taskEndDate);
            currentTask.setProject(project);
            currentTask.setComments(request.getParameter("taskComments"));
            currentTask.setEstimateWork(Double.parseDouble(request.getParameter("taskEstimateWork")));


            if(viewModel.getErrors().isEmpty()) {
                saveOrUpdateTask(getParameter(request, "taskID"), viewModel, project, currentTask);
            }

        } catch (Exception e) {
            viewModel.getErrors().add(e);
        } finally {
            viewModel.setTemplate("details_project_tasks_config.html");

            viewModel.getViewDatas().put("task", currentTask);
            viewModel.getViewDatas().put("tasks", this.projectService.listProjectTasks(project));
            viewModel.getViewDatas().put("taskTypes", this.projectService.listTaskType());
            viewModel.getViewDatas().put("project", project);
        }
    }

    private void saveOrUpdateTask(Optional<String> taskID, ViewModel viewModel, Project project, Task currentTask) {
        if (!taskID.get().isEmpty()) {
            viewModel.getViewDatas().put("task", this.projectService.updateTask(currentTask));
        } else {
            viewModel.getViewDatas().put("task", this.projectService.createTask(project, currentTask));
        }
    }
}
