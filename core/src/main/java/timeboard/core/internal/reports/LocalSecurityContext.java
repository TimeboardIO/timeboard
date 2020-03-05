package timeboard.core.internal.reports;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;

public class LocalSecurityContext implements SecurityContext {


    private Authentication auth;

    public LocalSecurityContext(Authentication authentication){
        this.auth = authentication;
    }

    @Override
    public Authentication getAuthentication() {
        return auth;
    }

    @Override
    public void setAuthentication(Authentication authentication) {
        this.auth = authentication;
    }
}
