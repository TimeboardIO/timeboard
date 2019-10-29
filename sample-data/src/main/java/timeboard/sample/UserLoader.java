package timeboard.sample;

/*-
 * #%L
 * sample-data
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
import timeboard.core.api.exceptions.BusinessException;
import timeboard.core.model.User;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class UserLoader {

    UserService userService;

    UserLoader(UserService userService){
        this.userService = userService;
    }

    public List<User> load() throws BusinessException {
        List<User> userSaved = new ArrayList<>();

        List<User> usersToSave = new ArrayList<>();
        // On créé 100 utilisateurs
        for (int i = 0; i < 100; i++) {
            User u = new User();
            u.setName("timeboard" + i);
            u.setPassword("timeboard" + i);
            u.setEmail("user" + i + "@timeboard.com");
            u.setImputationFutur(true);
            u.setBeginWorkDate(new Date());
            u.setFirstName("User" + i);
            u.setLogin("timeboard" + i);
            u.setAccountCreationTime(new Date());
            usersToSave.add(u);
            System.out.println("Stage user : "+u.getName());
        }

        try {
            userSaved = this.userService.createUsers(usersToSave);
            System.out.println("Save "+usersToSave.size()+" users");
        } catch (BusinessException e) {
            e.printStackTrace();
        }

        return userSaved;

    }


}
