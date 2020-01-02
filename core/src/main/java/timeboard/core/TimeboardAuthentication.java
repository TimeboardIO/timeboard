package timeboard.core;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import timeboard.core.model.Account;

import java.security.Principal;
import java.util.Collection;

public class TimeboardAuthentication implements Authentication {

    private Principal principal;
    private Account account;

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

    @Override
    public String getName() {
        return null;
    }
}
