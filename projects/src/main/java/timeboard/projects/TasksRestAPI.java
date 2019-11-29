package timeboard.projects;

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
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
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
        System.out.println("Start Tasks rest API !");
    }

    @Reference(cardinality = ReferenceCardinality.OPTIONAL, policyOption = ReferencePolicyOption.GREEDY)
    private ProjectService projectService;

    @GET
    @Path("/")
    public String getTasks(@Context HttpServletRequest request) throws Exception {
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

    @GET
    @Path("/approve")
    public String approveTask(@Context HttpServletRequest request) throws Exception {
        User actor = (User) req.getAttribute("actor");
        return this.changeTaskStatus(actor, request,  TaskStatus.IN_PROGESS);
    }

    @GET
    @Path("/deny")
    public String denyTask(@Context HttpServletRequest request) throws Exception {
        User actor = (User) req.getAttribute("actor");

        return this.changeTaskStatus(actor, request, TaskStatus.REFUSED);
    }

    private String changeTaskStatus(User actor, HttpServletRequest request,  TaskStatus status) throws Exception{
        final String taskIdStr = request.getParameter("task");
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


    @POST
    @Consumes("application/json")
    @Path("/")
    public String createTask(TaskWrapper toto) throws Exception {
        User actor = (User) req.getAttribute("actor");
        Date startDate = null;
        Date endDate = null;
/*
        try{
            startDate = DATE_FORMAT.parse(request.getParameter("startDate"));
            endDate = DATE_FORMAT.parse(request.getParameter("endDate"));

        }catch(Exception e) {
            throw new Exception("Incorrect date format");
        }

        if(startDate.getTime()>endDate.getTime()){
            throw new Exception("Start date must be before end date ");
        }

        String name = request.getParameter("taskName");
        String comment = request.getParameter("taskComments");
        if(comment == null) comment = "";

        double oe = Double.parseDouble(request.getParameter("originalEstimate"));
        if(oe <= 0.0){
            throw new Exception( "Original original estimate must be positive ");
        }

        Long projectID = Long.parseLong(request.getParameter("projectID"));
        Project project = null;
        try {
            project = this.projectService.getProjectByID(actor, projectID);
        } catch (BusinessException e) {
            throw new Exception(e.getMessage());
        }

        String type = request.getParameter("typeID");
        Long typeID = Long.parseLong(type);

        Long taskID = Long.parseLong(request.getParameter("taskID"));
        if(!(taskID != null && taskID == 0 )){
            try {
                projectService.deleteTaskByID(actor, taskID);
            } catch (Exception e){
                throw new Exception( e.getMessage());
            }
        }
        try{
            projectService.createTask(actor, project,
                    name, comment, startDate, endDate, oe, typeID, actor, ProjectService.ORIGIN_TIMEBOARD, null,null,null );
        }catch (Exception e){
            throw new Exception("Error in task creation please verify your inputs and retry");
        }
*/
        return MAPPER.writeValueAsString("DONE");

    }



    public static class TaskWrapper implements Serializable {
        public final Long taskID;
        public final String taskName;
        public final String taskComments;
        public final double originalEstimate;
        public final Date startDate;
        public final Date endDate;
        public final String assignee;
        public final Long assigneeID;
        public final String status;
        public final Long typeID;

        public TaskWrapper(Long taskID, String taskName, String taskComments, double originalEstimate, Date startDate, Date endDate, String assignee, Long assigneeID, String status, Long typeID) {
            this.taskID = taskID;
            this.taskName = taskName;
            this.taskComments = taskComments;
            this.originalEstimate = originalEstimate;
            this.startDate = startDate;
            this.endDate = (endDate != null ? endDate : new Date());
            this.assignee = assignee;
            this.status = status;
            this.typeID = typeID;
            this.assigneeID = assigneeID;
        }

        public String getStartDate() {
            return DATE_FORMAT.format(startDate);
        }

        public String getEndDate() {
            return DATE_FORMAT.format(endDate);
        }

        public double getOriginalEstimate() {
            return originalEstimate;
        }

        public Long getTaskID() {
            return taskID;
        }

        public String getTaskName() {
            return taskName;
        }

        public Long getTypeID() {
            return typeID;
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
