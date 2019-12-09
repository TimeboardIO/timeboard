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
import timeboard.core.model.*;
import timeboard.core.ui.TimeboardServlet;
import timeboard.core.ui.ViewModel;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;


@WebServlet(name = "ProjectTaskListServlet", urlPatterns = "/projects/tasks")
public class ProjectTaskListServlet extends TimeboardServlet {

    @Autowired
    public ProjectService projectService;


    @Override
    protected ClassLoader getTemplateResolutionClassLoader() {
        return ProjectTaskListServlet.class.getClassLoader();
    }

    @Override
    protected void handleGet(User actor, HttpServletRequest request, HttpServletResponse response, ViewModel viewModel) throws ServletException, IOException, BusinessException {

        Task task = new Task();
        if (request.getParameter("taskID") != null) {
            // Update case
            long taskID = Long.parseLong(request.getParameter("taskID"));
            task = (Task) this.projectService.getTaskByID(actor, taskID);
        }
        viewModel.getViewDatas().put("task", new TaskForm(task));

        long projectID = Long.parseLong(request.getParameter("projectID"));
        Project project = this.projectService.getProjectByID(actor, projectID);

        viewModel.setTemplate("details_project_tasks.html");
        viewModel.getViewDatas().put("project", project);
        viewModel.getViewDatas().put("tasks", this.projectService.listProjectTasks(actor, project));
        viewModel.getViewDatas().put("taskTypes", this.projectService.listTaskType());
        viewModel.getViewDatas().put("allTaskStatus", TaskStatus.values());
        viewModel.getViewDatas().put("allProjectMilestones", this.projectService.listProjectMilestones(actor, project));
        viewModel.getViewDatas().put("isProjectOwner", this.projectService.isProjectOwner(actor, project));

    }

    @Override
    protected void handlePost(User actor, HttpServletRequest request, HttpServletResponse response, ViewModel viewModel) throws ServletException, IOException, BusinessException {
        long id = Long.parseLong(request.getParameter("projectID"));
        Project project = this.projectService.getProjectByID(actor, id);

        viewModel.setTemplate("details_project_tasks.html");
        viewModel.getViewDatas().put("tasks", this.projectService.listProjectTasks(actor, project));
        viewModel.getViewDatas().put("project", project);
    }

    public static class TaskForm {
        private Long taskID;
        private String taskName;
        private String taskComment;
        private Date startDate;
        private Date endDate;
        private Double originalEstimate;
        private Long assignedUserID;
        private Long taskTypeID;
        private User assignedUser;

        private TaskType taskType;
        private TaskStatus taskStatus;
        private Long milestoneID;

        public TaskForm(Task task) {
            taskID = task.getId();
            taskType = task.getTaskType();
            milestoneID = task.getMilestone() != null ? task.getMilestone().getId() : null;

            originalEstimate = task.getOriginalEstimate();
            startDate = task.getStartDate();
            endDate = task.getEndDate();
            assignedUser = task.getAssigned();
            taskName = task.getName();
            taskComment = task.getComments();
            taskStatus = task.getTaskStatus();
            taskStatus = TaskStatus.PENDING;

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

        public Double getOriginalEstimate() {
            return originalEstimate;
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

        public TaskType getTaskType() {
            return taskType;
        }

        public void setTaskType(TaskType taskType) {
            this.taskType = taskType;
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
