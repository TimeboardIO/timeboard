package timeboard.webapp.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;
import timeboard.core.TimeboardAuthentication;
import timeboard.core.api.UserService;
import timeboard.core.model.Account;

@Component
public class DatabaseAuthenticationProvider  implements AuthenticationProvider {

    @Autowired
    private UserService userService;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        Account account = this.userService.findUserByLogin(((UsernamePasswordAuthenticationToken) authentication).getName());
        return new TimeboardAuthentication(account);
    }

    @Override
    public boolean supports(Class<?> aClass) {
        return aClass == UsernamePasswordAuthenticationToken.class;
    }
}
