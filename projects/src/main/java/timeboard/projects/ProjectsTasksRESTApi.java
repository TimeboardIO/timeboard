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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicyOption;
import timeboard.core.api.ProjectService;
import timeboard.core.model.*;
import timeboard.core.ui.TimeboardServlet;
import timeboard.security.SecurityContext;

import javax.servlet.Servlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

@Component(
        service = Servlet.class,
        property = {
                "osgi.http.whiteboard.servlet.pattern=/projects/tasks/api",
                "osgi.http.whiteboard.context.select=(osgi.http.whiteboard.context.name=timeboard)"
        }

)
public class ProjectsTasksRESTApi extends TimeboardServlet {

    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Reference(cardinality = ReferenceCardinality.OPTIONAL, policyOption = ReferencePolicyOption.GREEDY)
    private ProjectService projectService;

    @Override
    protected ClassLoader getTemplateResolutionClassLoader() {
        return ProjectsTasksRESTApi.class.getClassLoader();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        final String action = request.getParameter("action");

        response.setContentType("application/json");

        if(action.matches("getPendingTasks")){
            this.getPendingTasks(request, response);
        }else if(action.matches("approveTask")){
            this.approveTask(request, response);
        }else if(action.matches("denyTask")){
            this.denyTask(request, response);
        }else{
            MAPPER.writeValue(response.getWriter(), "Unknown action.");
            return;
        }
    }

    private void approveTask(HttpServletRequest request, HttpServletResponse response) throws IOException {
       this.changeTaskStatus(request, response, TaskStatus.IN_PROGESS);
    }

    private void denyTask(HttpServletRequest request, HttpServletResponse response) throws IOException {
        this.changeTaskStatus(request, response, TaskStatus.REFUSED);
    }

    private void changeTaskStatus(HttpServletRequest request, HttpServletResponse response, TaskStatus status) throws IOException {
        final String taskIdStr = request.getParameter("taskId");
        Long taskId = null;
        if(taskIdStr != null) {
            taskId = Long.parseLong(taskIdStr);
        }else{
            MAPPER.writeValue(response.getWriter(), "Missing argument taskId.");
            return;
        }
        if(taskId == null) {
            MAPPER.writeValue(response.getWriter(), "Invalid argument taskId.");
            return;
        }

        Task task;
        try{
            task = (Task) this.projectService.getTask(taskId);
        } catch (ClassCastException e){
            MAPPER.writeValue(response.getWriter(), "Task is not a project task.");
            return;
        } catch (Exception e){
            MAPPER.writeValue(response.getWriter(), "Task id not found.");
            return;
        }
        final TaskRevision lastRevision = task.getLatestRevision();
        final TaskRevision newRevision = new TaskRevision(SecurityContext.getCurrentUser(request), task,
                lastRevision.getRemainsToBeDone(),lastRevision.getAssigned(), status);
        this.projectService.updateTask(SecurityContext.getCurrentUser(request),task, newRevision);

        MAPPER.writeValue(response.getWriter(), "DONE");
        return;
    }

    private void getPendingTasks(HttpServletRequest request, HttpServletResponse response) throws IOException {

        final String strProjectID = request.getParameter("project");
        Long projectID = null;
        if(strProjectID != null){
            projectID = Long.parseLong(strProjectID);
        }else{
            MAPPER.writeValue(response.getWriter(), "Incorrect project argument");
            return;
        }
        final User currentUser = SecurityContext.getCurrentUser(request);
        final Project project = this.projectService.getProjectByID(currentUser, projectID);
        if(project == null){
            MAPPER.writeValue(response.getWriter(), "Project does not exists or you don't have enough permissions to access it.");
            return;
        }
        final List<Task> tasks = this.projectService.listProjectTasks(project);

        final Map<Long, UserTasksWrapper> result = new HashMap<>();

        for (Task task : tasks){
            if(task.getLatestRevision().getTaskStatus() == TaskStatus.PENDING){
                User assignee = task.getLatestRevision().getAssigned();
                if(assignee == null){
                    assignee = new User();
                    assignee.setId(0);
                    assignee.setName("");
                    assignee.setFirstName("");
                }
                final User finalAssignee = assignee;
                UserTasksWrapper userWrapper = result.computeIfAbsent(assignee.getId(), k-> new UserTasksWrapper(finalAssignee.getFirstName(), finalAssignee.getName(), finalAssignee.getId()));
                userWrapper.getTasks()
                        .add(new TaskWrapper(
                                task.getId(),
                                task.getName(),
                                task.getComments(),
                                task.getEstimateWork(),
                                task.getStartDate(),
                                task.getEndDate())
                        );
            }
        }

        response.setContentType("application/json");
        MAPPER.writeValue(response.getWriter(), new ArrayList<>(result.values()));
    }
    public static class UserTasksWrapper{

        private final String firstName;
        private final String name;
        private final Long id;

        private final List<TaskWrapper> tasks;

        public UserTasksWrapper(String firstName, String name, Long id) {
            this.firstName = firstName;
            this.name = name;
            this.id = id;
            this.tasks = new ArrayList<>();
        }

        public String getFirstName() {
            return firstName;
        }

        public String getName() {
            return name;
        }

        public Long getId() {
            return id;
        }

        public List<TaskWrapper> getTasks() {
            return tasks;
        }

        @Override
        public boolean equals(Object o) {
            return ((o instanceof UserTasksWrapper) && ((UserTasksWrapper) o).getId() == this.id);
        }

    }

    public static class TaskWrapper {
        private final Long taskID;
        private final String taskName;
        private final String taskComment;
        private final double estimateWork;
        private final Date startDate;
        private final Date endDate;
        public TaskWrapper(Long taskID, String taskName, String taskComment, double estimateWork, Date startDate, Date endDate) {
            this.taskID = taskID;
            this.taskName = taskName;
            this.taskComment = taskComment;
            this.estimateWork = estimateWork;
            this.startDate = startDate;
            this.endDate = (endDate != null ? endDate : new Date());
        }

        public String getStartDate() {
            return DATE_FORMAT.format(startDate);
        }

        public String getEndDate() {
            return DATE_FORMAT.format(endDate);
        }

        public double getEstimateWork() {
            return estimateWork;
        }

        public Long getTaskID() {
            return taskID;
        }

        public String getTaskName() {
            return taskName;
        }

        public String getTaskComment() {
            return taskComment;
        }
    }

}
