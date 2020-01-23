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
import java.util.HashSet;
import java.util.Set;

@Entity
@PrimaryKeyJoinColumn(name = "id")
public class Task extends AbstractTask implements Serializable {

    @Column(nullable = false)
    private double originalEstimate;

    @Column(nullable = false)
    private double effortLeft;

    @Column(nullable = false)
    private TaskStatus taskStatus;

    @OneToOne(targetEntity = TaskType.class)
    private TaskType taskType;

    @ManyToOne(targetEntity = Project.class, fetch = FetchType.EAGER)
    private Project project;

    @ManyToMany(targetEntity = Batch.class, fetch = FetchType.EAGER)
    private Set<Batch> batches;

    @OneToOne
    private Account assigned;


    public Task() {
        super();
    }


    public double getOriginalEstimate() {
        return originalEstimate;
    }

    public void setOriginalEstimate(double originalEstimate) {
        this.originalEstimate = originalEstimate;
    }

    public TaskStatus getTaskStatus() {
        return taskStatus;
    }

    public void setTaskStatus(TaskStatus taskStatus) {
        this.taskStatus = taskStatus;
    }

    public Account getAssigned() {
        return assigned;
    }

    public void setAssigned(Account assigned) {
        this.assigned = assigned;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public Set<Batch> getBatches() {
        return batches;
    }

    public void setBatches(Set<Batch> batches) {
        this.batches = batches;
    }

    public void addBatch(Batch batch) {
        if (batch == null) {
            this.batches = new HashSet<>();
        }
        this.batches.add(batch);
    }

    public double getEffortLeft() {
        return this.effortLeft;
    }

    public void setEffortLeft(double effortLeft) {
        this.effortLeft = effortLeft;
    }

    public TaskType getTaskType() {
        return taskType;
    }

    public void setTaskType(TaskType taskType) {
        this.taskType = taskType;
    }


    /**
     * Real Effort = EffortSpent + EffortLeft.
     *
     * @return Real Effort
     */
    @Transient
    public double getRealEffort() {
        return this.getEffortSpent() + this.getEffortLeft();
    }

    /**
     * Effort Spent.
     *
     * @return Effort Spent
     */
    @Transient
    public double getEffortSpent() {
        return this.getImputations().stream().map(imputation -> imputation.getValue()).mapToDouble(Double::doubleValue).sum();
    }


}
