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
import java.util.*;


@Entity
public class Task implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @OneToOne(targetEntity = TaskRevision.class, fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private TaskRevision latestRevision;

    @OneToMany(targetEntity = TaskRevision.class, mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("revisionDate desc")
    private List<TaskRevision> revisions;

    /**
     * Task creation origin
     */
    @Column
    private String origin;

    @OneToOne(targetEntity = TaskType.class)
    private TaskType taskType;

    @ManyToOne(targetEntity = Project.class, fetch = FetchType.EAGER)
    private Project project;

    @OneToMany(targetEntity = Imputation.class, mappedBy = "task")
    private Set<Imputation> imputations;

    public Task() {
        this.revisions = new ArrayList<>();
        this.imputations = new HashSet<>();
    }


    public TaskRevision getLatestRevision() {
        return latestRevision;
    }

    public void setLatestRevision(TaskRevision latestRevision) {
        this.latestRevision = latestRevision;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public Set<Imputation> getImputations() {
        return imputations;
    }

    public void setImputations(Set<Imputation> imputations) {
        this.imputations = imputations;
    }

    public double getEstimateWork(){
        if(this.getLatestRevision() != null) {
            return this.getLatestRevision().getEstimateWork();
        }else{
            return 0;
        }
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
                .filter(imputation -> imputation.getDay().equals(date))
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
}
