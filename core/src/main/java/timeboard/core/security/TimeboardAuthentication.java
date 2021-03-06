package timeboard.core.security;

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

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import timeboard.core.model.Account;
import timeboard.core.model.MembershipRole;
import timeboard.core.model.Organization;
import timeboard.core.model.Project;

import javax.persistence.Transient;
import java.security.Principal;
import java.util.Arrays;
import java.util.Collection;

public class TimeboardAuthentication implements Authentication {

    private Principal p;
    private Account account;

    private Account overriddenAccount;
    private Organization currentOrganization;

    public TimeboardAuthentication(final Account a) {
        this.account = a;
        this.p = () -> account.getEmail();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return null;
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public Account getDetails() {
        if (overriddenAccount != null) {
            return overriddenAccount;
        }
        return account;
    }

    @Override
    public Principal getPrincipal() {
        return this.p;
    }

    public boolean isImpersonalised() {
        return this.overriddenAccount != null;
    }

    @Override
    public boolean isAuthenticated() {
        return this.p != null;
    }

    @Override
    public void setAuthenticated(final boolean b) throws IllegalArgumentException {

    }

    public void setOverriddenAccount(Account overriddenAccount) {
        this.overriddenAccount = null;
        if (overriddenAccount != null && this.account.getId() != overriddenAccount.getId()) {
            this.overriddenAccount = overriddenAccount;
        }
    }


    public Organization getCurrentOrganization() {
        return currentOrganization;
    }

    public void setCurrentOrganization(final Organization currentOrganization) {
        this.currentOrganization = currentOrganization;
    }

    @Override
    public String getName() {
        return null;
    }

    @Transient
    public boolean currentOrganizationRole(final MembershipRole... roles) {
        if (this.getDetails() == null || this.getDetails().getOrganizationMemberships() == null || currentOrganization == null) {
            return false;
        }
        return this.getDetails().getOrganizationMemberships()
                .stream()
                .filter(o -> o.getOrganization() != null)
                .filter(o -> o.getOrganization().getId() == currentOrganization.getId())
                .filter(o -> Arrays.asList(roles).contains(o.getRole()))
                .count() > 0;
    }

    @Transient
    public boolean currentProjectRole(final Project project, final MembershipRole... roles) {
        if (project == null) {
            return false;
        }
        return project.getMembers()
                .stream()
                .filter(projectMembership -> projectMembership.getMember().getId() == this.getDetails().getId())
                .filter(projectMembership -> Arrays.asList(roles).contains(projectMembership.getRole()))
                .count() > 0;
    }

    @Transient
    public boolean isPublicCurrentOrganization() {
        return this.getDetails().getOrganizationMemberships().stream()
                .map(o -> o.getOrganization())
                .filter(o -> o.getId() == this.currentOrganization.getId())
                .allMatch(o -> o.isPublicOrganisation());
    }

    @Transient
    public boolean is(Account account) {
        return this.getDetails().equals(account);
    }


    @Override
    public String toString() {
        return getDetails().getScreenName();
    }
}
