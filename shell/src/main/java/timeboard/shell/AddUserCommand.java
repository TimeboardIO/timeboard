package timeboard.shell;

/*-
 * #%L
 * shell
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

import timeboard.core.api.UserService;
import timeboard.core.model.User;
import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

import java.util.Date;

@Service
@Command(scope = "timeboard", name = "add-user", description = "Create new user account")
public class AddUserCommand implements Action {

    @Option(name = "-u", aliases = {"--username"}, description = "New username (also used as login)", required = true, multiValued = false)
    String username;

    @Option(name = "-p", aliases = {"--password"}, description = "Account password", required = true, multiValued = false)
    String password;

    @Option(name = "-e", aliases = {"--email"}, description = "Account email", required = true, multiValued = false)
    String email;


    @Override
    public Object execute() throws Exception {

        BundleContext bnd = FrameworkUtil.getBundle(AddUserCommand.class).getBundleContext();
        ServiceReference<UserService> userServiceRef = bnd.getServiceReference(UserService.class);
        UserService userService =  bnd.getService(userServiceRef);

        User user = new User();
        user.setName(username);
        user.setPassword(password);
        user.setFirstName(username);
        user.setEmail(email);
        user.setBeginWorkDate(new Date());
        user.setImputationFutur(true);
        user.setLogin(username);
        user.setAccountCreationTime(new Date());
        userService.createUser(user);
        System.out.println("User "+username+" is created !");
        return null;
    }
}
