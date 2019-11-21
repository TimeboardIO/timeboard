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
public class TaskRevision implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column
    @Temporal(TemporalType.TIMESTAMP)
    private Date revisionDate;

    @OneToOne(targetEntity = Task.class, cascade = {CascadeType.PERSIST})
    private Task task;

    /**
     * Current task assigned at revision date
     */
    @OneToOne
    private User assigned;

    @Column(nullable = false)
    private double originalEstimate;

    @Column(nullable = false)
    private double effortLeft;

    @Column(nullable = false)
    private double effortSpent;

    @Column(nullable = false)
    private double realEffort;

    public TaskRevision(){
    }


    public TaskRevision(Date revisionDate, Task task, User assigned, double originalEstimate, double effortLeft, double effortSpent, double realEffort) {
        this.revisionDate = revisionDate;
        this.task = task;
        this.assigned = assigned;
        this.originalEstimate = originalEstimate;
        this.effortLeft = effortLeft;
        this.effortSpent = effortSpent;
        this.realEffort = realEffort;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getRevisionDate() {
        return revisionDate;
    }

    public void setRevisionDate(Date revisionDate) {
        this.revisionDate = revisionDate;
    }

    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
    }

    public User getAssigned() {
        return assigned;
    }

    public void setAssigned(User assigned) {
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
