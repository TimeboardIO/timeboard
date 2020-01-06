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
@RequestMapping("/projects/{projectID}/batches")
public class ProjectBatchesController {

    @Autowired
    public ProjectService projectService;


    @GetMapping
    protected String batchApp(TimeboardAuthentication authentication,
                                   @PathVariable Long projectID, Model model) throws  BusinessException {

        final Account actor = authentication.getDetails();
        final Project project = this.projectService.getProjectByID(actor, authentication.getCurrentOrganization(), projectID);

        model.addAttribute("project", project);
        return "project_batches.html";
    }

    @GetMapping(value = "/list", produces = {MediaType.APPLICATION_JSON_VALUE})
    protected ResponseEntity<List<BatchDecorator>> listBatches(TimeboardAuthentication authentication,
                                                                      @PathVariable Long projectID) throws BusinessException {

        final Account actor = authentication.getDetails();
        final Project project = this.projectService.getProjectByID(actor, authentication.getCurrentOrganization(), projectID);

        return ResponseEntity.ok(this.projectService.listProjectBatches(actor, project)
                .stream().map(batch -> new BatchDecorator(batch))
                .collect(Collectors.toList()));
    }


    @GetMapping("/{batchesID/delete")
    protected String deleteBatch(TimeboardAuthentication authentication,
                                     @PathVariable Long projetID, @PathVariable Long batchID) throws BusinessException {

        final Account actor = authentication.getDetails();
        this.projectService.deleteBatchByID(actor, batchID);

        return "redirect:/projects/" + projetID + "/batches";
    }

    @GetMapping("/{batchID}/setup")
    protected String setupBatch(TimeboardAuthentication authentication,
                                    @PathVariable Long projectID,
                                    @PathVariable Long batchID,
                                    Model model) throws BusinessException {

        final Account actor = authentication.getDetails();

        Batch batch = this.projectService.getBatchById(actor, batchID);
        model.addAttribute("batch", batch);
        model.addAttribute("taskIdsByBatch", this.projectService.listTasksByBatch(actor, batch));

        final Project project = this.projectService.getProjectByID(actor, authentication.getCurrentOrganization(), projectID);
        fillModelWithBatches(model, actor, project);

        return "project_batches_config.html";
    }


    @GetMapping("/create")
    protected String createBatchView(TimeboardAuthentication authentication,
                                         @PathVariable Long projectID, Model model) throws BusinessException {
        final Account actor = authentication.getDetails();

        model.addAttribute("batch", new Batch());

        final Project project = this.projectService.getProjectByID(actor, authentication.getCurrentOrganization(), projectID);
        fillModelWithBatches(model, actor, project);

        return "project_batches_config.html";

    }


    private Map<String, String> getCurrentBatchAttributes(HttpServletRequest request) {
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

    private void fillModelWithBatches(Model model, Account actor, Project project) throws BusinessException {
        model.addAttribute("project", project);
        model.addAttribute("batches", this.projectService.listProjectBatches(actor, project));
        model.addAttribute("allProjectTasks", this.projectService.listProjectTasks(actor, project));
        model.addAttribute("allBatchTypes", BatchType.values());
    }


    protected String createConfigLinks(Account actor,
                                       long orgID,
                                       HttpServletRequest request,
                                       Model model) throws BusinessException {

        long projectID = Long.parseLong(request.getParameter("projectID"));
        Project project = this.projectService.getProjectByID(actor, orgID, projectID);
        Batch currentBatch = null;

        try {

            Long batchID = Long.parseLong(request.getParameter("batchID"));
            currentBatch = this.projectService.getBatchById(actor, batchID);
            currentBatch = addTasksToBatch(actor, currentBatch, request);

            model.addAttribute("batch", currentBatch);

            model.addAttribute("allProjectTasks", this.projectService.listProjectTasks(actor, project));

        } catch (Exception e) {
            model.addAttribute("error", e);
        } finally {

            model.addAttribute("project", project);
            model.addAttribute("batches", this.projectService.listProjectBatches(actor, project));
            model.addAttribute("allBatchTypes", BatchType.values());
            model.addAttribute("taskIdsByBatch", this.projectService.listTasksByBatch(actor, currentBatch));
            return "details_project_batches_config_links.html";
        }
    }

    private Batch addTasksToBatch(Account actor,
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
        List<Task> oldTasks = this.projectService.listTasksByBatch(actor, currentBatch);

        return this.projectService.addTasksToBatch(actor, currentBatch, selectedTasks, oldTasks);
    }


    public class BatchDecorator {

        private Batch batch;

        public BatchDecorator(Batch batch) {
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
