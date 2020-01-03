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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import timeboard.core.TimeboardAuthentication;
import timeboard.core.api.ProjectService;
import timeboard.core.api.exceptions.BusinessException;
import timeboard.core.model.*;


import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/projects/{projectID}/milestones")
public class ProjectMilestonesController {

    @Autowired
    public ProjectService projectService;


    @GetMapping
    protected String milestonesApp(TimeboardAuthentication authentication,
                                   @PathVariable Long projectID, Model model) throws  BusinessException {

        final Account actor = authentication.getDetails();
        final Project project = this.projectService.getProjectByID(actor, projectID);

        model.addAttribute("project", project);
        return "project_milestones.html";
    }

    @GetMapping(value = "/list", produces = {MediaType.APPLICATION_JSON_VALUE})
    protected ResponseEntity<List<MilestoneDecorator>> listMilestones(TimeboardAuthentication authentication,
                                                                      @PathVariable Long projectID) throws BusinessException {

        final Account actor = authentication.getDetails();
        final Project project = this.projectService.getProjectByID(actor, projectID);

        return ResponseEntity.ok(this.projectService.listProjectMilestones(actor, project)
                .stream().map(milestone -> new MilestoneDecorator(milestone))
                .collect(Collectors.toList()));
    }


    @GetMapping("/{milestoneID/delete")
    protected String deleteMilestone(TimeboardAuthentication authentication,
                                     @PathVariable Long projetID, @PathVariable Long milestoneID) throws BusinessException {

        final Account actor = authentication.getDetails();
        this.projectService.deleteMilestoneByID(actor, milestoneID);

        return "redirect:/projects/" + projetID + "/milestones";
    }

    @GetMapping("/{milestoneID}/setup")
    protected String setupMilestone(TimeboardAuthentication authentication,
                                    @PathVariable Long projectID,
                                    @PathVariable Long milestoneID,
                                    Model model) throws BusinessException {

        final Account actor = authentication.getDetails();

        Batch batch = this.projectService.getMilestoneById(actor, milestoneID);
        model.addAttribute("milestone", batch);
        model.addAttribute("taskIdsByMilestone", this.projectService.listTasksByMilestone(actor, batch));

        final Project project = this.projectService.getProjectByID(actor, projectID);
        fillModelWithMilestones(model, actor, project);

        return "project_milestones_config.html";
    }


    @GetMapping("/create")
    protected String createMilestoneView(TimeboardAuthentication authentication,
                                         @PathVariable Long projectID, Model model) throws BusinessException {
        final Account actor = authentication.getDetails();

        model.addAttribute("milestone", new Batch());

        final Project project = this.projectService.getProjectByID(actor, projectID);
        fillModelWithMilestones(model, actor, project);

        return "project_milestones_config.html";

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

    private void fillModelWithMilestones(Model model, Account actor, Project project) throws BusinessException {
        model.addAttribute("project", project);
        model.addAttribute("milestones", this.projectService.listProjectMilestones(actor, project));
        model.addAttribute("allProjectTasks", this.projectService.listProjectTasks(actor, project));
        model.addAttribute("allMilestoneTypes", BatchType.values());
    }


    protected String createConfigLinks(Account actor,
                                       HttpServletRequest request,
                                       Model model) throws BusinessException {

        long projectID = Long.parseLong(request.getParameter("projectID"));
        Project project = this.projectService.getProjectByID(actor, projectID);
        Batch currentBatch = null;

        try {

            Long milestoneID = Long.parseLong(request.getParameter("milestoneID"));
            currentBatch = this.projectService.getMilestoneById(actor, milestoneID);
            currentBatch = addTasksToMilestone(actor, currentBatch, request);

            model.addAttribute("milestone", currentBatch);

            model.addAttribute("allProjectTasks", this.projectService.listProjectTasks(actor, project));

        } catch (Exception e) {
            model.addAttribute("error", e);
        } finally {

            model.addAttribute("project", project);
            model.addAttribute("milestones", this.projectService.listProjectMilestones(actor, project));
            model.addAttribute("allMilestoneTypes", BatchType.values());
            model.addAttribute("taskIdsByMilestone", this.projectService.listTasksByMilestone(actor, currentBatch));
            return "details_project_milestones_config_links.html";
        }
    }

    private Batch addTasksToMilestone(Account actor,
                                      Batch currentBatch,
                                      HttpServletRequest request) throws BusinessException {

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
        List<Task> oldTasks = this.projectService.listTasksByMilestone(actor, currentBatch);

        return this.projectService.addTasksToMilestone(actor, currentBatch, selectedTasks, oldTasks);
    }


    public class MilestoneDecorator {

        private Batch batch;

        public MilestoneDecorator(Batch batch) {
            this.batch = batch;
        }

        public String getName() {
            return this.batch.getName();
        }

        public BatchType getType() {
            return this.batch.getType();
        }

        public Date getDate() {
            return this.batch.getDate();
        }

    }
}
