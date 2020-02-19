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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import timeboard.core.api.DataTableService;
import timeboard.core.api.OrganizationService;
import timeboard.core.api.ProjectService;
import timeboard.core.api.exceptions.BusinessException;
import timeboard.core.api.sync.ProjectSyncPlugin;
import timeboard.core.model.*;
import timeboard.core.security.TimeboardAuthentication;

import javax.servlet.http.HttpServletRequest;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;


@Controller
@RequestMapping("/projects/{project}" + ProjectTasksController.URL)
public class ProjectTasksController extends ProjectBaseController {

    public static final String URL = "/tasks";

    private static final Logger LOGGER = LoggerFactory.getLogger(ProjectTasksController.class);

    @Autowired
    public ProjectService projectService;

    @Autowired
    public OrganizationService organizationService;

    @Autowired
    public DataTableService dataTableService;

    @Autowired(required = false)
    public List<ProjectSyncPlugin> projectImportServiceList;

    @GetMapping
    protected String handleGet(
            final TimeboardAuthentication authentication,
            @PathVariable final Project project, final Model model) throws BusinessException {

        final Task task = new Task();

        model.addAttribute("task", new TaskForm(task));
        model.addAttribute("import", 0);
        model.addAttribute("sync_plugins", this.projectImportServiceList);

        fillModel(model, authentication.getCurrentOrganization(), authentication, project);

        model.addAttribute("batchType", "Default");
        this.initModel(model, authentication, project);
        return "project_tasks.html";
    }

