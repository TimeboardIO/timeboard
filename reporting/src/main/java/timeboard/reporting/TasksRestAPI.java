package timeboard.reporting;

/*-
 * #%L
 * reporting
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
import org.osgi.service.component.annotations.*;
import timeboard.core.api.ProjectService;
import timeboard.core.api.exceptions.BusinessException;
import timeboard.core.model.Project;
import timeboard.core.model.Task;
import timeboard.core.model.TaskStatus;
import timeboard.core.model.User;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.PATCH;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Component(
        service = TasksRestAPI.class,
        property = {
                "osgi.jaxrs.resource=true",
                "osgi.jaxrs.application.select=(osgi.jaxrs.name=.default)"
        }
)
@Path("/tasks")
@Produces(MediaType.APPLICATION_JSON)
public class TasksRestAPI {

    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Context
    private HttpServletRequest req;

    @Activate
    private void init(){
        System.out.println("Start tasks rest API !");
    }

    @Reference(cardinality = ReferenceCardinality.OPTIONAL, policyOption = ReferencePolicyOption.GREEDY)
    private ProjectService projectService;

    @GET
    @Path("/")
    public String sayHello(@Context HttpServletRequest request) throws Exception {
        User actor = (User) req.getAttribute("actor");

        final String strProjectID = request.getParameter("project");
        Long projectID = null;
        if(strProjectID != null){
            projectID = Long.parseLong(strProjectID);
        }else{
            throw new Exception("Incorrect project argument");
        }

        final Project project = this.projectService.getProjectByID(actor, projectID);
        if(project == null){
            throw new Exception("Project does not exists or you don't have enough permissions to access it.");
        }
        final List<Task> tasks = this.projectService.listProjectTasks(actor, project);

        final List<TaskWrapper> result = new ArrayList<>();

        for (Task task : tasks){
            User assignee = task.getAssigned();
            if(assignee == null){
                assignee = new User();
                assignee.setId(0);
                assignee.setName("");
                assignee.setFirstName("");
            }

            result. add(new TaskWrapper(
                    task.getId(),
                    task.getName(),
                    task.getComments(),
                    task.getOriginalEstimate(),
                    task.getStartDate(),
                    task.getEndDate(),
                    assignee.getScreenName(), assignee.getId(),
                    task.getTaskStatus().name(),
                    (task.getTaskType() != null ?task.getTaskType().getId() : 0L)));

        }

        return MAPPER.writeValueAsString(result.toArray());
    }

    @PATCH
    @Path("/")
    private String approveTask(@Context HttpServletRequest request) throws Exception {
        User actor = (User) req.getAttribute("actor");

        return this.changeTaskStatus(actor, request,  TaskStatus.IN_PROGESS);
    }

    /*@GET
    @Path("/deny")
    private String denyTask(@Context HttpServletRequest request) throws IOException, BusinessException {
        User actor = (User) req.getAttribute("actor");

        return this.changeTaskStatus(actor, request, TaskStatus.REFUSED);
    }*/

    private String changeTaskStatus(User actor, HttpServletRequest request,  TaskStatus status) throws Exception{
        final String taskIdStr = request.getParameter("taskId");
        Long taskId = null;
        if(taskIdStr != null) {
            taskId = Long.parseLong(taskIdStr);
        }else{
            throw new Exception("Missing argument taskId.");
        }
        if(taskId == null) {
            throw new Exception("Invalid argument taskId.");
        }

        Task task;
        try{
            task = (Task) this.projectService.getTaskByID(actor, taskId);
            task.setTaskStatus(status);
            this.projectService.updateTask(actor, task);
        } catch (ClassCastException e){
            throw new Exception("Task is not a project task.");
        } catch (Exception e){
            throw new Exception("Task id not found.");
        }
        return MAPPER.writeValueAsString("DONE");
    }


    public static class TaskWrapper {
        private final Long taskID;
        private final String taskName;
        private final String taskComments;
        private final double oE;
        private final Date startDate;
        private final Date endDate;
        private final String assignee;
        private final Long assigneeID;

        private final String status;

        private final Long type;

        public TaskWrapper(Long taskID, String taskName, String taskComments, double oE, Date startDate, Date endDate, String assignee, Long assigneeID, String status, Long type) {
            this.taskID = taskID;
            this.taskName = taskName;
            this.taskComments = taskComments;
            this.oE = oE;
            this.startDate = startDate;
            this.endDate = (endDate != null ? endDate : new Date());
            this.assignee = assignee;
            this.status = status;
            this.type = type;
            this.assigneeID = assigneeID;
        }

        public String getStartDate() {
            return DATE_FORMAT.format(startDate);
        }

        public String getEndDate() {
            return DATE_FORMAT.format(endDate);
        }

        public double getoE() {
            return oE;
        }

        public Long getTaskID() {
            return taskID;
        }

        public String getTaskName() {
            return taskName;
        }

        public Long getType() {
            return type;
        }

        public Long getAssigneeID() {
            return assigneeID;
        }

        public String getAssignee() {
            return assignee;
        }
        public String getStatus() {
            return status;
        }

        public String getTaskComments() {
            return taskComments;
        }
    }
}
