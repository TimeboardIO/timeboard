package timeboard.core.ui;

/*-
 * #%L
 * core-ui
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;
import timeboard.core.api.ThreadLocalStorage;
import timeboard.core.api.UserService;
import timeboard.core.model.Account;

@Component("")
@SessionScope
public class UserInfo {

    @Autowired
    private UserService userService;

    private Account account;

    public Account getCurrentAccount() {
        if (account == null) {

            Authentication token = SecurityContextHolder.getContext().getAuthentication();

            if (token instanceof UsernamePasswordAuthenticationToken) {
                account = userService.findUserBySubject(((UsernamePasswordAuthenticationToken) token).getName());
            }

            if (token instanceof OAuth2AuthenticationToken) {
                account = userService.findUserBySubject(
                        (String) ((OAuth2AuthenticationToken) token).getPrincipal().getAttributes().get("sub"));
            }


        }
        return account;
    }

    public Long getCurrentOrganizationID() {
        return ThreadLocalStorage.getCurrentOrganizationID() != null
                ? ThreadLocalStorage.getCurrentOrganizationID()
                : getCurrentAccount().getId();
    }
}
