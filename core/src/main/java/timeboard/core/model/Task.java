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
import java.text.SimpleDateFormat;
import java.util.*;


@Entity
@PrimaryKeyJoinColumn(name = "id")
public class Task extends AbstractTask implements Serializable {

    @OneToOne(targetEntity = TaskRevision.class, fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private TaskRevision latestRevision;

    @OneToMany(targetEntity = TaskRevision.class, mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("revisionDate desc")
    private List<TaskRevision> revisions;

    @Column(nullable = false)
    private double estimateWork;

    /**
     * Task creation origin
     */
    @Column
    private String origin;

    @Column
    private String remotePath;

    @Column
    private Long remoteId;

    @OneToOne(targetEntity = TaskType.class)
    private TaskType taskType;

    @ManyToOne(targetEntity = Project.class, fetch = FetchType.EAGER)
    private Project project;


    public Task() {
        super();
        this.revisions = new ArrayList<>();
    }


    public TaskRevision getLatestRevision() {
        return latestRevision;
    }

    public void setLatestRevision(TaskRevision latestRevision) {
        this.latestRevision = latestRevision;
    }

    public double getEstimateWork() {
        return estimateWork;
    }

    public void setEstimateWork(double estimateWork) {
        this.estimateWork = estimateWork;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getRemotePath() { return remotePath; }

    public void setRemotePath(String remotePath) { this.remotePath = remotePath; }

    public Long getRemoteId() { return remoteId; }

    public void setRemoteId(Long remoteId) { this.remoteId = remoteId; }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }



    /**
     * EL.
     *
     * @return EL
     */
    @Transient
    public double getRemainsToBeDone() {
        if(this.getLatestRevision() != null) {
            return this.getLatestRevision().getRemainsToBeDone();
        }else{
            return 0;
        }
    }

    @Transient
    public void setRemainsToBeDone(final User actor, double rtbd) {
        TaskRevision taskRevision = this.getLatestRevision().clone();
        taskRevision.setRemainsToBeDone(rtbd);
        taskRevision.setRevisionDate(new Date());
        taskRevision.setRevisionActor(actor);
        this.setLatestRevision(taskRevision);
        this.revisions.add(taskRevision);
    }

    @Transient
    public void updateCurrentRemainsToBeDone(final User actor, double rtbd) {
        this.getLatestRevision().setRemainsToBeDone(rtbd);
    }

    public TaskType getTaskType() {
        return taskType;
    }

    public void setTaskType(TaskType taskType) {
        this.taskType = taskType;
    }


    /**
     * RE.
     *
     * @return RE
     */
    @Transient
    public double getReEstimateWork() {
        return this.getEffortSpent() + this.getRemainsToBeDone();
    }

    /**
     * ES.
     *
     * @return ES
     */
    @Transient
    public double getEffortSpent() {
        return this.getImputations().stream().map(imputation -> imputation.getValue()).mapToDouble(Double::doubleValue).sum();
    }

    @Transient
    public double findTaskImputationValueByDate(Date date) {
        Optional<Imputation> iOpt = this.getImputations().stream()
                .filter(imputation -> this.areDateSameDay(date, imputation.getDay()))
                .findFirst();
        if (iOpt.isPresent()) {
            return iOpt.get().getValue();
        } else {
            return 0;
        }
    }

    public List<TaskRevision> getRevisions() {
        return revisions;
    }

    public void setRevisions(List<TaskRevision> revisions) {
        this.revisions = revisions;
    }

    private boolean areDateSameDay(Date date1, Date date2){
        return new SimpleDateFormat("yyyy-MM-dd").format(date1).equals(new SimpleDateFormat("yyyy-MM-dd").format(date2));
    }
}
