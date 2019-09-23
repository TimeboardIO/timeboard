package kronops.security.service;

/*-
 * #%L
 * security
 * %%
 * Copyright (C) 2019 Kronops
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

import kronops.core.api.UserServiceBP;
import kronops.core.model.User;
import kronops.security.api.Credential;
import kronops.security.api.LoginService;
import kronops.security.api.UsernamePasswordCredential;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(
        service = LoginService.class,
        immediate = true
)
public class DatabaseAuthService implements LoginService {


    @Reference
    private UserServiceBP userServiceBP;


    @Override
    public boolean isServiceValidFor(Credential credential) {
        return credential instanceof UsernamePasswordCredential;
    }

    @Override
    public boolean validateCredential(Credential credential) {
        UsernamePasswordCredential upc = (UsernamePasswordCredential) credential;
        User user = null;
        try {
            user = this.userServiceBP.autenticateUser(upc.getUsername(), upc.getPassword());

        } catch (Exception e) {

        }

        return user != null;
    }
}
