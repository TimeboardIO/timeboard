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
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import timeboard.core.api.DataTableService;
import timeboard.core.api.ProjectService;
import timeboard.core.api.exceptions.BusinessException;
import timeboard.core.model.*;
import timeboard.core.ui.TimeboardServlet;
import timeboard.core.ui.UserInfo;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;


@Controller
@RequestMapping("/org/{orgID}/projects/{projectID}")
public class ProjectTasksController extends TimeboardServlet {

    @Autowired
    public ProjectService projectService;

    @Autowired
    public UserInfo userInfo;

    @Autowired
    public DataTableService dataTableService;

    @Override
    protected ClassLoader getTemplateResolutionClassLoader() {
        return ProjectTasksController.class.getClassLoader();
    }

    @GetMapping("/org/{orgID}/tasks")
    protected String listTasks(@PathVariable Long projectID, Model model) throws ServletException, IOException, BusinessException {

        final Account actor = this.userInfo.getCurrentAccount();

        Task task = new Task();

        model.addAttribute("task", new TaskForm(task));

        final Project project = this.projectService.getProjectByID(actor, projectID);

        fillModel(model, actor, project);

        return "details_project_tasks.html";
    }

    private void fillModel(Model model, Account actor, Project project) throws BusinessException {
        model.addAttribute("project", project);
        model.addAttribute("tasks", this.projectService.listProjectTasks(actor, project));
        model.addAttribute("taskTypes", this.projectService.listTaskType());
        model.addAttribute("allTaskStatus", TaskStatus.values());
        model.addAttribute("allProjectMilestones", this.projectService.listProjectMilestones(actor, project));
        model.addAttribute("isProjectOwner", this.projectService.isProjectOwner(actor, project));
        model.addAttribute("dataTableService", this.dataTableService);
    }

    @GetMapping("/org/{orgID}/tasks/{taskID}")
    protected String editTasks(@PathVariable Long projectID, @PathVariable Long taskID, Model model) throws ServletException, IOException, BusinessException {

        final Account actor = this.userInfo.getCurrentAccount();

        final Task task = (Task) this.projectService.getTaskByID(actor, taskID);

        model.addAttribute("task", new TaskForm(task));

        final Project project = this.projectService.getProjectByID(actor, projectID);

        fillModel(model, actor, project);

        return "details_project_tasks.html";
    }

    @PostMapping("/org/{orgID}/tasks")
    protected String handlePost(HttpServletRequest request, HttpServletResponse response, Model model) throws ServletException, IOException, BusinessException {
        Account actor = this.userInfo.getCurrentAccount();

        long id = Long.parseLong(request.getParameter("projectID"));
        Project project = this.projectService.getProjectByID(actor, id);

        model.addAttribute("tasks", this.projectService.listProjectTasks(actor, project));
        model.addAttribute("project", project);
        return "details_project_tasks.html";
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
        private Account assignedAccount;

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
            assignedAccount = task.getAssigned();
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

        public Account getAssignedAccount() {
            return assignedAccount;
        }

        public void setAssignedAccount(Account assignedAccount) {
            this.assignedAccount = assignedAccount;
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
