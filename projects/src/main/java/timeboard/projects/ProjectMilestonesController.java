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
import org.springframework.web.bind.annotation.RequestMapping;
import timeboard.core.api.ProjectService;
import timeboard.core.api.exceptions.BusinessException;
import timeboard.core.model.*;
import timeboard.core.ui.UserInfo;
import timeboard.core.ui.ViewModel;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/projects/{projectID}/milestones")
public class ProjectMilestonesController {

    @Autowired
    public ProjectService projectService;

    @Autowired
    private UserInfo userInfo;

    @GetMapping
    protected String listMilestones(@PathVariable Long projectID, Model viewModel) throws ServletException, IOException, BusinessException {

        final Account actor = this.userInfo.getCurrentAccount();
        final Project project = this.projectService.getProjectByID(actor, projectID);

        viewModel.addAttribute("milestones", this.projectService.listProjectMilestones(actor, project));
        viewModel.addAttribute("project", project);

        return "details_project_milestones";
    }

    @GetMapping("/{milestoneID/delete")
    protected String deleteMilestone(@PathVariable Long projetID, @PathVariable Long milestoneID) throws ServletException, IOException, BusinessException {

        final Account actor = this.userInfo.getCurrentAccount();
        this.projectService.deleteMilestoneByID(actor, milestoneID);

        return "redirect:/projects/" + projetID + "/milestones";
    }

    @GetMapping("/{milestoneID}/setup")
    protected String setupMilestone(@PathVariable Long projectID, @PathVariable Long milestoneID, Model viewModel) throws BusinessException {
        final Account actor = this.userInfo.getCurrentAccount();

        Milestone milestone = this.projectService.getMilestoneById(actor, milestoneID);
        viewModel.addAttribute("milestone", milestone);
        viewModel.addAttribute("taskIdsByMilestone", this.projectService.listTasksByMilestone(actor, milestone));

        final Project project = this.projectService.getProjectByID(actor, projectID);
        fillModelWithMilestones(viewModel, actor, project);

        return "details_project_milestones_config";
    }


    @GetMapping("/create")
    protected String createMilestoneView(@PathVariable Long projectID, Model viewModel) throws BusinessException {
        final Account actor = this.userInfo.getCurrentAccount();

        viewModel.addAttribute("milestone", new Milestone());

        final Project project = this.projectService.getProjectByID(actor, projectID);
        fillModelWithMilestones(viewModel, actor, project);

        return "details_project_milestones_config";

    }


    private Map<String, String> getCurrentMilestoneAttributes(HttpServletRequest request) {
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
                String key = param.substring(5);
                String value = request.getParameter(param);
                attributes.put(key, value);
            }
        }
        return attributes;
    }

    private void fillModelWithMilestones(Model viewModel, Account actor, Project project) throws BusinessException {
        viewModel.addAttribute("project", project);
        viewModel.addAttribute("milestones", this.projectService.listProjectMilestones(actor, project));
        viewModel.addAttribute("allProjectTasks", this.projectService.listProjectTasks(actor, project));
        viewModel.addAttribute("allMilestoneTypes", MilestoneType.values());
    }


    protected void createConfigLinks(Account actor, HttpServletRequest request, HttpServletResponse response, ViewModel viewModel) throws ServletException, IOException, BusinessException {

        long projectID = Long.parseLong(request.getParameter("projectID"));
        Project project = this.projectService.getProjectByID(actor, projectID);
        Milestone currentMilestone = null;

        try {

            Long milestoneID = Long.parseLong(request.getParameter("milestoneID"));
            currentMilestone = this.projectService.getMilestoneById(actor, milestoneID);
            currentMilestone = addTasksToMilestone(actor, currentMilestone, request);


            viewModel.getViewDatas().put("milestone", currentMilestone);

            viewModel.getViewDatas().put("allProjectTasks", this.projectService.listProjectTasks(actor, project));

        } catch (Exception e) {
            viewModel.getErrors().add(e);
        } finally {
            viewModel.setTemplate("details_project_milestones_config_links.html");

            viewModel.getViewDatas().put("project", project);
            viewModel.getViewDatas().put("milestones", this.projectService.listProjectMilestones(actor, project));
            viewModel.getViewDatas().put("allMilestoneTypes", MilestoneType.values());
            viewModel.getViewDatas().put("taskIdsByMilestone", this.projectService.listTasksByMilestone(actor, currentMilestone));
        }
    }

    private Milestone addTasksToMilestone(Account actor, Milestone currentMilestone, HttpServletRequest request) throws BusinessException {
        String[] selectedTaskIdsString = request.getParameterValues("taskSelected");
        List<Task> selectedTasks = Arrays
                .stream(selectedTaskIdsString)
                .map(id -> {
                    Task t = null;
                    try {
                        t = (Task) projectService.getTaskByID(actor, Long.getLong(id));
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        return t;
                    }
                }).collect(Collectors.toList());
        List<Task> oldTasks = this.projectService.listTasksByMilestone(actor, currentMilestone);

        return this.projectService.addTasksToMilestone(actor, currentMilestone, selectedTasks, oldTasks);
    }


}
