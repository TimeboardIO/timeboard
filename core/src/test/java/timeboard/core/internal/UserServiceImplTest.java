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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.osgi.service.log.LogService;
import timeboard.core.api.UserService;
import timeboard.core.api.exceptions.BusinessException;
import timeboard.core.api.exceptions.UserException;
import timeboard.core.api.exceptions.WrongPasswordException;
import timeboard.core.model.User;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class UserServiceImplTest {

    private static JpaTemplate JPA;

    private static LogService mockLogService;
    private static UserService userService;

    @BeforeAll
    public static void INIT(){
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("timeboard-pu-test", System.getProperties());
        EntityManager em = emf.createEntityManager();
        JpaTemplate jpaTemplate = new AbstractJpaTemplate(){

            @Override
            public <R> R txExpr(TransactionType transactionType, EmFunction<R> emFunction) {
                return emFunction.apply(em);
            }
        };

        JPA = jpaTemplate;

        mockLogService = Mockito.mock(LogService.class);
        userService = new UserServiceImpl(JPA, mockLogService);
    }

    @BeforeEach
    public void resetDB(){
        this.JPA.txExpr(entityManager -> {
            Query q = entityManager
                    .createQuery("TRUNCATE TABLE timeboard.User");
            q.executeUpdate();
            return null;
        });
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

        User myUserInDB = this.userService.findUserByLogin("login");
        Assertions.assertNotNull(myUserInDB);
        Assertions.assertNotNull(myUserInDB.getId());
        Assertions.assertEquals(myUserInDB.getFirstName(), "firstName");
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

        createdUser.setLogin("newLogin");
        createdUser.setFirstName("newFirstname");
        User updatedUser = this.userService.updateUser(createdUser);
        Assertions.assertNotNull(updatedUser);

        User myUserInDB = this.userService.findUserByLogin(updatedUser.getLogin());
        Assertions.assertNotNull(myUserInDB);
        Assertions.assertNotEquals(myUserInDB.getFirstName(), "firstName");
        Assertions.assertEquals(myUserInDB.getFirstName(), "newFirstname");
        Assertions.assertNotEquals(myUserInDB.getLogin(), "login");
        Assertions.assertEquals(myUserInDB.getLogin(), "newLogin");
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

        Mockito.when(this.userService.updateUser(newUser)).thenThrow(NoResultException.class);
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

        this.userService.updateUserPassword(createdUser.getId(), "password", "newPassword");
        User userAuth = this.userService.autenticateUser("login", "newPassword");
        Assertions.assertNotNull(userAuth);

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

        Mockito.doThrow(UserException.class).when(this.userService).updateUserPassword((long) 123, "password", "newPassword");
        Mockito.doThrow(WrongPasswordException.class).when(this.userService).updateUserPassword(createdUser.getId(), "passwordKO", "newPassword");

        this.userService.updateUserPassword(createdUser.getId(), "password", "newPassword");
        User userAuth = this.userService.autenticateUser("login", "password");
        Assertions.assertNull(userAuth);
        userAuth = this.userService.autenticateUser("login", "newPassword");
        Assertions.assertNotNull(userAuth);


    }


    /**
     * Test of successful user generated password update
     * @throws BusinessException
     * @throws UserException
     */
    @Test
    public void updateUserGeneratedPassword() throws BusinessException, UserException {
        User newUser = new User(
                "login",
                "password",
                "name",
                "firstName",
                "em@il.com",
                new Date(),
                new Date());

        User createdUser = this.userService.createUser(newUser);

        this.userService.updateUserGeneratedPassword(createdUser.getId(), "newPassword");
        User userAuth = this.userService.autenticateUser("login", "newPassword");
        Assertions.assertNotNull(userAuth);
    }


    /**
     * Test of failure user generated password update
     * @throws BusinessException
     * @throws UserException
     */
    @Test
    public void updateUserGeneratedKOPassword() throws BusinessException, UserException {
        User newUser = new User(
                "login",
                "password",
                "name",
                "firstName",
                "em@il.com",
                new Date(),
                new Date());

        User createdUser = this.userService.createUser(newUser);

        Mockito.doThrow(UserException.class).when(this.userService).updateUserGeneratedPassword((long) 123, "newPassword");

        this.userService.updateUserGeneratedPassword(createdUser.getId(), "newPassword");
        User userAuth = this.userService.autenticateUser("login", "password");
        Assertions.assertNull(userAuth);
        userAuth = this.userService.autenticateUser("login", "newPassword");
        Assertions.assertNotNull(userAuth);
    }

}
