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
import timeboard.core.api.ProjectService;
import timeboard.core.api.ProjectSnapshotService;
import timeboard.core.api.exceptions.BusinessException;
import timeboard.core.model.Account;
import timeboard.core.model.Project;
import timeboard.core.model.ProjectSnapshot;
import timeboard.core.model.Task;
import timeboard.core.ui.UserInfo;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/projects/{projectID}/snapshots")
public class ProjectSnapshotsController {

    @Autowired
    private ProjectService projectService;

    @Autowired
    private ProjectSnapshotService projectSnapshotService;

    @Autowired
    private UserInfo userInfo;

    @GetMapping
    public String display(@PathVariable Long projectID, Model model) throws BusinessException {

        final Account actor = this.userInfo.getCurrentAccount();
        final Project project = this.projectService.getProjectByID(actor, projectID);

        model.addAttribute("project", project);

        return "details_project_snapshots";
    }

    @GetMapping(value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ProjectSnapshotsController.ProjectSnapshotWrapper>> listProjectSnapshots(@PathVariable Long projectID) throws BusinessException {
        final Account actor = this.userInfo.getCurrentAccount();
        final Project project = this.projectService.getProjectByID(actor, projectID);
        return ResponseEntity.ok(project.getSnapshots().stream().map(projectSnapshot -> new ProjectSnapshotsController.ProjectSnapshotWrapper(projectSnapshot)).collect(Collectors.toList()));
    }

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ProjectSnapshotWrapper>> createSnapshot(@PathVariable Long projectID) throws BusinessException {
        final Account actor = this.userInfo.getCurrentAccount();
        final Project project = this.projectService.getProjectByID(actor, projectID);
        final ProjectSnapshot projectSnapshot = this.projectSnapshotService.createProjectSnapshot(actor, project);
        projectSnapshot.setProject(project);
        project.getSnapshots().add(projectSnapshot);
        this.projectService.updateProject(actor, project);
        return this.listProjectSnapshots(projectID);
    }

    public static class ProjectSnapshotWrapper {

        private double quotation;
        private double realEffort;
        private double effortSpent;
        private double effortLeft;
        private double originalEstimate;
        private Date projectSnapshotDate;
        private Long id;

        public ProjectSnapshotWrapper(ProjectSnapshot projectSnapshot) {
            this.projectSnapshotDate = projectSnapshot.getProjectSnapshotDate();
            this.originalEstimate = projectSnapshot.getOriginalEstimate();
            this.effortLeft = projectSnapshot.getEffortLeft();
            this.effortSpent = projectSnapshot.getEffortSpent();
            this.realEffort = projectSnapshot.getRealEffort();
            this.quotation = projectSnapshot.getQuotation();
            this.id = projectSnapshot.getId();
        }

        public double getQuotation() { return quotation; }

        public double getRealEffort() { return realEffort; }

        public double getEffortSpent() { return effortSpent; }

        public double getEffortLeft() { return effortLeft; }

        public double getOriginalEstimate() { return originalEstimate; }

        public Date getProjectSnapshotDate() { return projectSnapshotDate; }

        public Long getId() { return id; }

        public void setQuotation(double quotation) { this.quotation = quotation; }

        public void setRealEffort(double realEffort) { this.realEffort = realEffort; }

        public void setEffortSpent(double effortSpent) { this.effortSpent = effortSpent; }

        public void setEffortLeft(double effortLeft) { this.effortLeft = effortLeft; }

        public void setOriginalEstimate(double originalEstimate) { this.originalEstimate = originalEstimate; }

        public void setProjectSnapshotDate(Date projectSnapshotDate) { this.projectSnapshotDate = projectSnapshotDate; }

        public void setId(Long id) { this.id = id; }
    }
}
