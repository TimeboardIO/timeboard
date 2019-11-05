package timeboard.core.internal;

/*-
 * #%L
 * core
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
import org.apache.aries.jpa.support.impl.AbstractJpaTemplate;
import org.apache.aries.jpa.template.EmFunction;
import org.apache.aries.jpa.template.JpaTemplate;
import org.apache.aries.jpa.template.TransactionType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.osgi.service.log.LogService;
import timeboard.core.api.ProjectService;
import timeboard.core.api.UserService;
import timeboard.core.api.exceptions.BusinessException;
import timeboard.core.api.exceptions.UserException;
import timeboard.core.api.exceptions.WrongPasswordException;
import timeboard.core.internal.ProjectServiceImpl;
import timeboard.core.model.Task;
import timeboard.core.model.User;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class UserServiceImplTest {

    private static JpaTemplate JPA;

    private LogService mockLogService;
    private UserService userService;

    @BeforeAll
    public void INIT(){
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("timeboard-pu-test", System.getProperties());
        EntityManager em = emf.createEntityManager();
        JpaTemplate jpaTemplate = new AbstractJpaTemplate(){

            @Override
            public <R> R txExpr(TransactionType transactionType, EmFunction<R> emFunction) {
                return emFunction.apply(em);
            }
        };

        JPA = jpaTemplate;


        this.mockLogService = Mockito.mock(LogService.class);
        this.userService = new UserServiceImpl(JPA, this.mockLogService);
    }

    /**
     * Test of successful user creation
     * @throws BusinessException
     */
    @Test
    public void createUserTest() throws BusinessException {
        User newUser = new User(
                "login",
                "password",
                "name",
                "firstName",
                "em@il.com",
                new Date(),
                new Date());

        User createdUser = this.userService.createUser(newUser);
        Assertions.assertNotNull(createdUser);

        User myUserInDB = this.userService.findUserByLogin(createdUser.getLogin());
        Assertions.assertNotNull(myUserInDB);
        Assertions.assertNotNull(myUserInDB.getId());
        Assertions.assertEquals(myUserInDB.getFirstName(), createdUser.getFirstName());
    }


    /**
     * Test of successful user list creation
     * @throws BusinessException
     */
    @Test
    public void createUsersTest() throws BusinessException {

        List<User> usersList = new ArrayList<User>();
        User newUser1 = new User(
                "login1",
                "password1",
                "name1",
                "firstName1",
                "em1@il.com",
                new Date(),
                new Date());
        usersList.add(newUser1);

        User newUser2 = new User(
                "login2",
                "password2",
                "name2",
                "firstName2",
                "em2@il.com",
                new Date(),
                new Date());
        usersList.add(newUser2);

        User newUser3 = new User(
                "login3",
                "password3",
                "name3",
                "firstName3",
                "em3@il.com",
                new Date(),
                new Date());
        usersList.add(newUser3);

        List<User> createdUsers = this.userService.createUsers(usersList);
        Assertions.assertNotNull(createdUsers);

        Assertions.assertNotNull(userService.findUserByLogin(newUser1.getLogin()));
        Assertions.assertNotNull(userService.findUserByLogin(newUser2.getLogin()));
        Assertions.assertNotNull(userService.findUserByLogin(newUser3.getLogin()));
    }


    /**
     * Test of successful user update
     * @throws BusinessException
     */
    @Test
    public void updateUserTest() throws BusinessException {
        User newUser = new User(
                "login",
                "password",
                "name",
                "firstName",
                "em@il.com",
                new Date(),
                new Date());
        User createdUser = this.userService.createUser(newUser);

        newUser.setLogin("newLogin");
        newUser.setFirstName("newFirstname");
        User updatedUser = this.userService.updateUser(newUser);
        Assertions.assertNotNull(updatedUser);

        User myUserInDB = this.userService.findUserByLogin(updatedUser.getLogin());
        Assertions.assertNotNull(myUserInDB);
        Assertions.assertNotEquals(myUserInDB.getFirstName(), createdUser.getFirstName());
        Assertions.assertEquals(myUserInDB.getFirstName(), updatedUser.getFirstName());
    }


    /**
     * Test of failure user update
     * @throws BusinessException
     */
    @Test
    public void updateUserKOTest() throws BusinessException {
        User oldUser = new User(
                "login",
                "password",
                "name",
                "firstName",
                "em@il.com",
                new Date(),
                new Date());
        User createdUser = this.userService.createUser(oldUser);
        Assertions.assertNotNull(createdUser);

        User newUser = new User(
                "Alogin",
                "Apassword",
                "Aname",
                "AfirstName",
                "Aem@il.com",
                new Date(),
                new Date());
     /*   User updatedUser = this.userService.updateUser(newUser);
        Assertions.assertNotNull(updatedUser);


        User myOldUserInDB = this.userService.findUserByLogin(updatedUser.getLogin());
        Assertions.assertNotNull(myOldUserInDB);
        User myNewUserInDB = this.userService.findUserByLogin(updatedUser.getLogin());
        Assertions.assertNotNull(myNewUserInDB);*/
    }


    /**
     * Test of successful user password update
     * @throws WrongPasswordException
     * @throws BusinessException
     * @throws UserException
     */
    @Test
    public void updateUserPasswordTest() throws WrongPasswordException, UserException, BusinessException {
        User newUser = new User(
                "login",
                "password",
                "name",
                "firstName",
                "em@il.com",
                new Date(),
                new Date());

        User createdUser = this.userService.createUser(newUser);
        Assertions.assertNotNull(createdUser);

        this.userService.updateUserPassword(createdUser.getId(), "password", "newPassword");


    }


    /**
     * Test of failure user password update
     * @throws WrongPasswordException
     * @throws BusinessException
     * @throws UserException
     */
    @Test
    public void updateUserPasswordKOTest() throws WrongPasswordException, UserException, BusinessException {
        User newUser = new User(
                "login",
                "password",
                "name",
                "firstName",
                "em@il.com",
                new Date(),
                new Date());

        User createdUser = this.userService.createUser(newUser);
        Assertions.assertNotNull(createdUser);

        this.userService.updateUserPassword(createdUser.getId(), "KOpasswordKO", "newPassword");


    }


    /**
     * Test of successful user generated password update
     * @throws BusinessException
     * @throws UserException
     */
    @Test
    public void updateUserGeneratedPassword() throws BusinessException, UserException {

    }


    /**
     * Test of failure user generated password update
     * @throws BusinessException
     * @throws UserException
     */
    @Test
    public void updateUserGeneratedKOPassword() throws BusinessException, UserException {

    }

}
