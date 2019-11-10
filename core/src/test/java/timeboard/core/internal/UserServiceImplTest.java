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
import org.mockito.Mockito;
import org.osgi.service.log.LogService;
import timeboard.core.api.UserService;
import timeboard.core.api.exceptions.BusinessException;
import timeboard.core.model.User;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.Date;

public class UserServiceImplTest {

    private static JpaTemplate JPA;

    @BeforeAll
    public static void INIT(){
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("timeboard-pu-test", System.getProperties());
        EntityManager em = emf.createEntityManager();
        JpaTemplate jpaTemplate = new AbstractJpaTemplate(){

            @Override
            public <R> R txExpr(TransactionType transactionType, EmFunction<R> emFunction) {
                em.getTransaction().begin();
                R res = emFunction.apply(em);
                em.getTransaction().commit();
                return res;
            }
        };

        JPA = jpaTemplate;
    }

    /**
     * Test of successful user creation
     * @throws BusinessException
     */
    @Test
    public void createUserTest() throws BusinessException {

        LogService mockLogService = Mockito.mock(LogService.class);

        UserService userService = new UserServiceImpl(JPA, mockLogService);
        User newUser = new User(
                "login",
                "password",
                "name",
                "firstName",
                "em@il.com",
                new Date(),
                new Date());

        User createdUser = userService.createUser(newUser);
        Assertions.assertNotNull(createdUser.getId());

        User findedUser = userService.findUserByLogin("login");
        Assertions.assertNotNull(findedUser);
        Assertions.assertEquals(createdUser.getId(), findedUser.getId());


    }

}
