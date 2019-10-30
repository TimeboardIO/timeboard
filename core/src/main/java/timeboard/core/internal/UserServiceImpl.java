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

import org.mindrot.jbcrypt.BCrypt;
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

import javax.persistence.Query;
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

        user.setPassword(this.hashPassword(user.getPassword()));

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
                user.setPassword(this.hashPassword(user.getPassword()));
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
            this.logService.log(LogService.LOG_INFO, "User "+ user.getLogin()+" updated.");
            return user;
        });
    }

    @Override
    public void updateUserPassword(Long userID, String oldPassword, String newPassword) throws WrongPasswordException, UserException {

        User user = this.findUserByID(userID);
        if(user!=null && this.checkPassword(oldPassword, user.getPassword())){
            this.jpa.txExpr(entityManager -> {
                User u = entityManager.find(User.class, userID);

                u.setPassword(this.hashPassword(newPassword));

                entityManager.persist(u);
                this.logService.log(LogService.LOG_INFO, "User " + u.getLogin() + " successfully change his password.");
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
        User u = this.jpa.txExpr(entityManager -> {
            TypedQuery<User> q = entityManager
                    .createQuery("select u from User u "
                            + "where u.login = :login ", User.class);
            q.setParameter("login", login);
            return q.getSingleResult();
        });

        if(!this.checkPassword(password, u.getPassword())) {
            u = null;
        }

        return u;
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

    @Override
    public User findUserByExternalID(String origin, String userExternalID) {
        return this.jpa.txExpr(entityManager -> {
            //select * from timeboard.User
            // where JSON_EXTRACT(externalIDS, "$.github") = 'nicolas-lefloch'
            Query q = entityManager
                    .createNativeQuery("select * from User "
                            + "where JSON_EXTRACT(externalIDs, '$."+origin.toLowerCase()+"')" +
                            "= ?", User.class);

            q.setParameter(1, userExternalID);

            User user = (User) q.getSingleResult();

            return user;
        });
    }

    private String hashPassword(String password){
       return BCrypt.hashpw(password, BCrypt.gensalt(12));
    }

    private boolean checkPassword(String password, String hash){
       return BCrypt.checkpw(password, hash);
    }
}
