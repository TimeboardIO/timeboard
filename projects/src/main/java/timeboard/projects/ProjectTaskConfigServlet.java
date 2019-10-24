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

import timeboard.core.api.ProjectService;
import timeboard.core.api.UserService;
import timeboard.core.model.*;
import timeboard.core.ui.TimeboardServlet;
import timeboard.core.ui.ViewModel;
import timeboard.security.SecurityContext;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component(
        service = Servlet.class,
        scope = ServiceScope.PROTOTYPE,
        property = {
                "osgi.http.whiteboard.servlet.pattern=/projects/tasks/config",
                "osgi.http.whiteboard.context.select=(osgi.http.whiteboard.context.name=timeboard)"
        }
)
public class ProjectTaskConfigServlet extends TimeboardServlet {

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
        Task task = new Task();
        if (request.getParameter("taskID") != null) {
            // Update case
            long taskID = Long.parseLong(request.getParameter("taskID"));
            task = this.projectService.getTask(taskID);
            viewModel.getViewDatas().put("task", new TaskForm(task));
        } else {
            // New task case
            viewModel.getViewDatas().put("task", new TaskForm(task));
        }

        long projectID = Long.parseLong(request.getParameter("projectID"));
        Project project = this.projectService.getProjectByID(SecurityContext.getCurrentUser(request), projectID);

        viewModel.setTemplate("details_project_tasks_config.html");
        viewModel.getViewDatas().put("project", project);
        viewModel.getViewDatas().put("tasks", this.projectService.listProjectTasks(project));
        viewModel.getViewDatas().put("taskTypes", this.projectService.listTaskType());

        LocalDate start = LocalDate.parse(task.getLatestRevision().getStartDate().toString());
        LocalDate end = LocalDate.parse(task.getLatestRevision().getEndDate().toString());
        List<String> listOfTaskDates = start.datesUntil(end.plusDays(1))
                .map(localDate -> localDate.toString())
                .collect(Collectors.toList());
        viewModel.getViewDatas().put("listOfTaskDates", listOfTaskDates);

