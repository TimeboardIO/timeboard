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
@NamedQueries(
        {
                @NamedQuery(name = "ListUserProjects", query = "select p from Project p join fetch p.members m where (p.enable = true or p.enable is null) and m.member = :user")
        }
)
public class Project extends OrganizationEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(length = 50, unique = false)
    private String name;

    @Column
    private Double quotation;

    @Temporal(TemporalType.DATE)
    private Date startDate;

    @Column(length = 500)
    private String comments;

    @Column(columnDefinition = "TEXT")
    @Convert(converter = JSONToProjectAttributsConverter.class)
    @Lob
    private Map<String, ProjectAttributValue> attributes;

    @OneToMany(targetEntity = ProjectMembership.class,
            mappedBy = "project",
            orphanRemoval = true,
            cascade = {CascadeType.ALL},
            fetch = FetchType.EAGER
    )
    private Set<ProjectMembership> members;

    @Column(nullable = true)
    private Boolean enable;

    @OneToMany(targetEntity = Task.class, mappedBy = "project", cascade = CascadeType.ALL)
    private Set<Task> tasks;

    @OneToMany(targetEntity = Milestone.class, mappedBy = "project", cascade = CascadeType.ALL)
    private Set<Milestone> milestones;

    @OneToMany(targetEntity = ProjectTag.class, mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProjectTag> tags;


    public Project() {
        this.enable = true;
        this.members = new HashSet<>();
        this.tasks = new HashSet<>();
        this.attributes = new HashMap<>();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Double getQuotation() {
        if (this.quotation == null) {
            return 0.0;
        }
        return quotation;
    }

    public void setQuotation(Double quotation) {
        this.quotation = quotation;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public Set<ProjectMembership> getMembers() {
        return members;
    }

    public void setMembers(Set<ProjectMembership> members) {
        this.members = members;
    }

    public Set<Task> getTasks() {
        return tasks;
    }

    public void setTasks(Set<Task> tasks) {
        this.tasks = tasks;
    }

    public Map<String, ProjectAttributValue> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, ProjectAttributValue> attributes) {
        this.attributes = attributes;
    }

    public Set<Milestone> getMilestones() {
        return milestones;
    }

    public void setMilestones(Set<Milestone> milestones) {
        this.milestones = milestones;
    }

    public List<ProjectTag> getTags() {
        return tags;
    }

    public void setTags(List<ProjectTag> tags) {
        this.tags = tags;
    }

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public boolean isMember(Account actor) {
        //TODO check if actor is member of current project
        return true;
    }

    @Transient
    public String getColor() {
        if (this.getAttributes().get("project.color") != null) {
            return this.getAttributes().get("project.color").getValue();
        } else {
            return "#957DAD";
        }
    }
}
