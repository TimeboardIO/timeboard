package timeboard.core;

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

import java.security.Principal;
import java.util.Collection;

public class TimeboardAuthentication implements Authentication {

    private Principal principal;
    private Account account;
    private Long currentOrganization;

    public  TimeboardAuthentication(Account a){
        this.account = a;
        this.principal = new Principal() {
            @Override
            public String getName() {
                return account.getEmail();
            }
        };
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
        return this.principal;
    }

    @Override
    public boolean isAuthenticated() {
        return this.principal != null;
    }

    @Override
    public void setAuthenticated(boolean b) throws IllegalArgumentException {

    }

    public Long getCurrentOrganization() {
        return currentOrganization;
    }

    public void setCurrentOrganization(Long currentOrganization) {
        this.currentOrganization = currentOrganization;
    }

    @Override
    public String getName() {
        return null;
    }
}