    @GetMapping("/list")
    public ResponseEntity getTasks(final TimeboardAuthentication authentication,
                                   final HttpServletRequest request,
                                   @PathVariable final Project project) throws JsonProcessingException {

        final Account actor = authentication.getDetails();

        try {
            if (project == null) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("Project does not exists or you don't have enough permissions to access it.");
            }
            final List<Task> tasks = this.projectService.listProjectTasks(actor, project);

            final List<TasksRestAPI.TaskWrapper> result = new ArrayList<>();

            for (final Task task : tasks) {
                Account assignee = task.getAssigned();
                if (assignee == null) {
                    assignee = new Account();
                    assignee.setId(0);
                    assignee.setName("");
                    assignee.setFirstName("");
                }

                final List<Long> batchIDs = new ArrayList<>();
                final List<String> batchNames = new ArrayList<>();

                task.getBatches().forEach(b -> {
                    batchIDs.add(b.getId());
                    batchNames.add(b.getScreenName());
                });
                result.add(new TasksRestAPI.TaskWrapper(
                        task.getId(),
                        task.getName(),
                        task.getComments(),
                        task.getOriginalEstimate(),
                        task.getStartDate(),
                        task.getEndDate(),
                        assignee.getScreenName(), assignee.getId(),
                        task.getTaskStatus().name(),
                        task.getTaskType() != null ? task.getTaskType().getId() : 0L,
                        batchIDs, batchNames,
                        task.getTaskStatus().name(),
                        task.getTaskType() != null ? task.getTaskType().getTypeName() : "",
                        task.getEffortSpent() == 0
                ));

            }
            return ResponseEntity.status(HttpStatus.OK).body(result.toArray());
        } catch (final BusinessException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }

    }

    @GetMapping("/group/{batchType}")
    protected String listTasksGroupByBatchType(
            final TimeboardAuthentication authentication,
            @PathVariable final Project project,
            @PathVariable final String batchType,
            final Model model) throws BusinessException {

        final Account actor = authentication.getDetails();

        final Task task = new Task();

        final BatchType javaBatchType = BatchType.valueOf(batchType.toUpperCase());
        model.addAttribute("batchType", batchType);
        model.addAttribute("batchList", this.projectService.getBatchList(actor, project, javaBatchType));

        model.addAttribute("task", new TaskForm(task));
        model.addAttribute("import", 0);
        model.addAttribute("sync_plugins", this.projectImportServiceList);

        fillModel(model, authentication.getCurrentOrganization(), authentication, project);

        return "project_tasks.html";
    }

    private void fillModel(final Model model,
                           final Organization org,
                           final TimeboardAuthentication auth,
                           final Project project) throws BusinessException {

        model.addAttribute("project", project);
        model.addAttribute("tasks", this.projectService.listProjectTasks(auth.getDetails(), project));
        model.addAttribute("taskTypes", this.organizationService.listTaskType(org));
        model.addAttribute("allTaskStatus", TaskStatus.values());
        model.addAttribute("allProjectBatches", this.projectService.listProjectBatches(auth.getDetails(), project));
        model.addAttribute("allProjectBatchTypes", this.projectService.listProjectUsedBatchType(auth.getDetails(), project));
        model.addAttribute("isProjectOwner", this.projectService.isProjectOwner(auth.getDetails(), project));
        model.addAttribute("dataTableService", this.dataTableService);
        model.addAttribute("projectMembers", project.getMembers());
        this.initModel(model, auth, project);
    }

    @GetMapping("/{task}")
    protected String editTasks(
            final TimeboardAuthentication authentication,
            @PathVariable final Project project,
            @PathVariable final Long taskID, final Model model) throws BusinessException {

        final Account actor = authentication.getDetails();

        final Task task = (Task) this.projectService.getTaskByID(actor, taskID);

        model.addAttribute("task", new TaskForm(task));


        fillModel(model, authentication.getCurrentOrganization(), authentication, project);


        return "project_tasks.html";
    }

    @PatchMapping("/approve/{taskID}")
    public ResponseEntity approveTask(final TimeboardAuthentication authentication,
                                      @PathVariable final Long taskID) {
        final Account actor = authentication.getDetails();
        return this.changeTaskStatus(
                authentication.getCurrentOrganization(),
                actor,
                taskID,
                TaskStatus.IN_PROGRESS);
    }

    @PatchMapping("/deny/{taskID}")
    public ResponseEntity denyTask(final TimeboardAuthentication authentication,
                                   @PathVariable final Long taskID) {
        final Account actor = authentication.getDetails();
        return this.changeTaskStatus(
                authentication.getCurrentOrganization(),
                actor,
                taskID,
                TaskStatus.REFUSED);
    }

    private ResponseEntity changeTaskStatus(final Organization org,
                                            final Account actor,
                                            final Long taskID,
                                            final TaskStatus status) {


        if (taskID == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid argument taskId.");
        }

        final Task task;
        try {
            task = (Task) this.projectService.getTaskByID(actor, taskID);
            task.setTaskStatus(status);
            this.projectService.updateTask(org, actor, task);

        } catch (final ClassCastException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Task is not a project task.");
        } catch (final Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Task id not found.");
        }

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/delete/{taskID}")
    public ResponseEntity deleteTask(final TimeboardAuthentication authentication,
                                     @PathVariable final Long taskID) {
        final Account actor = authentication.getDetails();

        if (taskID == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid argument taskId.");
        }

        try {
            projectService.deleteTaskByID(actor, taskID);
        } catch (final Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }

        return ResponseEntity.ok().build();
    }


    @GetMapping("/batches")
    public ResponseEntity getBatches(final TimeboardAuthentication authentication,
                                     final HttpServletRequest request) throws JsonProcessingException {
        final Account actor = authentication.getDetails();
        Project project = null;

        final String strProjectID = request.getParameter("project");
        final String strBatchType = request.getParameter("batchType");

        Long projectID = null;
        if (strProjectID != null) {
            projectID = Long.parseLong(strProjectID);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Incorrect project argument");
        }

        BatchType batchType = null;
        if (strBatchType != null) {
            batchType = BatchType.valueOf(strBatchType.toUpperCase());
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Incorrect batchType argument");
        }

        try {
            project = this.projectService.getProjectByID(actor, authentication.getCurrentOrganization(), projectID);
        } catch (final BusinessException e) {
            // just handling exception
        }
        if (project == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Project does not exists or you don't have enough permissions to access it.");
        }

        List<Batch> batchList = null;
        try {
            batchList = projectService.getBatchList(actor, project, batchType);
        } catch (final BusinessException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Project does not exists or you don't have enough permissions to access it.");
        }

        final List<TasksRestAPI.BatchWrapper> batchWrapperList = new ArrayList<>();
        batchList.forEach(batch -> batchWrapperList.add(new TasksRestAPI.BatchWrapper(batch.getId(), batch.getScreenName())));

        return ResponseEntity.status(HttpStatus.OK).body(batchWrapperList.toArray());
    }

    @GetMapping("/chart")
    public ResponseEntity getDatasForCharts(
            final TimeboardAuthentication authentication,
            final HttpServletRequest request) throws BusinessException, JsonProcessingException {

        final TasksRestAPI.TaskGraphWrapper wrapper = new TasksRestAPI.TaskGraphWrapper();
        final Account actor = authentication.getDetails();

        final String taskIdStr = request.getParameter("task");
        Long taskID = null;
        if (taskIdStr != null) {
            taskID = Long.parseLong(taskIdStr);
        }
        if (taskID == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid argument taskId.");
        }

        final Task task = (Task) this.projectService.getTaskByID(actor, taskID);

        // Datas for dates (Axis X)
        final String formatLocalDate = "yyyy-MM-dd";
        final String formatDateToDisplay = "dd/MM/yyyy";
        final LocalDate start = LocalDate.parse(new SimpleDateFormat(formatLocalDate).format(task.getStartDate()));
        final LocalDate end = LocalDate.parse(new SimpleDateFormat(formatLocalDate).format(task.getEndDate()));
        final List<String> listOfTaskDates = start.datesUntil(end.plusDays(1))
                .map(localDate -> localDate.format(DateTimeFormatter.ofPattern(formatDateToDisplay)))
                .collect(Collectors.toList());
        wrapper.setListOfTaskDates(listOfTaskDates);

        // Datas for effort spent (Axis Y)
        final List<ValueHistory> effortSpentDB = this.projectService.getEffortSpentByTaskAndPeriod(actor,
                task, task.getStartDate(), task.getEndDate());
        final ValueHistory[] lastEffortSpentSum = {new ValueHistory(task.getStartDate(), 0.0)};
        final Map<Date, Double> effortSpentMap = listOfTaskDates
                .stream()
                .map(dateString -> {
                    return formatDate(formatDateToDisplay, dateString);
                })
                .map(date -> effortSpentDB.stream()
                        .filter(es -> new SimpleDateFormat(formatDateToDisplay)
                                .format(es.getDate()).equals(new SimpleDateFormat(formatDateToDisplay).format(date)))
                        .map(effort -> {
                            lastEffortSpentSum[0] = new ValueHistory(date, effort.getValue());
                            return lastEffortSpentSum[0];
                        })
                        .findFirst().orElse(new ValueHistory(date, lastEffortSpentSum[0].getValue())))
                .collect(Collectors.toMap(
                        e -> e.getDate(),
                        e -> e.getValue(),
                        (x, y) -> y, LinkedHashMap::new
                ));
        wrapper.setEffortSpentData(effortSpentMap.values());

        return ResponseEntity.status(HttpStatus.OK).body(wrapper);

    }

    private Date formatDate(final String formatDateToDisplay, final String dateString) {
        try {
            return new SimpleDateFormat(formatDateToDisplay).parse(dateString);
        } catch (final ParseException e) {
            LOGGER.error(e.getMessage());
        }
        return null;
    }

    @PostMapping
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