        List<EffortSpent> effortSpentList = this.projectService.getESByTaskAndPeriod(task.getId(), task.getLatestRevision().getStartDate(), task.getLatestRevision().getEndDate());
        List<Double> esList = listOfTaskDates
                .stream()
                .map(date -> {
                    return effortSpentList
                            .stream()
                            .filter(effortSpent -> effortSpent.getDate().toString().equals(date))
                            .map(effort -> effort.getSumPreviousValue())
                            .findFirst()
                            .orElse(0.0);
                })
                .collect(Collectors.toList());
        viewModel.getViewDatas().put("effortSpent", esList);

    }

    @Override
    protected void handlePost(HttpServletRequest request, HttpServletResponse response, ViewModel viewModel) throws ServletException, IOException {

        final User actor = SecurityContext.getCurrentUser(request);
        long projectID = Long.parseLong(request.getParameter("projectID"));
        Project project = this.projectService.getProjectByID(SecurityContext.getCurrentUser(request), projectID);
          Task currentTask;

        try {

            if (!getParameter(request, "taskID").get().isEmpty()) {
                Long taskID = Long.parseLong(request.getParameter("taskID"));
                currentTask = this.projectService.getTask(taskID);
                currentTask = updateTask(actor, project, currentTask, request);
            } else {
                currentTask = createTask(actor, project, request);
            }

            viewModel.getViewDatas().put("task", new TaskForm(currentTask));


        } catch (Exception e) {
            viewModel.getErrors().add(e);
        } finally {
            viewModel.setTemplate("details_project_tasks_config.html");

            viewModel.getViewDatas().put("tasks", this.projectService.listProjectTasks(project));
            viewModel.getViewDatas().put("taskTypes", this.projectService.listTaskType());
            viewModel.getViewDatas().put("project", project);
        }
    }

    private Task createTask(User actor, Project project, HttpServletRequest request) throws ParseException {

        TaskForm taskForm = new TaskForm(request);



        return this.projectService.createTask(actor,
                project,
                taskForm.getTaskName(),
                taskForm.getTaskComment(),
                taskForm.getStartDate(),
                taskForm.getEndDate(),
                taskForm.getEstimateWork(),
                taskForm.getTaskTypeID(),
                this.userService.findUserByID(taskForm.getAssignedUserID())
                );
    }

    private Task updateTask(User actor, Project project, Task currentTask, HttpServletRequest request) throws ParseException {
        TaskForm taskForm = new TaskForm(request);

        final TaskType taskType = this.projectService.findTaskTypeByID(taskForm.getTaskTypeID());
        if(taskType != null) {
            currentTask.setTaskType(taskType);
        }
        final TaskRevision rev = new TaskRevision(actor,
                currentTask,
                taskForm.getTaskName(),
                taskForm.getTaskComment(),
                taskForm.getStartDate(),
                taskForm.getEndDate(),
                taskForm.getEstimateWork(),
                currentTask.getRemainsToBeDone(),
                this.userService.findUserByID(taskForm.getAssignedUserID()));


        return this.projectService.updateTask(actor, currentTask, rev);
    }





    public static class TaskForm {
        private Long taskID;
        private String taskName;
        private String taskComment;
        private Date startDate;
        private Date endDate;
        private Double estimateWork;
        private Long assignedUserID;
        private Long taskTypeID;
        private User assignedUser;
        private TaskType taskType;

        public TaskForm(Task task){
            taskID = task.getId();
            taskType = task.getTaskType();
            if(task.getLatestRevision() != null) {
                estimateWork = task.getLatestRevision().getEstimateWork();
                startDate = task.getLatestRevision().getStartDate();
                endDate = task.getLatestRevision().getEndDate();
                assignedUser = task.getLatestRevision().getAssigned();
                taskName = task.getLatestRevision().getName();
                taskComment = task.getLatestRevision().getComments();
            }else{
                startDate = new Date();
                endDate = new Date();
                assignedUser = null;
            }
        }

        public TaskForm(HttpServletRequest request) throws ParseException {
            if(!request.getParameter("taskID").isEmpty()) {
                taskID = Long.parseLong(request.getParameter("taskID"));
            }
            taskName = request.getParameter("taskName");
            taskComment = request.getParameter("taskComments");
            startDate = DATE_FORMAT.parse(request.getParameter("taskStartDate"));
            endDate = DATE_FORMAT.parse(request.getParameter("taskEndDate"));
            estimateWork = Double.parseDouble(request.getParameter("taskEstimateWork"));

            if(request.getParameter("taskTypeID") != null &&  !request.getParameter("taskTypeID").isEmpty()) {
                taskTypeID = Long.parseLong(request.getParameter("taskTypeID"));
            }

            if(request.getParameter("taskAssigned") != null && !request.getParameter("taskAssigned").isEmpty()) {
                assignedUserID = Long.parseLong(request.getParameter("taskAssigned"));
            }
        }

        public Long getTaskID() {
            return taskID;
        }

        public void setTaskID(Long taskID) {
            this.taskID = taskID;
        }

        public String getTaskName() {
            return taskName;
        }

        public String getTaskComment() {
            return taskComment;
        }

        public Date getStartDate() {
            return startDate;
        }

        public Date getEndDate() {
            return endDate;
        }

        public Double getEstimateWork() {
            return estimateWork;
        }

        public Long getAssignedUserID() {
            return assignedUserID;
        }

        public void setAssignedUserID(Long assignedUserID) {
            this.assignedUserID = assignedUserID;
        }

        public Long getTaskTypeID() {
            return taskTypeID;
        }

        public void setTaskTypeID(Long taskTypeID) {
            this.taskTypeID = taskTypeID;
        }

        public User getAssignedUser() {
            return assignedUser;
        }

        public void setAssignedUser(User assignedUser) {
            this.assignedUser = assignedUser;
        }
    }
}
