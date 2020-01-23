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
import timeboard.core.model.Project;

import javax.persistence.Transient;
import java.security.Principal;
import java.util.Collection;

public class TimeboardAuthentication implements Authentication {

    private Principal p;
    private Account account;
    private Long currentOrganization;

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
        return account;
    }

    @Override
    public Principal getPrincipal() {
        return this.p;
    }

    @Override
    public boolean isAuthenticated() {
        return this.p != null;
    }

    @Override
    public void setAuthenticated(final boolean b) throws IllegalArgumentException {

    }

    public Long getCurrentOrganization() {
        return currentOrganization;
    }

    public void setCurrentOrganization(final Long currentOrganization) {
        this.currentOrganization = currentOrganization;
    }

    @Override
    public String getName() {
        return null;
    }

    @Transient
    public boolean currentOrganizationRole(final MembershipRole role) {
        return account.getOrganizations()
                .stream()
                .filter(o -> o.getOrganization().getId() == currentOrganization)
                .filter(o -> o.getRole() == role)
                .count() > 0;
    }

    @Transient
    public boolean currentProjectRole(final Project project, final MembershipRole role) {
        return project.getMembers()
                .stream()
                .filter(projectMembership -> projectMembership.getMember().getId() == account.getId())
                .filter(projectMembership -> projectMembership.getRole() == role)
                .count() > 0;
    }

    @Transient
    public boolean isPublicCurrentOrganization() {
        return account.getOrganizations().stream()
                .map(o -> o.getOrganization())
                .filter(o -> o.getId() == this.currentOrganization)
                .allMatch(o -> o.isPublicOrganisation());
    }


}
