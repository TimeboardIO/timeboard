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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import timeboard.core.api.ProjectImportService;
import timeboard.core.api.ProjectService;
import timeboard.core.api.UserService;
import timeboard.core.api.exceptions.BusinessException;
import timeboard.core.model.*;
import timeboard.core.ui.UserInfo;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;


@Controller
@RequestMapping("/org/{orgID}/projects/import")
public class ProjectImportController {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Autowired
    private ProjectService projectService;

    @Autowired
    private UserService userService;

    @Autowired
    private UserInfo userInfo;

    @Autowired(
            required = false
    )
    private List<ProjectImportService> projectImportServices;


    @PostMapping
    protected void handlePost(@PathVariable Long orgID, HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException, BusinessException {
        final long projectID = Long.parseLong(req.getParameter("projectID"));
        RequestDispatcher requestDispatcher = req.getRequestDispatcher("/org/" + orgID + "/projects/config?projectID=" + projectID);
        requestDispatcher.forward(req, resp);
    }

    @GetMapping
    protected void handleGet(@PathVariable Long orgID, HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException, BusinessException {

        Account actor = this.userInfo.getCurrentAccount();
        final String type = req.getParameter("type");
        final long projectID = Long.parseLong(req.getParameter("projectID"));

        String message = null;

        ImportResponse importResponse = new ImportResponse();

        final Optional<ProjectImportService> optionalService = this.projectImportServices.stream()
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


                                newTasks.forEach(task -> {
                                            String taskName = task.getTitle();
                                            if (taskName.length() >= 100) {
                                                taskName = taskName.substring(0, 99);
                                            }
                                            String taskComment = task.getComments();
                                            Date startDate = task.getStartDate();
                                            Date endDate = task.getStopDate();
                                            double originaEstimate = 0;
                                            Long taskTypeID = null;
                                            Account assignedAccountID = this.userService.findUserByID(task.getLocalUserID());
                                            String origin = task.getOrigin();
                                            String remotePath = null;
                                            Long remoteId = task.getId();
                                            Milestone milestone = null;
                                            projectService.createTask(actor, project, taskName, taskComment,
                                                    startDate, endDate, originaEstimate, taskTypeID, assignedAccountID, origin,
                                                    remotePath, String.valueOf(remoteId), milestone);
                                        }
                                );

                                for (ProjectImportService.RemoteTask remoteTask : updatedTasks) {
                                    Task taskToUpdate = (Task) projectService.getTaskByID(actor, remoteTask.getId());
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

        RequestDispatcher requestDispatcher = req.getRequestDispatcher("/org/" + orgID + "/projects/config?projectID=" + projectID);
        req.setAttribute("errors", importResponse.getErrors());
        req.setAttribute("importSuccess", message);
        requestDispatcher.forward(req, resp);
    }


    private boolean isUpdated(Account actor, long projectID, ProjectImportService.RemoteTask task) throws BusinessException {
        return !this.isNewTask(actor, projectID, task);
    }

    private boolean isNewTask(Account actor, long projectID, ProjectImportService.RemoteTask task) throws BusinessException {
        AbstractTask existingTask = this.projectService.getTaskByID(actor, task.getId());
        return existingTask == null;
    }

    private void mergeAssignee(UserService userService, String externalID, ProjectImportService.RemoteTask task) {
        final Account remoteAccount = userService.findUserByExternalID(externalID, task.getUserName());
        if (remoteAccount != null) {
            task.setLocalUserID(remoteAccount.getId());
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
