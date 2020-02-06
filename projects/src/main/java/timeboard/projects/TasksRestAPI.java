package timeboard.projects;

/*-
 * #%L
 * reports
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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import timeboard.core.api.OrganizationService;
import timeboard.core.api.ProjectService;
import timeboard.core.api.UserService;
import timeboard.core.model.*;
import timeboard.core.security.TimeboardAuthentication;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

@Component
@RestController
@RequestMapping(value = "/api/tasks", produces = MediaType.APPLICATION_JSON_VALUE)
public class TasksRestAPI {

    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Autowired
    private ProjectService projectService;

    @Autowired
    private OrganizationService organizationService;

    @Autowired
    private UserService userService;


    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity createTask(final TimeboardAuthentication authentication,
                                     @RequestBody final TaskWrapper taskWrapper) {

        final Task t = new Task();
        final Account actor = authentication.getDetails();
        final long orgID = authentication.getCurrentOrganization();

        try {

            final Long taskID = taskWrapper.taskID;

            t.setStartDate(startDateValidator(taskWrapper));
            t.setEndDate(endDateValidator(taskWrapper));
            t.setName(nameValidator(taskWrapper));
            t.setComments(commentsValidator(taskWrapper));
            t.setOriginalEstimate(oeValidator(taskWrapper));
            t.setProject(projectValidator(taskWrapper, actor, orgID));
            t.setTaskType(taskTypeValidator(taskWrapper));
            t.setAssigned(assigneeValidator(taskWrapper));
            t.setBatches(batchesValidator(taskWrapper, actor));
            t.setTaskStatus(taskStatusValidator(taskWrapper));

            final Task newTask =  processCreateOrUpdate(actor, orgID, taskID, t);
            taskWrapper.setTaskID(newTask.getId());
            return ResponseEntity.status(HttpStatus.OK).body(MAPPER.writeValueAsString(taskWrapper));

        } catch (TaskCreationException | JsonProcessingException e ) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }

    }

    private Task processCreateOrUpdate(Account actor,Long orgID,  Long taskID, Task t) throws TaskCreationException {
        Task task = null;
        try {
            if (taskID != null && taskID != 0) {
                final Task oldTask = (Task) projectService.getTaskByID(actor, taskID);
                if (oldTask.getEffortSpent() > 0 && oldTask.getAssigned() != t.getAssigned()) {
                    throw new TaskCreationException("You can not modify assignee because he already add imputation on this task");
                }
                t.setOrigin(oldTask.getOrigin());
                t.setId(oldTask.getId());
                task = processUpdateTask(orgID, actor, t);

            }
            else {
                task = processCreateTask(orgID, actor, t);
            }

            return task;
        } catch (final Exception e) {
            throw new TaskCreationException("Error in task creation/update please verify your inputs and retry. (" + e.getMessage() + ")");
        }
    }

    private TaskStatus taskStatusValidator(@RequestBody final TaskWrapper taskWrapper) {
        return  TaskStatus.valueOf(taskWrapper.getStatus());
    }

    private Set<Batch> batchesValidator(@RequestBody final TaskWrapper taskWrapper, final Account actor) {
        Set<Batch> returnList = null;
        final List<Long> batchIDList = taskWrapper.batchIDs;

        if (batchIDList != null && !batchIDList.isEmpty()) {
            returnList = new HashSet<>();
            for (final Long batchID : batchIDList) {
                try {
                    final Batch batch = this.projectService.getBatchById(actor, batchID);
                    if (batch != null) {
                        returnList.add(batch);
                    }
                } catch (final Exception e) {
                    // Do nothing, just handling the exceptions
                }
            }
        }
        return returnList;
    }

    private Account assigneeValidator(@RequestBody final TaskWrapper taskWrapper) {
        final Long assigneeID = taskWrapper.assigneeID;

        if (assigneeID != null && assigneeID > 0) {
            return userService.findUserByID(assigneeID);
        }
        return  null;
    }

    private TaskType taskTypeValidator(@RequestBody final TaskWrapper taskWrapper) throws TaskCreationException {
        try {
            return this.organizationService.findTaskTypeByID(taskWrapper.typeID);
        } catch (Exception e) {
            throw new TaskCreationException("Could not find task type");
        }
    }

    private Project projectValidator(@RequestBody final TaskWrapper taskWrapper, Account actor, Long orgID) throws TaskCreationException {
        final Long projectID = taskWrapper.projectID;
        Project project = null;
        try {
            project = this.projectService.getProjectByID(actor, orgID, projectID);
        } catch (final Exception e) {
            throw new TaskCreationException("Can not found project "+e.getMessage());
        }

        return project;
    }

    private double oeValidator(@RequestBody final TaskWrapper taskWrapper) throws TaskCreationException {
        final double oe = taskWrapper.originalEstimate;
        if (oe <= 0.0) {
            throw new TaskCreationException("Original original estimate must be positive ");
        }
        return oe;
    }

    private Date startDateValidator(@RequestBody final TaskWrapper taskWrapper) throws TaskCreationException {
        Date startDate = null;
        Date endDate = null;

        try {
            startDate = DATE_FORMAT.parse(taskWrapper.startDate);
            endDate = DATE_FORMAT.parse(taskWrapper.endDate);
        } catch (final Exception e) {
            throw new TaskCreationException("Incorrect date format");
        }

        if (startDate.getTime() > endDate.getTime()) {
            throw new TaskCreationException("Start date must be before end date ");
        }
        return startDate;
    }

    private Date endDateValidator(@RequestBody final TaskWrapper taskWrapper) throws TaskCreationException {
        Date endDate = null;

        try {
            endDate = DATE_FORMAT.parse(taskWrapper.endDate);
        } catch (final Exception e) {
            throw new TaskCreationException("Incorrect date format.");
        }
        return endDate;

    }

    private String commentsValidator(@RequestBody final TaskWrapper taskWrapper) {
        String comment = taskWrapper.taskComments;
        if (comment == null) {
            comment = "";
        }
        return comment;
    }

    private String nameValidator(@RequestBody final TaskWrapper taskWrapper) throws TaskCreationException {
        final String name = taskWrapper.taskName;
        if (name == null) {
            throw new TaskCreationException("You have to specify task name.");
        }
        if (name.length() > 100) {
            throw new TaskCreationException("The task name you specify is too long. The field is limited to 100 prints.");
        }
        return name;
    }


    private Task processCreateTask(final Long orgID, final Account actor, final Task task) {
        return projectService.createTask(orgID, actor, task.getProject(),
                task.getName(), task.getComments(), task.getStartDate(),
                task.getEndDate(), task.getOriginalEstimate(), task.getTaskType(), task.getAssigned(),
                ProjectService.ORIGIN_TIMEBOARD, null, null, TaskStatus.PENDING,
                task.getBatches());
    }

    private Task processUpdateTask(final Long orgID, final Account actor, final Task task) {
        return projectService.updateTask(orgID, actor, task);
    }




    public static class TaskGraphWrapper implements Serializable {
        public List<String> listOfTaskDates;
        public Collection<Double> effortSpentData;
        public Collection<Double> realEffortData;

        public TaskGraphWrapper() {
        }

        public void setListOfTaskDates(final List<String> listOfTaskDates) {
            this.listOfTaskDates = listOfTaskDates;
        }

        public void setEffortSpentData(final Collection<Double> effortSpentData) {
            this.effortSpentData = effortSpentData;

        }

        public void setRealEffortData(final Collection<Double> realEffortData) {
            this.realEffortData = realEffortData;
        }
    }

    public static class BatchWrapper implements Serializable {

        public Long batchID;
        public String batchName;

        public BatchWrapper(final Long batchID, final String batchName) {
            this.batchID = batchID;
            this.batchName = batchName;
        }

        public Long getBatchID() {
            return batchID;
        }

        public void setBatchID(final Long batchID) {
            this.batchID = batchID;
        }

        public String getBatchName() {
            return batchName;
        }

        public void setBatchName(final String batchName) {
            this.batchName = batchName;
        }


    }

    public static class TaskWrapper implements Serializable {
        public Long taskID;
        public Long projectID;

        public String taskName;
        public String taskComments;

        public double originalEstimate;

        public String startDate;
        public String endDate;

        public String assignee;
        public Long assigneeID;

        public Long typeID;
        public String typeName;

        public String status;
        public String statusName;

        public List<Long> batchIDs;
        public List<String> batchNames;

        public boolean canChangeAssignee;

        public TaskWrapper() {
        }

        public TaskWrapper(final Long taskID,
                           final String taskName,
                           final String taskComments,
                           final double originalEstimate,
                           final Date startDate,
                           final Date endDate,
                           final String assignee,
                           final Long assigneeID,
                           final String status,
                           final Long typeID,
                           final List<Long> batchIDs,
                           final List<String> batchNames,
                           final String statusName,
                           final String typeName,
                           final boolean canChangeAssignee) {

            this.taskID = taskID;

            this.taskName = taskName;
            this.taskComments = taskComments;
            this.originalEstimate = originalEstimate;
            this.startDate = DATE_FORMAT.format(startDate);
            if (endDate != null) {
                this.endDate = DATE_FORMAT.format(endDate);
            }
            this.assignee = assignee;
            this.assigneeID = assigneeID;
            this.status = status;
            this.typeID = typeID;

            this.batchNames = batchNames;
            this.batchIDs = batchIDs;

            this.statusName = statusName;
            this.typeName = typeName;
            this.canChangeAssignee = canChangeAssignee;
        }


        public List<Long> getBatchIDs() {
            return batchIDs;
        }

        public void setBatchIDs(final List<Long> batchIDs) {
            this.batchIDs = batchIDs;
        }

        public List<String> getBatchNames() {
            return batchNames;
        }

        public void setBatchNames(final List<String> batchNames) {
            this.batchNames = batchNames;
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

        public void setTaskName(final String taskName) {
            this.taskName = taskName;
        }

        public String getTaskComments() {
            return taskComments;
        }

        public void setTaskComments(final String taskComments) {
            this.taskComments = taskComments;
        }

        public double getOriginalEstimate() {
            return originalEstimate;
        }

        public void setOriginalEstimate(final double originalEstimate) {
            this.originalEstimate = originalEstimate;
        }

        public String getStartDate() {
            return startDate;
        }

        public String getAssignee() {
            return assignee;
        }

        public void setAssignee(final String assignee) {
            this.assignee = assignee;
        }

        public Long getAssigneeID() {
            return assigneeID;
        }

        public void setAssigneeID(final Long assigneeID) {
            this.assigneeID = assigneeID;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(final String status) {
            this.status = status;
        }

        public Long getTypeID() {
            return typeID;
        }

        public void setTypeID(final Long typeID) {
            this.typeID = typeID;
        }

        public Long getProjectID() {
            return projectID;
        }

        public void setProjectID(final Long projectID) {
            this.projectID = projectID;
        }

        public String getEndDate() {
            return this.endDate;
        }
    }


    public class TaskCreationException extends Exception {

        public TaskCreationException(String message){
            super(message);
        }
    }
}
