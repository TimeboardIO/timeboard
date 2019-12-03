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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.Serializable;
import javax.servlet.http.HttpServletRequest;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.osgi.service.component.annotations.*;
import timeboard.core.api.ProjectService;
import timeboard.core.api.UserService;
import timeboard.core.api.exceptions.BusinessException;
import timeboard.core.model.*;


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

    @Reference(cardinality = ReferenceCardinality.OPTIONAL, policyOption = ReferencePolicyOption.GREEDY)
    private UserService userService;

    @GET
    @Path("/")
    public Response getTasks(@Context HttpServletRequest request) throws JsonProcessingException {
        User actor = (User) req.getAttribute("actor");

        final String strProjectID = request.getParameter("project");
        Long projectID = null;
        if (strProjectID != null) {
            projectID = Long.parseLong(strProjectID);
        } else {
            return Response.status(Response.Status.BAD_REQUEST).entity("Incorrect project argument").build();
        }

        try {
            final Project project = this.projectService.getProjectByID(actor, projectID);
            if (project == null) {
                return Response.status(Response.Status.FORBIDDEN).entity("Project does not exists or you don't have enough permissions to access it.").build();
            }
            final List<Task> tasks = this.projectService.listProjectTasks(actor, project);

            final List<TaskWrapper> result = new ArrayList<>();

            for (Task task : tasks) {
                User assignee = task.getAssigned();
                if (assignee == null) {
                    assignee = new User();
                    assignee.setId(0);
                    assignee.setName("");
                    assignee.setFirstName("");
                }

                result.add(new TaskWrapper(
                        task.getId(),
                        task.getName(),
                        task.getComments(),
                        task.getOriginalEstimate(),
                        task.getStartDate(),
                        task.getEndDate(),
                        assignee.getScreenName(), assignee.getId(),
                        task.getTaskStatus().name(),
                        (task.getTaskType() != null ? task.getTaskType().getId() : 0L),
                        (task.getMilestone() != null ? task.getMilestone().getId() : 0L)));

            }
            return Response.ok().entity(MAPPER.writeValueAsString(result.toArray())).build();
        } catch (BusinessException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }

    }

    @GET
    @Path("/chart")
    public Response getDatasForCharts(@Context HttpServletRequest request) throws BusinessException, JsonProcessingException {
        TaskGraphWrapper wrapper = new TaskGraphWrapper();
        User actor = (User) req.getAttribute("actor");


        final String taskIdStr = request.getParameter("task");
        Long taskID = null;
        if(taskIdStr != null) {
            taskID = Long.parseLong(taskIdStr);
        }
        if(taskID == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Invalid argument taskId.").build();
        }

        Task task;
        try {
            task = (Task) this.projectService.getTaskByID(actor, taskID);
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Invalid argument taskId.").build();

        }

        // Datas for dates (Axis X)
        String formatLocalDate = "yyyy-MM-dd";
        String formatDateToDisplay = "dd/MM/yyyy";
        LocalDate start = LocalDate.parse(new SimpleDateFormat(formatLocalDate).format(task.getStartDate()));
        LocalDate end = LocalDate.parse(new SimpleDateFormat(formatLocalDate).format(task.getEndDate()));
        List<String> listOfTaskDates = start.datesUntil(end.plusDays(1))
                .map(localDate -> localDate.format(DateTimeFormatter.ofPattern(formatDateToDisplay)))
                .collect(Collectors.toList());
        wrapper.setListOfTaskDates( listOfTaskDates);

        // Datas for effort spent (Axis Y)
        List<EffortHistory> effortSpentDB = this.projectService.getEffortSpentByTaskAndPeriod(actor, task, task.getStartDate(), task.getEndDate());
        final EffortHistory[] lastEffortSpentSum = {new EffortHistory(task.getStartDate(), 0.0)};
        Map<Date, Double> effortSpentMap = listOfTaskDates
                .stream()
                .map(dateString -> {
                    try {
                        return new SimpleDateFormat(formatDateToDisplay).parse(dateString);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    return null;
                })
                .map(date -> effortSpentDB.stream()
                        .filter(es -> new SimpleDateFormat(formatDateToDisplay).format(es.getDate()).equals(new SimpleDateFormat(formatDateToDisplay).format(date)))
                        .map(effort -> {
                            lastEffortSpentSum[0] = new EffortHistory(date, effort.getValue());
                            return lastEffortSpentSum[0];
                        })
                        .findFirst().orElse(new EffortHistory(date, lastEffortSpentSum[0].getValue())))
                .collect(Collectors.toMap(
                        e -> e.getDate(),
                        e -> e.getValue(),
                        (x, y) -> y, LinkedHashMap::new
                ));
        wrapper.setEffortSpentData(effortSpentMap.values());

        // Datas for effort estimate (Axis Y)
        List<EffortHistory> effortLeftDB = this.projectService.getTaskEffortLeftHistory(actor, task);
        final EffortHistory[] lastEffortEstimate = {new EffortHistory(task.getStartDate(), task.getOriginalEstimate())};
        Map<Date, Double> effortEstimateMap = listOfTaskDates
                .stream()
                .map(dateString -> {
                    try {
                        return new SimpleDateFormat(formatDateToDisplay).parse(dateString);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    return null;
                })
                .map(date -> effortLeftDB.stream()
                        .filter(el -> new SimpleDateFormat(formatDateToDisplay).format(el.getDate()).equals(new SimpleDateFormat(formatDateToDisplay).format(date)))
                        .map(effortLeft -> {
                            lastEffortEstimate[0] = new EffortHistory(date, effortLeft.getValue() + effortSpentMap.get(date));
                            return lastEffortEstimate[0];
                        })
                        .findFirst().orElse(new EffortHistory(date, lastEffortEstimate[0].getValue())))
                .collect(Collectors.toMap(
                        e -> e.getDate(),
                        e -> e.getValue(),
                        (x, y) -> y, LinkedHashMap::new
                ));
        wrapper.setRealEffortData(effortEstimateMap.values());

        return Response.ok().entity(MAPPER.writeValueAsString(wrapper)).build();

    }


    @GET
    @Path("/approve")
    public Response approveTask(@Context HttpServletRequest request) {
        User actor = (User) req.getAttribute("actor");
        return this.changeTaskStatus(actor, request,  TaskStatus.IN_PROGRESS);
    }

    @GET
    @Path("/deny")
    public Response denyTask(@Context HttpServletRequest request){
        User actor = (User) req.getAttribute("actor");

        return this.changeTaskStatus(actor, request, TaskStatus.REFUSED);
    }

    private Response changeTaskStatus(User actor, HttpServletRequest request,  TaskStatus status){
        final String taskIdStr = request.getParameter("task");
        Long taskID = null;
        if(taskIdStr != null) {
            taskID = Long.parseLong(taskIdStr);
        }else{
            return Response.status(Response.Status.BAD_REQUEST).entity("Missing argument taskId.").build();
        }
        if(taskID == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Invalid argument taskId.").build();
        }

        Task task;
        try{
            task = (Task) this.projectService.getTaskByID(actor, taskID);
            task.setTaskStatus(status);
            this.projectService.updateTask(actor, task);
        } catch (ClassCastException e){
            return Response.status(Response.Status.BAD_REQUEST).entity("Task is not a project task.").build();
        } catch (Exception e){
            return Response.status(Response.Status.BAD_REQUEST).entity("Task id not found.").build();
        }

        return Response.ok().build();
    }

    @GET
    @Path("/delete")
    public Response deleteTask(@Context HttpServletRequest request) {
        User actor = (User) req.getAttribute("actor");

        final String taskIdStr = request.getParameter("task");
        Long taskID = null;
        if (taskIdStr != null) {
            taskID = Long.parseLong(taskIdStr);
        } else {
            return Response.status(Response.Status.BAD_REQUEST).entity("Missing argument taskId.").build();
        }
        if (taskID == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Invalid argument taskId.").build();
        }

        try {
            projectService.deleteTaskByID(actor, taskID);
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }

        return Response.ok().build();
    }



    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/")
    public Response createTask(TaskWrapper taskWrapper) throws JsonProcessingException {
        User actor = (User) req.getAttribute("actor");
        Date startDate = null;
        Date endDate = null;
        try {
            startDate = DATE_FORMAT.parse(taskWrapper.startDate);
            endDate = DATE_FORMAT.parse(taskWrapper.endDate);

        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Incorrect date format").build();
        }

        if (startDate.getTime() > endDate.getTime()) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Start date must be before end date ").build();
        }

        String name = taskWrapper.taskName;
        String comment = taskWrapper.taskComments;
        if (comment == null) {
            comment = "";
        }
        double oe = taskWrapper.originalEstimate;
        if (oe <= 0.0) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Original original estimate must be positive ").build();
        }

        Long projectID = taskWrapper.projectID;
        Project project = null;
        try {
            project = this.projectService.getProjectByID(actor, projectID);
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }

        Long milestoneID = taskWrapper.milestoneID;
        Milestone milestone = null;
        try {
            milestone = this.projectService.getMilestoneById(actor, milestoneID);
        } catch (Exception e) {
        }


        Task task = null;
        Long typeID = taskWrapper.typeID;

        Long taskID =taskWrapper.taskID;
        if (!(taskID != null && taskID == 0 )) {
            try {
                task = (Task) projectService.getTaskByID(actor, taskID);

                User assignee = userService.findUserByID(taskWrapper.assigneeID);
                final TaskType taskType = this.projectService.findTaskTypeByID(taskWrapper.getTypeID());

                task.setName(taskWrapper.getTaskName());
                task.setComments(taskWrapper.getTaskComments());
                task.setOriginalEstimate(taskWrapper.getOriginalEstimate());
                task.setStartDate(DATE_FORMAT.parse(taskWrapper.getStartDate()));
                task.setEndDate(DATE_FORMAT.parse(taskWrapper.getEndDate()));
                task.setAssigned(assignee);
                task.setTaskType(taskType);
                task.setMilestone(milestone);
                task.setTaskStatus(taskWrapper.getStatus() != null ? TaskStatus.valueOf(taskWrapper.getStatus()) : TaskStatus.PENDING);

                projectService.updateTask(actor,task);
            } catch (Exception e) {
                return Response.status(Response.Status.BAD_REQUEST).entity("Error in task creation please verify your inputs and retry").build();
            }
        } else {
            try {
                task = projectService.createTask(actor, project,
                        name, comment, startDate, endDate, oe, typeID, actor, ProjectService.ORIGIN_TIMEBOARD, null,null,null);
            } catch (Exception e) {
                return Response.status(Response.Status.BAD_REQUEST).header("msg","Error in task creation please verify your inputs and retry. (" + e.getMessage() + ")").build();
            }
        }

        taskWrapper.setTaskID(task.getId());
        return Response.ok().entity(MAPPER.writeValueAsString(taskWrapper)).build();

    }


    public static class TaskGraphWrapper implements Serializable {
        public TaskGraphWrapper() {}

        public List<String> listOfTaskDates;
        public Collection<Double> effortSpentData;
        public Collection<Double> realEffortData;
        
        public void setListOfTaskDates(List<String> listOfTaskDates) {
            this.listOfTaskDates = listOfTaskDates;
        }

        public void setEffortSpentData(Collection<Double> effortSpentData) {
            this.effortSpentData = effortSpentData;

        }

        public void setRealEffortData(Collection<Double> realEffortData) {
            this.realEffortData = realEffortData;
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
        public String status;

        public Long milestoneID;

        public TaskWrapper(){}

        public TaskWrapper(Long taskID, String taskName, String taskComments, double originalEstimate, Date startDate, Date endDate, String assignee, Long assigneeID, String status, Long typeID, Long milestoneID) {
            this.taskID = taskID;
            this.taskName = taskName;
            this.taskComments = taskComments;
            this.originalEstimate = originalEstimate;
            this.startDate = DATE_FORMAT.format(startDate);
            this.endDate = DATE_FORMAT.format(endDate);
            this.assignee = assignee;
            this.assigneeID = assigneeID;
            this.status = status;
            this.typeID = typeID;
            this.milestoneID = milestoneID;
        }

        public Long getMilestoneID() {
            return milestoneID;
        }

        public void setMilestoneID(Long milestoneID) {
            this.milestoneID = milestoneID;
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

        public void setTaskName(String taskName) {
            this.taskName = taskName;
        }

        public String getTaskComments() {
            return taskComments;
        }

        public void setTaskComments(String taskComments) {
            this.taskComments = taskComments;
        }

        public double getOriginalEstimate() {
            return originalEstimate;
        }

        public void setOriginalEstimate(double originalEstimate) {
            this.originalEstimate = originalEstimate;
        }

        public String getStartDate() {
            return startDate;
        }

        public String getAssignee() {
            return assignee;
        }

        public void setAssignee(String assignee) {
            this.assignee = assignee;
        }

        public Long getAssigneeID() {
            return assigneeID;
        }

        public void setAssigneeID(Long assigneeID) {
            this.assigneeID = assigneeID;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public Long getTypeID() {
            return typeID;
        }

        public void setTypeID(Long typeID) {
            this.typeID = typeID;
        }

        public Long getProjectID() {
            return projectID;
        }

        public void setProjectID(Long projectID) {
            this.projectID = projectID;
        }

        public String getEndDate() {
           return this.endDate;
        }
    }
}
