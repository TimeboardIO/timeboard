package timeboard.core.model;

/*-
 * #%L
 * core
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

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
public class ProjectSnapshot extends OrganizationEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column
    @Temporal(TemporalType.TIMESTAMP)
    private Date projectSnapshotDate;

    @OneToMany(targetEntity = TaskSnapshot.class, mappedBy = "projectSnapshot", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TaskSnapshot> taskSnapshots;

    @ManyToOne(targetEntity = Project.class, fetch = FetchType.EAGER)
    private Project project;

    @Column(nullable = false)
    private double originalEstimate;

    @Column(nullable = false)
    private double effortLeft;

    @Column(nullable = false)
    private double effortSpent;

    @Column(nullable = false)
    private double quotation;

    public ProjectSnapshot() {
        this.taskSnapshots = new ArrayList<>();
    }

    public ProjectSnapshot(final Long id,
                           final Date projectSnapshotDate,
                           final List<TaskSnapshot> taskSnapshots,
                           final Project project,
                           final double originalEstimate,
                           final double effortLeft,
                           final double effortSpent,
                           final double realEffort,
                           final double quotation) {
        this.id = id;
        this.projectSnapshotDate = projectSnapshotDate;
        this.taskSnapshots = taskSnapshots;
        this.project = project;
        this.originalEstimate = originalEstimate;
        this.effortLeft = effortLeft;
        this.effortSpent = effortSpent;
        this.quotation = quotation;
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public Date getProjectSnapshotDate() {
        return projectSnapshotDate;
    }

    public void setProjectSnapshotDate(final Date projectSnapshotDate) {
        this.projectSnapshotDate = projectSnapshotDate;
    }

    public List<TaskSnapshot> getTaskSnapshots() {
        return taskSnapshots;
    }

    public void setTaskSnapshots(final List<TaskSnapshot> taskSnapshots) {
        this.taskSnapshots = taskSnapshots;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(final Project project) {
        this.project = project;
    }

    public double getOriginalEstimate() {
        return originalEstimate;
    }

    public void setOriginalEstimate(final double originalEstimate) {
        this.originalEstimate = originalEstimate;
    }

    public double getEffortLeft() {
        return effortLeft;
    }

    public void setEffortLeft(final double effortLeft) {
        this.effortLeft = effortLeft;
    }

    public double getEffortSpent() {
        return effortSpent;
    }

    public void setEffortSpent(final double effortSpent) {
        this.effortSpent = effortSpent;
    }

    public double getRealEffort() {
        return effortSpent + effortLeft;
    }

    public double getQuotation() {
        return quotation;
    }

    public void setQuotation(final double quotation) {
        this.quotation = quotation;
    }
}
