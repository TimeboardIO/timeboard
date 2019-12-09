package timeboard.webapp;

/*-
 * #%L
 * webapp
 * %%
 * Copyright (C) 2019 Timeboard
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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.InteractiveAuthenticationSuccessEvent;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Component;
import timeboard.core.api.UserService;
import timeboard.core.api.exceptions.BusinessException;
import timeboard.core.model.Account;

import java.util.Map;

@Component
public class SuccessfulLoginListener {

    @Autowired
    private UserService userService;

    @EventListener
    public void doSomething(InteractiveAuthenticationSuccessEvent event) throws BusinessException {

        Map<String, Object> userAttributes = ((OAuth2AuthenticationToken) event.getSource()).getPrincipal().getAttributes();
        Account account = this.userService.findUserBySubject((String) userAttributes.get("sub"));

        if(account == null){
            this.userService.userProvisionning((String)userAttributes.get("sub"), (String)userAttributes.get("email"));
        }
    }
}
