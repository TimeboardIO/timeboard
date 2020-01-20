package timeboard.webapp;

import org.springframework.security.core.context.SecurityContextHolder;
import timeboard.core.api.ThreadLocalStorage;
import timeboard.core.model.Account;
import timeboard.core.model.Organization;
import timeboard.core.security.TimeboardAuthentication;

public class SecurityUtils {

    private SecurityUtils(){

    }

    public static TimeboardAuthentication signIn(Organization o, Account a){
        TimeboardAuthentication auth = new TimeboardAuthentication(a);
        SecurityContextHolder.getContext().setAuthentication(auth);
        ThreadLocalStorage.setCurrentOrgId(o.getId());
        return auth;
    }

}
