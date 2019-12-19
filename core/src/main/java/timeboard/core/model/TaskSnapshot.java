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
import java.util.Date;


@Entity
public class TaskSnapshot extends OrganizationEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column
    @Temporal(TemporalType.TIMESTAMP)
    private Date snapshotDate;

    @OneToOne(targetEntity = Task.class, cascade = {CascadeType.PERSIST})
    private Task task;

    @ManyToOne(targetEntity = ProjectSnapshot.class, fetch = FetchType.EAGER)
    private ProjectSnapshot projectSnapshot;


    /**
     * Current task assigned at revision date.
     */
    @OneToOne
    private Account assigned;

    @Column(nullable = false)
    private double originalEstimate;

    @Column(nullable = false)
    private double effortLeft;

    @Column(nullable = false)
    private double effortSpent;

    @Column(nullable = false)
    private double realEffort;

    public TaskSnapshot() {
    }


    public TaskSnapshot(Date snapshotDate, Task task, Account assigned) {
        this.snapshotDate = snapshotDate;
        this.task = task;
        this.assigned = assigned;
        this.originalEstimate = task.getOriginalEstimate();
        this.effortLeft = task.getEffortLeft();
        this.effortSpent = task.getEffortSpent();
        this.realEffort = task.getRealEffort();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getSnapshotDate() {
        return snapshotDate;
    }

    public void setSnapshotDate(Date snapshotDate) {
        this.snapshotDate = snapshotDate;
    }

    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
    }

    public Account getAssigned() {
        return assigned;
    }

    public void setAssigned(Account assigned) {
        this.assigned = assigned;
    }

    public double getOriginalEstimate() {
        return originalEstimate;
    }

    public void setOriginalEstimate(double originalEstimate) {
        this.originalEstimate = originalEstimate;
    }

    public double getEffortLeft() {
        return effortLeft;
    }

    public void setEffortLeft(double effortLeft) {
        this.effortLeft = effortLeft;
    }

    public double getEffortSpent() {
        return effortSpent;
    }

    public void setEffortSpent(double effortSpent) {
        this.effortSpent = effortSpent;
    }

    public double getRealEffort() {
        return realEffort;
    }

    public void setRealEffort(double realEffort) {
        this.realEffort = realEffort;
    }

}
