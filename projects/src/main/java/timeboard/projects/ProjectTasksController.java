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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import timeboard.core.api.DataTableService;
import timeboard.core.api.OrganizationService;
import timeboard.core.api.ProjectService;
import timeboard.core.api.exceptions.BusinessException;
import timeboard.core.api.sync.ProjectSyncPlugin;
import timeboard.core.model.*;
import timeboard.core.security.TimeboardAuthentication;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


@Controller
@RequestMapping("/projects/{projectID}")
public class ProjectTasksController {


    @Autowired
    public ProjectService projectService;

    @Autowired
    public OrganizationService organizationService;

    @Autowired
    public DataTableService dataTableService;

    @Autowired(required = false)
    public List<ProjectSyncPlugin> projectImportServiceList;

    @GetMapping("/tasks")
    protected String listTasks(
            final TimeboardAuthentication authentication,
            @PathVariable final Long projectID, final Model model) throws BusinessException {

        final Account actor = authentication.getDetails();

        final Task task = new Task();
        final Project project = this.projectService.getProjectByID(actor, authentication.getCurrentOrganization(), projectID);

        model.addAttribute("task", new TaskForm(task));
        model.addAttribute("import", 0);
        model.addAttribute("sync_plugins", this.projectImportServiceList);

        fillModel(model, authentication.getCurrentOrganization(), actor, project);

        model.addAttribute("batchType", "Default");
        return "project_tasks.html";
    }


    @GetMapping("/tasks/group/{batchType}")
    protected String listTasksGroupByBatchType(
            final TimeboardAuthentication authentication,
            @PathVariable final Long projectID, @PathVariable final String batchType, final Model model) throws BusinessException {

        final Account actor = authentication.getDetails();

        final Task task = new Task();
        final Project project = this.projectService.getProjectByID(actor, authentication.getCurrentOrganization(), projectID);

        final BatchType javaBatchType = BatchType.valueOf(batchType.toUpperCase());
        model.addAttribute("batchType", batchType);
        model.addAttribute("batchList", this.projectService.getBatchList(actor, project, javaBatchType));

        model.addAttribute("task", new TaskForm(task));
        model.addAttribute("import", 0);
        model.addAttribute("sync_plugins", this.projectImportServiceList);

        fillModel(model, authentication.getCurrentOrganization(), actor, project);

        return "project_tasks.html";
    }

    private void fillModel(final Model model, final Long orgID, final Account actor, final Project project) throws BusinessException {
        model.addAttribute("project", project);
        model.addAttribute("tasks", this.projectService.listProjectTasks(actor, project));
        model.addAttribute("taskTypes", this.organizationService.listTaskType(orgID));
        model.addAttribute("allTaskStatus", TaskStatus.values());
        model.addAttribute("allProjectBatches", this.projectService.listProjectBatches(actor, project));
        model.addAttribute("allProjectBatchTypes", this.projectService.listProjectUsedBatchType(actor, project));
        model.addAttribute("isProjectOwner", this.projectService.isProjectOwner(actor, project));
        model.addAttribute("dataTableService", this.dataTableService);
        model.addAttribute("projectMembers", project.getMembers());
    }

    @GetMapping("/tasks/{taskID}")
    protected String editTasks(
            final TimeboardAuthentication authentication,
            @PathVariable final Long projectID,
            @PathVariable final Long taskID, final Model model) throws BusinessException {

        final Account actor = authentication.getDetails();

        final Task task = (Task) this.projectService.getTaskByID(actor, taskID);

        model.addAttribute("task", new TaskForm(task));

        final Project project = this.projectService.getProjectByID(actor, authentication.getCurrentOrganization(), projectID);

        fillModel(model, authentication.getCurrentOrganization(), actor, project);


        return "project_tasks.html";
    }

    @PostMapping("/tasks")
    protected String handlePost(
            final TimeboardAuthentication authentication,
            final HttpServletRequest request, final Model model, final RedirectAttributes attributes) throws BusinessException {

        final Account actor = authentication.getDetails();

        final long projectID = Long.parseLong(request.getParameter("projectID"));
        final Project project = this.projectService.getProjectByID(actor, authentication.getCurrentOrganization(), projectID);

        model.addAttribute("tasks", this.projectService.listProjectTasks(actor, project));
        model.addAttribute("project", project);
        attributes.addFlashAttribute("success", "Project created successfully.");
        return "project_tasks.html";
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
        private List<Long> batchesID;

        public TaskForm(final Task task) {
            this.taskID = task.getId();
            this.taskType = task.getTaskType();
            if (task.getBatches() != null) {
                this.batchesID = new ArrayList<>();
                task.getBatches().forEach(batch -> batchesID.add(batch.getId()));
            }
            this.originalEstimate = task.getOriginalEstimate();
            this.startDate = task.getStartDate();
            this.endDate = task.getEndDate();
            this.assignedAccount = task.getAssigned();
            this.taskName = task.getName();
            this.taskComment = task.getComments();
            this.taskStatus = task.getTaskStatus();
            this.taskStatus = TaskStatus.PENDING;

        }

        public Long getTaskID() {
            return taskID;
        }

        public void setTaskID(final Long taskID) {
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

        public void setAssignedUserID(final Long assignedUserID) {
            this.assignedUserID = assignedUserID;
        }

        public Long getTaskTypeID() {
            return taskTypeID;
        }

        public void setTaskTypeID(final Long taskTypeID) {
            this.taskTypeID = taskTypeID;
        }

        public TaskType getTaskType() {
            return taskType;
        }

        public void setTaskType(final TaskType taskType) {
            this.taskType = taskType;
        }

        public Account getAssignedAccount() {
            return assignedAccount;
        }

        public void setAssignedAccount(final Account assignedAccount) {
            this.assignedAccount = assignedAccount;
        }

        public TaskStatus getTaskStatus() {
            return taskStatus;
        }

        public void setTaskStatus(final TaskStatus taskStatus) {
            this.taskStatus = taskStatus;
        }

        public List<Long> getBatchesID() {
            return batchesID;
        }

        public void setBatchesID(final List<Long> batchesID) {
            this.batchesID = batchesID;
        }
    }
}
