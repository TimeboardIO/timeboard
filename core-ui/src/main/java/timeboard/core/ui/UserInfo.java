package timeboard.core.ui;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;
import timeboard.core.api.UserService;
import timeboard.core.model.User;

@Component
@SessionScope
public class UserInfo {

    @Autowired
    private UserService userService;

    private User user;

    public User getCurrentUser() {
        if (user == null) {
            OAuth2AuthenticationToken authentication = (OAuth2AuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
            user = userService.findUserBySubject((String) authentication.getPrincipal().getAttributes().get("sub"));
        }
        return user;
    }
}
