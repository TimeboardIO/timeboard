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
import timeboard.core.api.ProjectImportService;
import timeboard.core.api.ProjectService;
import timeboard.core.api.exceptions.BusinessException;
import timeboard.core.model.Project;
import timeboard.core.model.Task;
import timeboard.core.model.TaskRevision;
import timeboard.core.model.User;
import timeboard.security.SecurityContext;
import org.osgi.service.component.annotations.*;

import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Serializable;
import java.rmi.Remote;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component(
        service = Servlet.class,
        scope = ServiceScope.PROTOTYPE,
        property = {
                "osgi.http.whiteboard.servlet.pattern=/projects/import",
                "osgi.http.whiteboard.context.select=(osgi.http.whiteboard.context.name=timeboard)"
        }
)
public class ProjectImportServlet extends HttpServlet {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Reference
    private ProjectService projectService;

    @Reference(
            policyOption = ReferencePolicyOption.GREEDY,
            cardinality = ReferenceCardinality.MULTIPLE,
            collectionType = CollectionType.SERVICE
    )
    private List<ProjectImportService> projectImportServlets;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        final long projectID = Long.parseLong(req.getParameter("projectID"));
        RequestDispatcher requestDispatcher = req.getRequestDispatcher("/projects/config?projectID="+projectID);
        requestDispatcher.forward(req, resp);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        final String type = req.getParameter("type");
        final long projectID = Long.parseLong(req.getParameter("projectID"));
        final User actor = SecurityContext.getCurrentUser(req);
        final Project project = this.projectService.getProjectByID(actor, projectID);

        String message = null;

        ImportResponse importResponse = new ImportResponse();

        final Optional<ProjectImportService> optionalService = this.projectImportServlets.stream()
                .filter(projectImportService -> projectImportService.getServiceName().equals(type))
                .findFirst();

        if(optionalService.isPresent()){
            try {
                message = optionalService.get().importTasksToProject(SecurityContext.getCurrentUser(req), projectID);

                final ProjectImportService importPlugin = optionalService.get();

                final List<ProjectImportService.RemoteTask> remoteTasks = importPlugin.getRemoteTasks(SecurityContext.getCurrentUser(req), projectID);

                remoteTasks.stream().forEach(task -> mergeAssignee(projectID, task));

                final List<ProjectImportService.RemoteTask> newTasks = remoteTasks.stream()
                        .filter(task -> isNewTask(projectID, task)).collect(Collectors.toList());

                final List<TaskRevision> updatedTasks = remoteTasks.stream()
                        .filter(task -> isUpdated(projectID, task)).map(remoteTask -> remoteTask.toTaskRevision()).collect(Collectors.toList());

                final List<Long> deletedTasks = remoteTasks.stream()
                        .filter(task -> isDeleted(projectID, task)).map(remoteTask -> remoteTask.getID()).collect(Collectors.toList());

                newTasks.forEach(task ->
                        {
                            String taskName = null;
                            String taskComment = null;
                            Date startDate = null;
                            Date endDate = null;
                            double OE = 0;
                            Long taskTypeID = null;
                            User assignedUserID = null;
                            String origin = null;
                            String remotePath = null;
                            Long remoteId = null;
                            this.projectService.createTask(actor, project, taskName, taskComment, startDate, endDate, OE, taskTypeID, assignedUserID, origin, remotePath, remoteId);
                        }
                );

                deletedTasks.forEach(taskID -> {
                    try {
                        this.projectService.deleteTaskByID(actor, taskID);
                    } catch (BusinessException e) {

                    }
                });


            } catch (BusinessException e) {
                importResponse.getErrors().add(e);
            }
        }else{
            importResponse.getErrors().add(new BusinessException("Missing "+type+" Service"));
        }


        RequestDispatcher requestDispatcher = req.getRequestDispatcher("/projects/config?projectID="+projectID);
        req.setAttribute("errors", importResponse.getErrors());
        req.setAttribute("importSuccess", message);
        requestDispatcher.forward(req, resp);
    }

    private boolean isDeleted(long projectID, ProjectImportService.RemoteTask task) {
        return true;
    }

    private boolean isUpdated(long projectID, ProjectImportService.RemoteTask task) {
        return true;
    }

    private boolean isNewTask(long projectID, ProjectImportService.RemoteTask task) {
        return true;
    }

    private void mergeAssignee(long projectID, ProjectImportService.RemoteTask task) {
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
