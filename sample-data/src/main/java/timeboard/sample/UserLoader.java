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
import timeboard.core.model.Account;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class UserLoader {

    UserService userService;

    UserLoader(UserService userService){
        this.userService = userService;
    }

    public List<Account> load(int nbUsers) throws BusinessException {
        List<Account> accountSaved = new ArrayList<>();

        List<Account> usersToSave = new ArrayList<>();
        // On créé "nbUsers" utilisateurs
        for (int i = 0; i < nbUsers; i++) {
            Account u = new Account();
            u.setName("timeboard" + i);
            u.setEmail("user" + i + "@timeboard.com");
            u.setBeginWorkDate(new Date());
            u.setFirstName("User" + i);
            u.setAccountCreationTime(new Date());
            usersToSave.add(u);
         }

            accountSaved = this.userService.createUsers(usersToSave);


        return accountSaved;

    }


}
