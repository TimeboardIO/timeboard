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
import timeboard.core.api.exceptions.BusinessException;
import timeboard.core.model.*;

import timeboard.core.ui.TimeboardServlet;
import timeboard.core.ui.ViewModel;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

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


    @Override
    protected ClassLoader getTemplateResolutionClassLoader() {
        return ProjectMilestoneConfigServlet.class.getClassLoader();
    }

    @Override
    protected void handleGet(User actor, HttpServletRequest request, HttpServletResponse response, ViewModel viewModel) throws ServletException, IOException, BusinessException {
        if (request.getParameter("milestoneID") != null) {
            // Update case
            long milestoneID = Long.parseLong(request.getParameter("milestoneID"));
            try {
            Milestone milestone = this.projectService.getMilestoneById(actor, milestoneID);
            viewModel.getViewDatas().put("milestone", milestone);
            viewModel.getViewDatas().put("taskIdsByMilestone", this.projectService.listTasksByMilestone(actor, milestone));
            } catch (BusinessException e) {
                viewModel.getErrors().add(e);
            }
        } else {
            // New milestone case
            viewModel.getViewDatas().put("milestone", new Milestone());
        }

        long projectID = Long.parseLong(request.getParameter("projectID"));
        Project project = null;
        try {
            project = this.projectService.getProjectByID(actor, projectID);
            viewModel.getViewDatas().put("project", project);
            viewModel.getViewDatas().put("milestones", this.projectService.listProjectMilestones(actor, project));
            viewModel.getViewDatas().put("allProjectTasks", this.projectService.listProjectTasks(actor, project));
        } catch (BusinessException e) {
            viewModel.getErrors().add(e);
        }

        viewModel.getViewDatas().put("allMilestoneTypes", MilestoneType.values());
        viewModel.setTemplate("projects:details_project_milestones_config.html");

    }

    @Override
    protected void handlePost(User actor, HttpServletRequest request, HttpServletResponse response, ViewModel viewModel)
            throws ServletException, IOException, BusinessException, ParseException {

        long projectID = Long.parseLong(request.getParameter("projectID"));
        Project project = this.projectService.getProjectByID(actor, projectID);
        viewModel.getViewDatas().put("project", project);
        Milestone currentMilestone = null;
        viewModel.setTemplate("projects:details_project_milestones_config.html");

        if (!getParameter(request, "milestoneID").get().isEmpty()) {
            Long milestoneID = Long.parseLong(request.getParameter("milestoneID"));
            currentMilestone = this.projectService.getMilestoneById(actor, milestoneID);
            currentMilestone = updateMilestone(actor, currentMilestone, project, request);
        } else {
            currentMilestone = createMilestone(actor, project, request);
        }

        viewModel.getViewDatas().put("milestone", currentMilestone);
        viewModel.getViewDatas().put("taskIdsByMilestone", this.projectService.listTasksByMilestone(actor, currentMilestone));
        viewModel.getViewDatas().put("milestones", this.projectService.listProjectMilestones(actor, project));
        viewModel.getViewDatas().put("allMilestoneTypes", MilestoneType.values());
        viewModel.getViewDatas().put("allProjectTasks", this.projectService.listProjectTasks(actor, project));

    }

    private Milestone createMilestone(User actor, Project project, HttpServletRequest request) throws ParseException, BusinessException {
        String name = request.getParameter("milestoneName");
        Date date = new Date(DATE_FORMAT.parse(request.getParameter("milestoneDate")).getTime()+(2 * 60 * 60 * 1000) +1);
        MilestoneType type = request.getParameter("milestoneType") != null ? MilestoneType.valueOf(request.getParameter("milestoneType")) : MilestoneType.DELIVERY;
        Map<String, String> attributes = this.getCurrentMilestoneAttributes(request);
        Set<Task> tasks = new HashSet<>();

        return this.projectService.createMilestone(actor, name, date, type, attributes, tasks, project);
    }

    private Milestone updateMilestone(User actor, Milestone currentMilestone, Project project, HttpServletRequest request) throws ParseException, BusinessException {

        currentMilestone.setName(request.getParameter("milestoneName"));
        currentMilestone.setDate(new Date(DATE_FORMAT.parse(request.getParameter("milestoneDate")).getTime()+(2 * 60 * 60 * 1000) +1));
        currentMilestone.setType(request.getParameter("milestoneType") != null ? MilestoneType.valueOf(request.getParameter("milestoneType")) : MilestoneType.DELIVERY);
        currentMilestone.setAttributes(this.getCurrentMilestoneAttributes(request));
        currentMilestone.setTasks(new HashSet<>());
        currentMilestone.setProject(project);

        return this.projectService.updateMilestone(actor, currentMilestone);
    }

    private Map<String, String> getCurrentMilestoneAttributes(HttpServletRequest request){
        Map<String, String> attributes = new HashMap<>();

        String newAttrKey = request.getParameter("newAttrKey");
        String newAttrValue = request.getParameter("newAttrValue");
        if (!newAttrKey.isEmpty()) {
            attributes.put(newAttrKey, newAttrValue);
        }
        Enumeration<String> paramNames = request.getParameterNames();
        while (paramNames.hasMoreElements()) {
            String param = paramNames.nextElement();
            if (param.startsWith("attr-")) {
                String key = param.substring(5, param.length());
                String value = request.getParameter(param);
                attributes.put(key, value);
            }
        }
        return attributes;
    }
}
