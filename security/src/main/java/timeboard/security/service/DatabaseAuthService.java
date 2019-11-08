package timeboard.security.service;

/*-
 * #%L
 * security
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

import org.osgi.service.component.annotations.*;
import timeboard.core.api.ProjectService;
import timeboard.core.api.UserService;
import timeboard.core.model.User;
import timeboard.security.api.Credential;
import timeboard.security.api.LoginService;
import timeboard.security.api.UsernamePasswordCredential;

@Component(
        service = LoginService.class,
        immediate = true
)
public class DatabaseAuthService implements LoginService {


    @Reference(
            policyOption = ReferencePolicyOption.GREEDY,
            policy = ReferencePolicy.STATIC,
            cardinality = ReferenceCardinality.OPTIONAL
    )
    private UserService userService;

    @Override
    public boolean isServiceValidFor(Credential credential) {
        return credential instanceof UsernamePasswordCredential;
    }

    @Override
    public boolean validateCredential(Credential credential) {
        UsernamePasswordCredential upc = (UsernamePasswordCredential) credential;
        User user = null;
        try {

            user = this.userService.autenticateUser(upc.getUsername(), upc.getPassword());

        } catch (Exception e) {
                e.printStackTrace();
        }

        return user != null;
    }
}
