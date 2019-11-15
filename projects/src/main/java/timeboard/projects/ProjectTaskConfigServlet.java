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
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

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
            task = (Task) this.projectService.getTask(taskID);
            viewModel.getViewDatas().put("task", new TaskForm(task));
        } else {
            // New task case
            viewModel.getViewDatas().put("task", new TaskForm(task));
        }

        long projectID = Long.parseLong(request.getParameter("projectID"));
        Project project = this.projectService.getProjectByID(SecurityContext.getCurrentUser(request), projectID);

        viewModel.setTemplate("projects:details_project_tasks_config.html");
        viewModel.getViewDatas().put("project", project);
        viewModel.getViewDatas().put("tasks", this.projectService.listProjectTasks(project));
        viewModel.getViewDatas().put("taskTypes", this.projectService.listTaskType());
        viewModel.getViewDatas().put("allTaskStatus", TaskStatus.values());
        viewModel.getViewDatas().put("allProjectMilestones", this.projectService.listProjectMilestones(project));


        /* Get datas for line-chart*/
        if(task.getId() != null) {
            this.getDatasForCharts(viewModel, task);
        }

    }

    private void getDatasForCharts(ViewModel viewModel, Task task) {
        // Datas for dates (Axis X)
        String formatLocalDate = "yyyy-MM-dd";
        String formatDateToDisplay = "dd/MM/yyyy";
        LocalDate start = LocalDate.parse(new SimpleDateFormat(formatLocalDate).format(task.getStartDate()));
        LocalDate end = LocalDate.parse(new SimpleDateFormat(formatLocalDate).format(task.getEndDate()));
        List<String> listOfTaskDates = start.datesUntil(end.plusDays(1))
                .map(localDate -> localDate.format(DateTimeFormatter.ofPattern(formatDateToDisplay)))
                .collect(Collectors.toList());
        viewModel.getViewDatas().put("listOfTaskDates", listOfTaskDates);

        // Datas for effort spent (Axis Y)
        List<EffortSpent> effortSpentDB = this.projectService.getESByTaskAndPeriod(task.getId(), task.getStartDate(), task.getEndDate());
        final Double[] lastSum = {0.0};
        List<Double> effortSpent = listOfTaskDates
                .stream()
                .map(date -> effortSpentDB.stream()
                        .filter(es -> new SimpleDateFormat(formatDateToDisplay).format(es.getDate()).equals(date))
                        .map(effort -> {
                            lastSum[0] = effort.getSumPreviousValue();
                            return effort.getSumPreviousValue();
                        })
                        .findFirst().orElse(lastSum[0]))
                .collect(Collectors.toList());
        viewModel.getViewDatas().put("effortSpent", effortSpent);

        // Datas for effort estimate (Axis Y)
        List<EffortEstimate> effortEstimateDB = this.projectService.getEstimateByTask(task.getId());
        final Double[] lastEstimate = {0.0};
        List<Double> effortEstimate = listOfTaskDates
                .stream()
                .map(date -> effortEstimateDB.stream()
                        .filter(ee -> new SimpleDateFormat(formatDateToDisplay).format(ee.getDate()).equals(date))
                        .map(estimate -> {
                            lastEstimate[0] = estimate.getEstimateValue();
                            return estimate.getEstimateValue();
                        })
                        .findFirst().orElse(lastEstimate[0]))
                .collect(Collectors.toList());
        viewModel.getViewDatas().put("reEstimate", effortEstimate);
    }

    @Override
    protected void handlePost(HttpServletRequest request, HttpServletResponse response, ViewModel viewModel) throws ServletException, IOException {

        final User actor = SecurityContext.getCurrentUser(request);
        long projectID = Long.parseLong(request.getParameter("projectID"));
        Project project = this.projectService.getProjectByID(SecurityContext.getCurrentUser(request), projectID);
          Task currentTask = null;

        try {

            if (!getParameter(request, "taskID").get().isEmpty()) {
                Long taskID = Long.parseLong(request.getParameter("taskID"));
                currentTask = (Task) this.projectService.getTask(taskID);
                currentTask = this.updateTask(actor, project, currentTask, request);
            } else {
                currentTask = this.createTask(actor, project, request);
            }

            viewModel.getViewDatas().put("task", new TaskForm(currentTask));

        } catch (Exception e) {
            viewModel.getErrors().add(e);
        } finally {
            viewModel.setTemplate("projects:details_project_tasks_config.html");

            viewModel.getViewDatas().put("tasks", this.projectService.listProjectTasks(project));
            viewModel.getViewDatas().put("taskTypes", this.projectService.listTaskType());
            viewModel.getViewDatas().put("project", project);
            viewModel.getViewDatas().put("allTaskStatus", TaskStatus.values());
            viewModel.getViewDatas().put("allProjectMilestones", this.projectService.listProjectMilestones(project));

            /* Get datas for line-chart*/
            if(currentTask.getId() != null) {
                this.getDatasForCharts(viewModel, currentTask);
            }
        }
    }

    private Task createTask(User actor, Project project, HttpServletRequest request) throws ParseException {
        TaskForm taskForm = new TaskForm(request);
        Milestone milestone = taskForm.getMilestoneID() != null ? this.projectService.getMilestoneById(taskForm.getMilestoneID()) : null;

        return this.projectService.createTaskWithMilestone(actor,
                project,
                taskForm.getTaskName(),
                taskForm.getTaskComment(),
                taskForm.getStartDate(),
                taskForm.getEndDate(),
                taskForm.getEstimateWork(),
                taskForm.getTaskTypeID(),
                this.userService.findUserByID(taskForm.getAssignedUserID()),
                milestone
                );
    }

    private Task updateTask(User actor, Project project, Task currentTask, HttpServletRequest request) throws ParseException {
        TaskForm taskForm = new TaskForm(request);
        Milestone milestone = taskForm.getMilestoneID() != null ? this.projectService.getMilestoneById(taskForm.getMilestoneID()) : null;

        final TaskType taskType = this.projectService.findTaskTypeByID(taskForm.getTaskTypeID());
        currentTask.setTaskType(taskType);
        currentTask.setName(taskForm.getTaskName());
        currentTask.setComments(taskForm.getTaskComment());
        currentTask.setStartDate(taskForm.getStartDate());
        currentTask.setEndDate(taskForm.getEndDate());
        currentTask.setEstimateWork( taskForm.getEstimateWork());
        currentTask.setMilestone(milestone);
        final TaskRevision rev = new TaskRevision(actor,
                currentTask,
                currentTask.getRemainsToBeDone(),
                this.userService.findUserByID(taskForm.getAssignedUserID()),
                taskForm.getTaskStatus());

        return this.projectService.updateTaskWithMilestone(actor, currentTask, rev, milestone);
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
        private TaskStatus taskStatus;
        private Long milestoneID;

        public TaskForm(Task task){
            taskID = task.getId();
            taskType = task.getTaskType();
            milestoneID = task.getMilestone() != null ? task.getMilestone().getId() : null;
            if(task.getLatestRevision() != null) {
                estimateWork = task.getEstimateWork();
                startDate = task.getStartDate();
                endDate = task.getEndDate();
                assignedUser = task.getLatestRevision().getAssigned();
                taskName = task.getName();
                taskComment = task.getComments();
                taskStatus = task.getLatestRevision().getTaskStatus();
            }else{
                startDate = new Date();
                endDate = new Date();
                assignedUser = null;
                taskStatus = TaskStatus.PENDING;
            }
        }

        public TaskForm(HttpServletRequest request) throws ParseException {
            if(!request.getParameter("taskID").isEmpty()) {
                taskID = Long.parseLong(request.getParameter("taskID"));
            }
            taskName = request.getParameter("taskName");
            taskComment = request.getParameter("taskComments");
            startDate = new Date(DATE_FORMAT.parse(request.getParameter("taskStartDate")).getTime()+(2 * 60 * 60 * 1000) +1);
            endDate = new Date(DATE_FORMAT.parse(request.getParameter("taskEndDate")).getTime()+(2 * 60 * 60 * 1000) +1);
            estimateWork = Double.parseDouble(request.getParameter("taskEstimateWork"));
            taskStatus = request.getParameter("taskStatus") != null ? TaskStatus.valueOf(request.getParameter("taskStatus")) : TaskStatus.PENDING;

            if(request.getParameter("taskMilestoneId") != null && !request.getParameter("taskMilestoneId").isEmpty()) {
                milestoneID = Long.parseLong(request.getParameter("taskMilestoneId"));
            }

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

        public TaskStatus getTaskStatus() {
            return taskStatus;
        }

        public void setTaskStatus(TaskStatus taskStatus) {
            this.taskStatus = taskStatus;
        }

        public Long getMilestoneID() {
            return milestoneID;
        }

        public void setMilestoneID(Long milestoneID) {
            this.milestoneID = milestoneID;
        }
    }
}
