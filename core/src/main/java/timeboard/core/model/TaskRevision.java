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
public class TaskRevision implements Serializable, Cloneable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(length = 50, nullable = false)
    private String name;

    @Column(length = 500)
    private String comments;

    @Column(nullable = false)
    private double estimateWork;

    @OneToOne
    private User assigned;

    @Temporal(TemporalType.DATE)
    private Date startDate;

    @Temporal(TemporalType.DATE)
    private Date endDate;

    @Column(nullable = false)
    private Double remainsToBeDone;

    @OneToOne(targetEntity = Task.class, cascade = {CascadeType.PERSIST})
    private Task task;

    @Column
    @Temporal(TemporalType.TIMESTAMP)
    private Date revisionDate;

    @OneToOne
    private User revisionActor;

    private TaskStatus taskStatus;

    public TaskRevision(User actor, Task t, String taskName, String taskComment, Date startDate, Date endDate, double oe, double rtbd, User assignedUser, TaskStatus taskStatus) {
        this.setRevisionDate(new Date());
        this.setName(taskName);
        this.setComments(taskComment);
        this.setStartDate(startDate);
        this.setEndDate(endDate);
        this.setEstimateWork(oe);
        this.setRemainsToBeDone(rtbd);
        this.setRevisionActor(actor);
        this.setAssigned(assignedUser);
        this.setTask(t);
        this.setTaskStatus(taskStatus);
    }

    public TaskRevision(){
    }

    public double getEstimateWork() {
        return estimateWork;
    }

    public void setEstimateWork(double estimateWork) {
        this.estimateWork = estimateWork;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public Date getRevisionDate() {
        return revisionDate;
    }

    public void setRevisionDate(Date revisionDate) {
        this.revisionDate = revisionDate;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Double getRemainsToBeDone() {
        return remainsToBeDone;
    }

    public void setRemainsToBeDone(Double remainsToBeDone) {
        this.remainsToBeDone = remainsToBeDone;
    }

    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
    }

    public User getRevisionActor() {
        return revisionActor;
    }

    public void setRevisionActor(User revisionActor) {
        this.revisionActor = revisionActor;
    }

    public User getAssigned() {
        return assigned;
    }

    public void setAssigned(User assigned) {
        this.assigned = assigned;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public TaskStatus getTaskStatus() {
        return taskStatus;
    }

    public void setTaskStatus(TaskStatus taskStatus) {
        this.taskStatus = taskStatus;
    }

    @Override
    protected TaskRevision clone()  {

        final TaskRevision taskRevision = new TaskRevision();
        taskRevision.setName(this.getName());
        taskRevision.setComments(this.getComments());
        taskRevision.setTask(this.getTask());
        taskRevision.setAssigned(this.getAssigned());
        taskRevision.setRevisionActor(this.getRevisionActor());
        taskRevision.setRemainsToBeDone(this.getRemainsToBeDone());
        taskRevision.setStartDate(this.getStartDate());
        taskRevision.setEndDate(this.getEndDate());
        taskRevision.setEstimateWork(this.getEstimateWork());
        taskRevision.setRevisionDate(this.getRevisionDate());
        taskRevision.setTaskStatus(this.getTaskStatus());

        return taskRevision;
    }
}
