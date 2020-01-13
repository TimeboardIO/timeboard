package timeboard.core.model;

/*-
 * #%L
 * core
 * %%
 * Copyright (C) 2019 - 2020 Timeboard
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

import org.springframework.beans.factory.annotation.Value;
import timeboard.core.model.converters.JSONToStringMapConverter;

import javax.persistence.*;
import java.util.*;
import java.util.stream.Collectors;

@Entity
@Table
@NamedQueries(
        {
                @NamedQuery(
                        name = Organization.FIND_BY_NAME,
                        query = "select o from Organization o where o.name = :name and o.enabled = true")
        }
)
public class Organization {

    public static final String FIND_BY_NAME = "org_find_by_name";
    public static final String SETUP_PUBLIC = "org_public";

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column
    private Boolean enabled=true;

    @Column(columnDefinition = "TEXT")
    @Convert(converter = JSONToStringMapConverter.class)
    @Lob
    private Map<String, String> setup;

    @Column
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdDate;

    @OneToMany(targetEntity = OrganizationMembership.class,
            mappedBy = "organization",
            orphanRemoval = true,
            cascade = {CascadeType.ALL},
            fetch = FetchType.EAGER
    )
    private Set<OrganizationMembership> members = new HashSet<>();

    @OneToMany(targetEntity = DefaultTask.class,
            mappedBy = "organization",
            orphanRemoval = true,
            cascade = {CascadeType.ALL},
            fetch = FetchType.EAGER
    )
    private Set<DefaultTask> defaultTasks = new HashSet<>();

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public Set<OrganizationMembership> getMembers() {
        return members;
    }

    public void setMembers(Set<OrganizationMembership> members) {
        this.members = members;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public Map<String, String> getSetup() {
        return setup;
    }

    public void setSetup(Map<String, String> setup) {
        this.setup = setup;
    }

    public Set<DefaultTask> getDefaultTasks() {
        return defaultTasks;
    }

    public void setDefaultTasks(Set<DefaultTask> defaultTasks) {
        this.defaultTasks = defaultTasks;
    }


    /**
     * Test if current org is public
     * @return true if setup contain key SETUP_PUBLIC with "true" as value
     */
    @Transient
    public boolean isPublicOrganisation(){
        return this.getSetup().containsKey(SETUP_PUBLIC) && this.getSetup().get(SETUP_PUBLIC).equals("true");
    }

    @Transient
    public List<MembershipRole> getAccountRoles(Account target) {
        return this.getMembers()
                .stream().filter(om -> om.getMember().getId() == target.getId())
                .map(om -> om.getRole())
                .collect(Collectors.toList());
    }

}
