package timeboard.webapp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.InteractiveAuthenticationSuccessEvent;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Component;
import timeboard.core.api.UserService;
import timeboard.core.api.exceptions.BusinessException;
import timeboard.core.model.User;

import java.util.Map;

@Component
public class SuccessfulLoginListener {

    @Autowired
    private UserService userService;

    @EventListener
    public void doSomething(InteractiveAuthenticationSuccessEvent event) throws BusinessException {

        Map<String, Object> userAttributes = ((OAuth2AuthenticationToken) event.getSource()).getPrincipal().getAttributes();
        User user = this.userService.findUserBySubject((String) userAttributes.get("sub"));

        if(user == null){
            this.userService.userProvisionning((String)userAttributes.get("sub"), (String)userAttributes.get("email"));
        }
    }
}
