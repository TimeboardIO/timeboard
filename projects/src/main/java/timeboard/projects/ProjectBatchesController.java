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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import timeboard.core.api.ProjectService;
import timeboard.core.api.exceptions.BusinessException;
import timeboard.core.model.*;
import timeboard.core.security.TimeboardAuthentication;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/projects/{projectID}/batches")
public class ProjectBatchesController {

    @Autowired
    public ProjectService projectService;

    @GetMapping(value = "/{batchID}/delete", produces = MediaType.APPLICATION_JSON_VALUE)
    public String deleteBatch(
            final TimeboardAuthentication authentication,
            @PathVariable final Long projectID,
            @PathVariable final Long batchID,
            final RedirectAttributes attributes) {

        final Account actor = authentication.getDetails();
        try {
            this.projectService.deleteBatchByID(actor, batchID);
        } catch (final BusinessException e) {
            attributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/projects/" + projectID + "/batches";
    }

    @GetMapping
    protected String batchApp(final TimeboardAuthentication authentication,
                              @PathVariable final Long projectID, final Model model) throws BusinessException {

        final Account actor = authentication.getDetails();
        final Project project = this.projectService.getProjectByID(actor, authentication.getCurrentOrganization(), projectID);

        model.addAttribute("project", project);
        model.addAttribute("batchTypes", BatchType.values());

        return "project_batches.html";
    }

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    protected ResponseEntity<BatchDecorator> createBatch(final TimeboardAuthentication authentication,
                                                         @ModelAttribute final BatchWrapper batch,
                                                         @PathVariable final Long projectID) throws BusinessException {


        final Account actor = authentication.getDetails();
        final Project project = this.projectService.getProjectByID(actor, authentication.getCurrentOrganization(), projectID);

        if (batch.getId() == null) {
            final Batch newBatch = this.projectService.createBatch(
                    actor,
                    batch.getName(),
                    batch.getDate(),
                    batch.getType(),
                    new HashMap<>(),
                    new HashSet<>(),
                    project);

            return ResponseEntity.ok(new BatchDecorator(newBatch));
        } else {

            final Batch dbBatch = this.projectService.getBatchById(actor, batch.getId());
            batch.updpate(dbBatch);
            this.projectService.updateBatch(actor, dbBatch);

            return ResponseEntity.ok(new BatchDecorator(dbBatch));
        }
    }


    @GetMapping(value = "/list", produces = {MediaType.APPLICATION_JSON_VALUE})
    protected ResponseEntity<List<BatchDecorator>> listBatches(final TimeboardAuthentication authentication,
                                                               @PathVariable final Long projectID) throws BusinessException {

        final Account actor = authentication.getDetails();
        final Project project = this.projectService.getProjectByID(actor, authentication.getCurrentOrganization(), projectID);

        return ResponseEntity.ok(this.projectService.listProjectBatches(actor, project)
                .stream().map(batch -> new BatchDecorator(batch))
                .collect(Collectors.toList()));
    }


    @GetMapping(value = "/{batchID}", produces = MediaType.APPLICATION_JSON_VALUE)
    protected ResponseEntity<BatchDecorator> setupBatch(final TimeboardAuthentication authentication,
                                                        @PathVariable final Long projectID,
                                                        @PathVariable final Long batchID,
                                                        final Model model) throws BusinessException {

        final Account actor = authentication.getDetails();

        final Batch batch = this.projectService.getBatchById(actor, batchID);
        model.addAttribute("batch", batch);
        model.addAttribute("taskIdsByBatch", this.projectService.listTasksByBatch(actor, batch));


        return ResponseEntity.ok(new BatchDecorator(batch));
    }


    @GetMapping("/create")
    protected String createBatchView(final TimeboardAuthentication authentication,
                                     @PathVariable final Long projectID, final Model model) throws BusinessException {
        final Account actor = authentication.getDetails();

        model.addAttribute("batch", new Batch());

        final Project project = this.projectService.getProjectByID(actor, authentication.getCurrentOrganization(), projectID);
        this.fillModelWithBatches(model, actor, project);

        return "project_batches_config.html";

    }


    private void fillModelWithBatches(final Model model, final Account actor, final Project project) throws BusinessException {
        model.addAttribute("project", project);
        model.addAttribute("batches", this.projectService.listProjectBatches(actor, project));
        model.addAttribute("allProjectTasks", this.projectService.listProjectTasks(actor, project));
        model.addAttribute("allBatchTypes", BatchType.values());
    }


    protected String createConfigLinks(final Account actor,
                                       final long orgID,
                                       final HttpServletRequest request,
                                       final Model model) throws BusinessException {

        final long projectID = Long.parseLong(request.getParameter("projectID"));
        final Project project = this.projectService.getProjectByID(actor, orgID, projectID);
        Batch currentBatch = null;

        try {

            final Long batchID = Long.parseLong(request.getParameter("batchID"));
            currentBatch = this.projectService.getBatchById(actor, batchID);
            currentBatch = addTasksToBatch(actor, currentBatch, request);

            model.addAttribute("batch", currentBatch);

            model.addAttribute("allProjectTasks", this.projectService.listProjectTasks(actor, project));

        } catch (final Exception e) {
            model.addAttribute("error", e);
        }

        model.addAttribute("project", project);
        model.addAttribute("batches", this.projectService.listProjectBatches(actor, project));
        model.addAttribute("allBatchTypes", BatchType.values());
        model.addAttribute("taskIdsByBatch", this.projectService.listTasksByBatch(actor, currentBatch));
        return "details_project_batches_config_links.html";

    }

    private Batch addTasksToBatch(final Account actor,
                                  final Batch currentBatch,
                                  final HttpServletRequest request) throws BusinessException {

        final String[] selectedTaskIdsString = request.getParameterValues("taskSelected");
        final List<Task> selectedTasks = Arrays
                .stream(selectedTaskIdsString)
                .map(id -> {
                    Task t = null;
                    try {
                        t = (Task) projectService.getTaskByID(actor, Long.getLong(id));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return t;

                }).collect(Collectors.toList());
        final List<Task> oldTasks = this.projectService.listTasksByBatch(actor, currentBatch);

        return this.projectService.addTasksToBatch(actor, currentBatch, selectedTasks, oldTasks);
    }

    public class BatchWrapper {

        private Long id;
        private String name;
        private BatchType type;
        private Date date;

        public Long getId() {
            return id;
        }

        public void setId(final Long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(final String name) {
            this.name = name;
        }

        public BatchType getType() {
            return type;
        }

        public void setType(final BatchType type) {
            this.type = type;
        }

        public Date getDate() {
            return date;
        }

        public void setDate(final Date date) {
            this.date = date;
        }

        public void updpate(final Batch dbBatch) {
            dbBatch.setName(this.getName());
            dbBatch.setType(this.getType());
            dbBatch.setDate(this.getDate());
        }
    }

    public class BatchDecorator {

        private Batch batch = new Batch();

        public BatchDecorator() {
        }

        public BatchDecorator(final Batch batch) {
            this.batch = batch;
        }

        public Long getId() {
            return this.batch.getId();
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

        public Integer getTasks() {
            return this.batch.getTasks().size();
        }


    }
}
