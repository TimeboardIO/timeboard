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


//import timeboard.apigenerator.annotation.RPCEntity;

import timeboard.core.model.converters.JSONToStringMapConverter;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Entity
@Table
public class Account implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column(nullable = false)
    @Temporal(TemporalType.DATE)
    private Date accountCreationTime;

    @Column(nullable = false)
    @Temporal(TemporalType.DATE)
    private Date beginWorkDate;

    @Column(nullable = true)
    private String name;

    @Column(nullable = true)
    private String firstName;

    @Column(nullable = false, unique = false)
    private String email;

    @Column(nullable = true, unique = true)
    private String remoteSubject;

    @Column
    private Boolean isOrganization = false;


    @Column
    private String localLogin;

    @Column
    private String localPassword;


    @Column(columnDefinition = "TEXT")
    @Convert(converter = JSONToStringMapConverter.class)
    @Lob
    private Map<String, String> externalIDs;


    @OneToMany(targetEntity = AccountHierarchy.class,
            mappedBy = "organization",
            orphanRemoval = true,
            cascade = {CascadeType.ALL},
            fetch = FetchType.EAGER
    )
    private Set<AccountHierarchy> members;

    public Account() {
        this.externalIDs = new HashMap<>();
        this.isOrganization = false;
    }


    public Account(final String name, final String firstName,
                   final String email, final Date accountCreationTime, final Date beginWorkDate) {
        super();
        this.name = name;
        this.firstName = firstName;
        this.email = email;
        this.accountCreationTime = accountCreationTime;
        this.beginWorkDate = beginWorkDate;
        this.externalIDs = new HashMap<>();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getRemoteSubject() {
        return remoteSubject;
    }

    public void setRemoteSubject(String remoteSubject) {
        this.remoteSubject = remoteSubject;
    }

    public Date getAccountCreationTime() {
        return accountCreationTime;
    }

    public void setAccountCreationTime(Date accountCreationTime) {
        this.accountCreationTime = accountCreationTime;
    }

    public Date getBeginWorkDate() {
        return beginWorkDate;
    }

    public void setBeginWorkDate(Date beginWorkDate) {
        this.beginWorkDate = beginWorkDate;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean getIsOrganization() {
        if(this.isOrganization == null){
            return false;
        }else {
            return isOrganization;
        }
    }

    public void setIsOrganization(Boolean isOrganization) {
        this.isOrganization = isOrganization;
    }

    public Set<AccountHierarchy> getMembers() {
        return members;
    }

    public String getLocalLogin() {
        return localLogin;
    }

    public void setLocalLogin(String localLogin) {
        this.localLogin = localLogin;
    }

    public String getLocalPassword() {
        return localPassword;
    }

    public void setLocalPassword(String localPassword) {
        this.localPassword = localPassword;
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
        if (this.isOrganization != null && this.isOrganization) {
            return this.getName();
        }
        return "My Organization";
    }


    public Map<String, String> getExternalIDs() {
        return externalIDs;
    }

    public void setExternalIDs(Map<String, String> externalIDs) {
        this.externalIDs = externalIDs;
    }



}
