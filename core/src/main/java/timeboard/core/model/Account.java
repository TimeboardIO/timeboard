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


import timeboard.core.model.converters.JSONToStringMapConverter;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Calendar;
import java.util.*;

@Entity
@Table
public class Account implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column(nullable = false)
    @Temporal(TemporalType.DATE)
    private Calendar accountCreationTime;

    @Column(nullable = true)
    private String name;

    @Column(nullable = true)
    private String firstName;

    @Column(nullable = false, unique = false)
    private String email;

    @Column(nullable = true, unique = true)
    private String remoteSubject;


    @Column(columnDefinition = "TEXT")
    @Convert(converter = JSONToStringMapConverter.class)
    @Lob
    private Map<String, String> externalIDs;


    @OneToMany(targetEntity = OrganizationMembership.class,
            mappedBy = "member",
            orphanRemoval = true,
            fetch = FetchType.EAGER
    )
    private Set<OrganizationMembership> organizations;

    public Account() {
        this.externalIDs = new HashMap<>();
        this.organizations = new HashSet<>();
    }


    public Account(final String name, final String firstName,
                   final String email, final Calendar accountCreationTime) {
        super();
        this.name = name;
        this.firstName = firstName;
        this.email = email;
        this.accountCreationTime = accountCreationTime;
        this.externalIDs = new HashMap<>();
    }


    public Set<OrganizationMembership> getOrganizationMemberships() {
        if (this.organizations == null) {
            this.organizations = new HashSet<>();
        }
        return this.organizations;
    }

    public void setOrganizations(final Set<OrganizationMembership> organizations) {
        this.organizations = organizations;
    }

    public long getId() {
        return id;
    }

    public void setId(final long id) {
        this.id = id;
    }

    public String getRemoteSubject() {
        return remoteSubject;
    }

    public void setRemoteSubject(final String remoteSubject) {
        this.remoteSubject = remoteSubject;
    }

    public Calendar getAccountCreationTime() {
        return accountCreationTime;
    }

    public void setAccountCreationTime(final Calendar accountCreationTime) {
        this.accountCreationTime = accountCreationTime;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(final String firstName) {
        this.firstName = firstName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(final String email) {
        this.email = email;
    }


    @Transient
    public String getScreenName() {
        if (this.getFirstName() == null && this.getName() == null) {
            return this.getEmail();
        } else if (this.getFirstName() == null) {
            return this.getName();
        } else if (this.getName() == null) {
            return this.getFirstName();
        } else {
            return this.getFirstName() + " " + this.getName();
        }
    }

    @Transient
    public String getScreenOrgName() {
        return this.getName();
    }

    @Transient
    public boolean isMemberOf(final Optional<Organization> orgToTest) {
        if (orgToTest.isPresent()) {
            if (orgToTest.get().isPublicOrganisation()) {
                return true;
            }

            return orgToTest.get().getMembers()
                    .stream().filter(om -> om.getMember().getId() == this.getId())
                    .count() >= 1;
        } else {
            return false;
        }
    }

    public Map<String, String> getExternalIDs() {
        return externalIDs;
    }

    public void setExternalIDs(final Map<String, String> externalIDs) {
        this.externalIDs = externalIDs;
    }


}
