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

import org.osgi.service.log.LogService;
import timeboard.core.api.UserService;
import timeboard.core.api.exceptions.BusinessException;
import timeboard.core.api.exceptions.UserException;
import timeboard.core.api.exceptions.WrongPasswordException;
import timeboard.core.model.Project;
import timeboard.core.model.User;
import org.apache.aries.jpa.template.JpaTemplate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceScope;

import javax.persistence.TypedQuery;
import java.util.List;
import java.util.stream.Collectors;

@Component(
        service = UserService.class,
        immediate = true
)
public final class UserServiceImpl implements UserService {

    /**
     * Injected instance of timeboard persistence unit.
     */
    @Reference(
            target = "(osgi.unit.name=timeboard-pu)",
            scope = ReferenceScope.BUNDLE)
    private JpaTemplate jpa;

    @Reference
    private LogService logService;

    @Override
    public User createUser(final User user) throws BusinessException {
        return this.jpa.txExpr(entityManager -> {
            entityManager.persist(user);
            this.logService.log(LogService.LOG_INFO, "User " + user.getFirstName() + " " + user.getName() + " created");
            return user;
        });
    }

    @Override
    public List<User> createUsers(List<User> users) throws BusinessException {
        return this.jpa.txExpr(entityManager -> {
            users.forEach(user -> {
                entityManager.persist(user);
                this.logService.log(LogService.LOG_INFO, "User " + user.getFirstName() + " " + user.getName() + " created");
            });
            return users;
        });
    }

    @Override
    public User updateUser(User user) {
        return this.jpa.txExpr(entityManager -> {

            User u = entityManager.find(User.class, user.getId());
            u.setFirstName(user.getFirstName());
            u.setName(user.getName());
            u.setLogin(user.getLogin());
            u.setEmail(user.getEmail());

            entityManager.persist(u);
            logservice.log(LogService.LOG_INFO, "User "+ user.getLogin()+" updated.");
            return user;
        });
    }

    @Override
    public void updateUserPassword(Long userID, String oldPassword, String newPassword) throws WrongPasswordException, UserException {

        User user = this.findUserByID(userID);
        if(user!=null && user.getPassword().matches(oldPassword)){
            this.jpa.txExpr(entityManager -> {
                User u = entityManager.find(User.class, userID);
                entityManager.persist(u);
                logservice.log(LogService.LOG_INFO, "User " + u.getLogin() + " successfully change his password.");
                return u;
            });
        }else if(user != null){
            throw new WrongPasswordException();
        }else{
            throw new UserException("User does not exist.");
        }

    }

    @Override
    public List<User> searchUserByName(final String prefix) {
        return this.jpa.txExpr(entityManager -> {
            TypedQuery<User> q = entityManager
                    .createQuery(
                            "select u from User u "
                                    + "where u.name LIKE CONCAT('%',:prefix,'%')",
                            User.class);
            q.setParameter("prefix", prefix);
            return q.getResultList();
        });
    }

    @Override
    public List<User> searchUserByName(final String prefix, final Long pID) {
        return this.jpa.txExpr(entityManager -> {
            Project project = entityManager.find(Project.class, pID);
            List<User> matchedUser = project.getMembers().stream()
                    .filter(projectMembership -> projectMembership
                            .getMember()
                            .getName().startsWith(prefix))
                    .map(projectMembership -> projectMembership.getMember())
                    .collect(Collectors.toList());
            return matchedUser;
        });
    }

    @Override
    public User autenticateUser(final String login, final String password) {
        return this.jpa.txExpr(entityManager -> {
            TypedQuery<User> q = entityManager
                    .createQuery("select u from User u "
                            + "where u.login = :login "
                            + "and u.password = :password", User.class);
            q.setParameter("login", login);
            q.setParameter("password", password);
            return q.getSingleResult();
        });
    }

    @Override
    public User findUserByLogin(final String login) {
        return this.jpa.txExpr(entityManager -> {
            TypedQuery<User> q = entityManager
                    .createQuery("select u from User u "
                            + "where u.login = :login", User.class);
            q.setParameter("login", login);
            return q.getSingleResult();
        });
    }

    @Override
    public User findUserByID(final Long userID) {
        if(userID == null){
            return null;
        }
        return jpa.txExpr(entityManager -> entityManager
                .find(User.class, userID));
    }


}
