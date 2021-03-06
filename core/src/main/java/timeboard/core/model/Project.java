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

import timeboard.core.model.converters.JSONToProjectAttributsConverter;

import javax.persistence.*;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;


@Entity
@NamedQueries(
        {
                @NamedQuery(name = Project.PROJECT_LIST, query =
                        "select p from Project p join p.members m " +
                                "where (p.enable = true or p.enable is null) and :user in m.member and p.organizationID = :orgID"),

                @NamedQuery(name = Project.PROJECT_GET_BY_ID, query =
                        "select p from Project p join p.members m " +
                                "where p.id = :projectID and m.member = :user and p.organizationID = :orgID"),

        }
)
@Table(name = "Project", uniqueConstraints = {
        @UniqueConstraint(columnNames = {
                "name", "organizationID"
        })
})
public class Project extends OrganizationEntity implements Serializable {

    public static final String PROJECT_COLOR_ATTR = "project.color";
    public static final String PROJECT_LIST = "pjt_list";
    public static final String PROJECT_GET_BY_ID = "pjt_get_by_id";

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

    @OneToMany(targetEntity = ProjectTag.class, mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProjectTag> tags;

    @OneToMany(targetEntity = ProjectSnapshot.class, mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProjectSnapshot> snapshots;


    public Project() {
        this.enable = true;
        this.members = new HashSet<>();
        this.tasks = new HashSet<>();
        this.attributes = new HashMap<>();
        this.snapshots = new ArrayList<>();
        this.tags = new ArrayList<>();
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public Double getQuotation() {
        if (this.quotation == null) {
            return 0.0;
        }
        return quotation;
    }

    public void setQuotation(final Double quotation) {
        this.quotation = quotation;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(final Date startDate) {
        this.startDate = startDate;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(final String comments) {
        this.comments = comments;
    }

    public Set<ProjectMembership> getMembers() {
        return members;
    }

    public void setMembers(final Set<ProjectMembership> members) {
        this.members = members;
    }

    public Set<Task> getTasks() {
        return tasks;
    }

    public void setTasks(final Set<Task> tasks) {
        this.tasks = tasks;
    }

    public Map<String, ProjectAttributValue> getAttributes() {
        return attributes;
    }

    public void setAttributes(final Map<String, ProjectAttributValue> attributes) {
        this.attributes = attributes;
    }


    public List<ProjectTag> getTags() {
        return tags;
    }

    public void setTags(final List<ProjectTag> tags) {
        this.tags = tags;
    }

    public List<ProjectSnapshot> getSnapshots() {
        return snapshots;
    }

    public void setSnapshots(final List<ProjectSnapshot> snapshots) {
        this.snapshots = snapshots;
    }

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(final boolean enable) {
        this.enable = enable;
    }


    @Transient
    public Set<ProjectMembership> getMemberShipsByRole(final MembershipRole role) {
        if (role != null) {
            return this.getMembers()
                    .stream()
                    .filter(projectMembership -> projectMembership.getRole() == role)
                    .collect(Collectors.toSet());
        }
        return this.getMembers();
    }

    @Transient
    public boolean isMember(final Account actor) {
        return this.getMembers()
                .stream()
                .filter(projectMembership -> projectMembership.getMember().getId() == actor.getId())
                .count() == 1;
    }

    @Transient
    public boolean isMember(final Account actor, final MembershipRole role) {
        return this.getMembers()
                .stream()
                .filter(projectMembership -> projectMembership.getMember().getId() == actor.getId())
                .filter(pm -> pm.getRole() == role)
                .count() == 1;
    }


    @Transient
    public String getColor() {
        if (this.getAttributes().get("project.color") != null) {
            return this.getAttributes().get("project.color").getValue();
        } else {
            return "#957DAD";
        }
    }

    @Override
    public String toString() {
        return "project(" + this.getId() + ") - " + this.getName();
    }
}
