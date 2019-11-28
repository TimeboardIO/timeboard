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

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.osgi.service.component.annotations.*;
import timeboard.core.api.ProjectImportService;
import timeboard.core.api.ProjectService;
import timeboard.core.api.UserService;
import timeboard.core.api.exceptions.BusinessException;
import timeboard.core.model.*;

import timeboard.core.ui.TimeboardServlet;
import timeboard.core.ui.ViewModel;

@Component(
        service = Servlet.class,
        scope = ServiceScope.PROTOTYPE,
        property = {
                "osgi.http.whiteboard.servlet.pattern=/projects/import",
                "osgi.http.whiteboard.context.select=(osgi.http.whiteboard.context.name=timeboard)"
        }
)
public class ProjectImportServlet extends TimeboardServlet {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Reference
    private ProjectService projectService;

    @Reference
    private UserService userService;

    @Reference(
            policyOption = ReferencePolicyOption.GREEDY,
            cardinality = ReferenceCardinality.MULTIPLE,
            collectionType = CollectionType.SERVICE
    )
    private List<ProjectImportService> projectImportServlets;

    @Override
    protected ClassLoader getTemplateResolutionClassLoader() {
        return ProjectExportServlet.class.getClassLoader();
    }

    @Override
    protected void handlePost(User actor, HttpServletRequest req, HttpServletResponse resp, ViewModel viewModel) throws ServletException, IOException, BusinessException {
        final long projectID = Long.parseLong(req.getParameter("projectID"));
        RequestDispatcher requestDispatcher = req.getRequestDispatcher("/projects/config?projectID=" + projectID);
        requestDispatcher.forward(req, resp);
    }

    @Override
    protected void handleGet(User actor, HttpServletRequest req, HttpServletResponse resp, ViewModel viewModel) throws ServletException, IOException, BusinessException {

        final String type = req.getParameter("type");
        final long projectID = Long.parseLong(req.getParameter("projectID"));

        String message = null;

        ImportResponse importResponse = new ImportResponse();

        final Optional<ProjectImportService> optionalService = this.projectImportServlets.stream()
                .filter(projectImportService -> projectImportService.getServiceName().equals(type))
                .findFirst();

        if (optionalService.isPresent()) {
            final ProjectImportService importPlugin = optionalService.get();
            try {
                ProjectImportBackgroundTasks
                        .getInstance()
                        .importInBackground(actor, () -> {
                            try {
                                final Project project = projectService.getProjectByID(actor, projectID);

                                final List<ProjectImportService.RemoteTask> remoteTasks;

                                remoteTasks = importPlugin.getRemoteTasks(actor, projectID);

                                remoteTasks.stream().forEach(task -> mergeAssignee(userService, importPlugin.getServiceName(), task));

                                final List<ProjectImportService.RemoteTask> newTasks = new ArrayList<>();
                                for (ProjectImportService.RemoteTask task1 : remoteTasks) {
                                    if (isNewTask(actor, projectID, task1)) {
                                        newTasks.add(task1);
                                    }
                                }

                                final List<ProjectImportService.RemoteTask> updatedTasks = new ArrayList<>();
                                for (ProjectImportService.RemoteTask task1 : remoteTasks) {
                                    if (isUpdated(actor, projectID, task1)) {
                                        updatedTasks.add(task1);
                                    }
                                }


                                newTasks.forEach(task ->
                                        {
                                            String taskName = task.getTitle();
                                            if (taskName.length()>=100) {
                                                taskName = taskName.substring(0, 99);
                                            }
                                            String taskComment = task.getComments();
                                            Date startDate = task.getStartDate();
                                            Date endDate = task.getStopDate();
                                            double OE = 0;
                                            Long taskTypeID = null;
                                            User assignedUserID = this.userService.findUserByID(task.getLocalUserID());
                                            String origin = task.getOrigin();
                                            String remotePath = null;
                                            Long remoteId = task.getID();
                                            Milestone milestone = null;
                                            projectService.createTask(actor, project, taskName, taskComment, startDate, endDate, OE, taskTypeID, assignedUserID, origin, remotePath, String.valueOf(remoteId), milestone);
                                        }
                                );

                                for (ProjectImportService.RemoteTask remoteTask : updatedTasks) {
                                    Task taskToUpdate = (Task) projectService.getTaskByID(actor, remoteTask.getID());
                                    taskToUpdate.setName(remoteTask.getTitle());
                                    projectService.updateTask(actor, taskToUpdate);
                                }

                            } catch (BusinessException e) {
                                e.printStackTrace();
                            }

                        });
            } catch (Exception e) {
                e.printStackTrace();
            }


        } else {
            importResponse.getErrors().add(new BusinessException("Missing " + type + " Service"));
        }

        RequestDispatcher requestDispatcher = req.getRequestDispatcher("/projects/config?projectID=" + projectID);
        req.setAttribute("errors", importResponse.getErrors());
        req.setAttribute("importSuccess", message);
        requestDispatcher.forward(req, resp);
    }



    private boolean isUpdated(User actor,long projectID, ProjectImportService.RemoteTask task) throws BusinessException {
        return !this.isNewTask(actor, projectID, task);
    }

    private boolean isNewTask(User actor, long projectID, ProjectImportService.RemoteTask task) throws BusinessException{
        AbstractTask existingTask = this.projectService.getTaskByID(actor, task.getID());
        return existingTask == null;
    }

    private void mergeAssignee(UserService userService, String externalID, ProjectImportService.RemoteTask task) {
        final User remoteUser = userService.findUserByExternalID(externalID, task.getUserName());
        if(remoteUser != null){
            task.setLocalUserID(remoteUser.getId());
        }
    }

    public static class ImportResponse implements Serializable {

        private final List<Exception> errors;

        public ImportResponse() {
            this.errors = new ArrayList<>();
        }

        public List<Exception> getErrors() {
            return errors;
        }

    }

}
