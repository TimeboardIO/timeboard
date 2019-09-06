package kronops.core.model;

/*-
 * #%L
 * core
 * %%
 * Copyright (C) 2019 Kronops
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


//import kronops.apigenerator.annotation.RPCEntity;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "user")

public class User implements Serializable {

    @Id
    @Column(length = 36)
    private long id;

    @Column(nullable = false, unique = true, length = 50)
    private String login;

    @Column(nullable = false)
    @Temporal(TemporalType.DATE)
    private Date accountCreationTime;

    /**
     * Represent the time the user started working as an employee. This date is equal to accountCreationTime, or can be
     * set BEFORE it
     */
    @Column(nullable = false)
    @Temporal(TemporalType.DATE)
    private Date beginWorkDate;


    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private boolean imputationFutur = false;

    @Column(nullable = false)
    private boolean projectManager = false;

    @Column(nullable = false)
    private boolean validateOwnImputation = false;


    @Column(nullable = true)
    private String matriculeID;

    @Column(nullable = true)
    private String mantisUserName;

    @Column(nullable = true)
    private String jiraUserName;

    @Column(nullable = true)
    private String mailToken;

    @Column(nullable = true)
    private Date mailTokenEndDate;

    @Column(nullable = true)
    private String apiToken;


    public User() {
     }


    public User(final String login, final String password, final String name, final String firstName,
                final String email, final Date accountCreationTime, final Date beginWorkDate) {
        this.login = login;
        this.password = password;
        this.name = name;
        this.firstName = firstName;
        this.email = email;
        this.accountCreationTime = accountCreationTime;
        this.beginWorkDate = beginWorkDate;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isImputationFutur() {
        return imputationFutur;
    }

    public void setImputationFutur(boolean imputationFutur) {
        this.imputationFutur = imputationFutur;
    }

    public boolean isProjectManager() {
        return projectManager;
    }

    public void setProjectManager(boolean projectManager) {
        this.projectManager = projectManager;
    }

    public boolean isValidateOwnImputation() {
        return validateOwnImputation;
    }

    public void setValidateOwnImputation(boolean validateOwnImputation) {
        this.validateOwnImputation = validateOwnImputation;
    }

    public String getMatriculeID() {
        return matriculeID;
    }

    public void setMatriculeID(String matriculeID) {
        this.matriculeID = matriculeID;
    }

    public String getMantisUserName() {
        return mantisUserName;
    }

    public void setMantisUserName(String mantisUserName) {
        this.mantisUserName = mantisUserName;
    }

    public String getJiraUserName() {
        return jiraUserName;
    }

    public void setJiraUserName(String jiraUserName) {
        this.jiraUserName = jiraUserName;
    }

    public String getMailToken() {
        return mailToken;
    }

    public void setMailToken(String mailToken) {
        this.mailToken = mailToken;
    }

    public Date getMailTokenEndDate() {
        return mailTokenEndDate;
    }

    public void setMailTokenEndDate(Date mailTokenEndDate) {
        this.mailTokenEndDate = mailTokenEndDate;
    }

    public String getApiToken() {
        return apiToken;
    }

    public void setApiToken(String apiToken) {
        this.apiToken = apiToken;
    }
}
