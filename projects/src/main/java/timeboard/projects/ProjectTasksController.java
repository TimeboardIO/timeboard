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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import timeboard.core.TimeboardAuthentication;
import timeboard.core.api.DataTableService;
import timeboard.core.api.ProjectService;
import timeboard.core.api.exceptions.BusinessException;
import timeboard.core.api.sync.ProjectSyncCredentialField;
import timeboard.core.api.sync.ProjectSyncPlugin;
import timeboard.core.api.sync.ProjectSyncService;
import timeboard.core.model.*;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


@Controller
@RequestMapping("/projects/{projectID}")
public class ProjectTasksController {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Autowired
    public ProjectService projectService;

    @Autowired
    public DataTableService dataTableService;

    @Autowired
    public AsyncJobService asyncJobService;

    @Autowired
    public ProjectSyncService projectSyncService;

    @Autowired(required = false)
    public List<ProjectSyncPlugin> projectImportServiceList;

    @GetMapping("/tasks")
    protected String listTasks(
            TimeboardAuthentication authentication,
            @PathVariable Long projectID, Model model) throws BusinessException {

        final Account actor = authentication.getDetails();

        final Task task = new Task();
        final Project project = this.projectService.getProjectByID(actor, authentication.getCurrentOrganization(), projectID);

        model.addAttribute("task", new TaskForm(task));
        model.addAttribute("import", this.asyncJobService.getAccountJobs(actor).size());
        model.addAttribute("sync_plugins", this.projectImportServiceList);

        fillModel(model, actor, project);

        model.addAttribute("batchType", "Default");
        return "project_tasks.html";
    }


    @GetMapping("/tasks/group/{batchType}")
    protected String listTasksGroupByBatchType(
            TimeboardAuthentication authentication,
            @PathVariable Long projectID, @PathVariable String batchType, Model model) throws BusinessException {

        final Account actor = authentication.getDetails();

        final Task task = new Task();
        final Project project = this.projectService.getProjectByID(actor, authentication.getCurrentOrganization(), projectID);

        BatchType javaBatchType = BatchType.valueOf(batchType.toUpperCase());
        model.addAttribute("batchType", batchType);
        model.addAttribute("batchList", this.projectService.getBatchList(actor, project, javaBatchType));

        model.addAttribute("task", new TaskForm(task));
        model.addAttribute("import", this.asyncJobService.getAccountJobs(actor).size());
        model.addAttribute("sync_plugins", this.projectImportServiceList);

        fillModel(model, actor, project);

        return "project_tasks.html";
    }

    private void fillModel(Model model, Account actor, Project project) throws BusinessException {
        model.addAttribute("project", project);
        model.addAttribute("tasks", this.projectService.listProjectTasks(actor, project));
        model.addAttribute("taskTypes", this.projectService.listTaskType());
        model.addAttribute("allTaskStatus", TaskStatus.values());
        model.addAttribute("allProjectBatches", this.projectService.listProjectBatches(actor, project));
        model.addAttribute("allProjectBatchTypes", this.projectService.listProjectUsedBatchType(actor, project));
        model.addAttribute("isProjectOwner", this.projectService.isProjectOwner(actor, project));
        model.addAttribute("dataTableService", this.dataTableService);
        model.addAttribute("projectMembers", project.getMembers());
    }

    @GetMapping("/tasks/{taskID}")
    protected String editTasks(
                    TimeboardAuthentication authentication,
                    @PathVariable Long projectID,
                    @PathVariable Long taskID, Model model) throws BusinessException {

        final Account actor = authentication.getDetails();

        final Task task = (Task) this.projectService.getTaskByID(actor, taskID);

        model.addAttribute("task", new TaskForm(task));

        final Project project = this.projectService.getProjectByID(actor, authentication.getCurrentOrganization(), projectID);

        fillModel(model, actor, project);


        return "project_tasks.html";
    }

    @PostMapping(value = "/tasks/sync/{serviceName}", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    protected String importFromJIRA(
            TimeboardAuthentication authentication,
            @PathVariable Long projectID,
            @PathVariable String serviceName,
            @RequestBody MultiValueMap<String, String> formBody) throws BusinessException, JsonProcessingException {

        final Account actor = authentication.getDetails();
        final Project project = this.projectService.getProjectByID(actor, authentication.getCurrentOrganization(), projectID);

        final List<ProjectSyncCredentialField> creds = this.projectSyncService.getServiceFields(serviceName);

        creds.forEach(field -> {
            if(formBody.containsKey(field.getFieldKey())){
                field.setValue(formBody.get(field.getFieldKey()).get(0));
            }
        });

        this.projectSyncService.syncProjectTasks(authentication.getCurrentOrganization(), actor, project, serviceName, creds);


        return "redirect:/projects/"+projectID+"/tasks";
    }

    @PostMapping("/tasks")
    protected String handlePost(
            TimeboardAuthentication authentication,
            HttpServletRequest request, Model model,  RedirectAttributes attributes) throws BusinessException {

        Account actor = authentication.getDetails();

        long projectID = Long.parseLong(request.getParameter("projectID"));
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

        public TaskForm(Task task) {
            this.taskID = task.getId();
            this.taskType = task.getTaskType();
            if( task.getBatches() != null){
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

        public List<Long> getBatchesID() {
            return batchesID;
        }

        public void setBatchesID(List<Long> batchesID) {
            this.batchesID = batchesID;
        }
    }
}
