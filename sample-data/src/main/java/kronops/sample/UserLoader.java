package kronops.sample;

/*-
 * #%L
 * sample-data
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

import kronops.core.api.UserService;
import kronops.core.api.exceptions.BusinessException;
import kronops.core.model.User;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Component(
        service = UserLoader.class,
        immediate = true
)
public class UserLoader {

    @Reference
    UserService userService;


    @Activate
    public void load() throws BusinessException {
        List<User> users = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            User u = new User();
            u.setName("kronops" + i);
            u.setPassword("kronops" + i);
            u.setEmail("user" + i + "@kronops.com");
            u.setImputationFutur(true);
            u.setBeginWorkDate(new Date());
            u.setFirstName("User" + i);
            u.setLogin("kronops" + i);
            u.setAccountCreationTime(new Date());
            users.add(u);
            System.out.println("Stage user : "+u.getName());
        }

        try {
            this.userService.createUsers(users);
            System.out.println("Save "+users.size()+" users");
        } catch (BusinessException e) {
            e.printStackTrace();
        }

    }


}
