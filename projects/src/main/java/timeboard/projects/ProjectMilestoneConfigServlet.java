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

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;
import timeboard.core.api.ProjectService;
import timeboard.core.api.UserService;
import timeboard.core.model.*;
import timeboard.core.ui.TimeboardServlet;
import timeboard.core.ui.ViewModel;
import timeboard.security.SecurityContext;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Component(
        service = Servlet.class,
        scope = ServiceScope.PROTOTYPE,
        property = {
                "osgi.http.whiteboard.servlet.pattern=/projects/milestones/config",
                "osgi.http.whiteboard.context.select=(osgi.http.whiteboard.context.name=timeboard)"
        }
)
public class ProjectMilestoneConfigServlet extends TimeboardServlet {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    @Reference
    public ProjectService projectService;

    @Reference
    public UserService userService;


    @Override
    protected ClassLoader getTemplateResolutionClassLoader() {
        return ProjectMilestoneConfigServlet.class.getClassLoader();
    }

    @Override
    protected void handleGet(HttpServletRequest request, HttpServletResponse response, ViewModel viewModel) throws ServletException, IOException {
        if (request.getParameter("milestoneID") != null) {
            // Update case
            long milestoneID = Long.parseLong(request.getParameter("milestoneID"));
            Milestone milestone = this.projectService.getMilestoneById(milestoneID);
            viewModel.getViewDatas().put("milestone", milestone);
        } else {
            // New milestone case
            viewModel.getViewDatas().put("milestone", new Milestone());
        }

        long projectID = Long.parseLong(request.getParameter("projectID"));
        Project project = this.projectService.getProjectByID(SecurityContext.getCurrentUser(request), projectID);

        viewModel.setTemplate("projects:details_project_milestones_config.html");
        viewModel.getViewDatas().put("project", project);
        viewModel.getViewDatas().put("milestones", this.projectService.listProjectMilestones(project));
        viewModel.getViewDatas().put("milestoneTypes", MilestoneType.values());

    }

    @Override
    protected void handlePost(HttpServletRequest request, HttpServletResponse response, ViewModel viewModel) throws ServletException, IOException {

        long projectID = Long.parseLong(request.getParameter("projectID"));
        Project project = this.projectService.getProjectByID(SecurityContext.getCurrentUser(request), projectID);
        Milestone currentMilestone = null;

        try {

            if (!getParameter(request, "milestoneID").get().isEmpty()) {
                Long milestoneID = Long.parseLong(request.getParameter("milestoneID"));
                currentMilestone = this.projectService.getMilestoneById(milestoneID);
                currentMilestone = updateMilestone(currentMilestone, request);
            } else {
                currentMilestone = createMilestone(request);
            }

            viewModel.getViewDatas().put("milestone", currentMilestone);


        } catch (Exception e) {
            viewModel.getErrors().add(e);
        } finally {
            viewModel.setTemplate("projects:details_project_milestones_config.html");

            viewModel.getViewDatas().put("project", project);
            viewModel.getViewDatas().put("milestones", this.projectService.listProjectMilestones(project));
            viewModel.getViewDatas().put("milestoneTypes", MilestoneType.values());
        }
    }

    private Milestone createMilestone(HttpServletRequest request) throws ParseException {
        String name = request.getParameter("milestoneName");
        Date date = new Date(DATE_FORMAT.parse(request.getParameter("milestoneDate")).getTime()+(2 * 60 * 60 * 1000) +1);
        MilestoneType type = request.getParameter("milestoneType") != null ? MilestoneType.valueOf(request.getParameter("milestoneType")) : MilestoneType.DELIVERY;
        Map<String, String> attributes = new HashMap<>();
        Set<Task> tasks = new HashSet<>();

        return this.projectService.createMilestone(name, date, type, attributes, tasks);
    }

    private Milestone updateMilestone(Milestone currentMilestone, HttpServletRequest request) throws ParseException {

        currentMilestone.setName(request.getParameter("milestoneName"));
        currentMilestone.setDate(new Date(DATE_FORMAT.parse(request.getParameter("milestoneDate")).getTime()+(2 * 60 * 60 * 1000) +1));
        currentMilestone.setType(request.getParameter("milestoneType") != null ? MilestoneType.valueOf(request.getParameter("milestoneType")) : MilestoneType.DELIVERY);
        currentMilestone.setAttributes(new HashMap<>());
        currentMilestone.setTasks(new HashSet<>());

        return this.projectService.updateMilestone(currentMilestone);
    }
}
