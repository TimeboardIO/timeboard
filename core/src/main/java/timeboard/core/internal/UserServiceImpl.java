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

import org.apache.aries.jpa.template.JpaTemplate;
import org.mindrot.jbcrypt.BCrypt;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceScope;
import org.osgi.service.log.LogService;
import timeboard.core.api.UserService;
import timeboard.core.api.exceptions.BusinessException;
import timeboard.core.model.Project;
import timeboard.core.model.User;

import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import java.util.Date;
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

    public UserServiceImpl() {

    }

    public UserServiceImpl(JpaTemplate jpaTemplate, LogService logService) {
        this.jpa = jpaTemplate;
        this.logService = logService;
    }

    @Override
    public List<User> createUsers(List<User> users) {
        return this.jpa.txExpr(entityManager -> {
            users.forEach(user -> {
                entityManager.persist(user);
                this.logService.log(LogService.LOG_INFO, "User " + user.getFirstName() + " " + user.getName() + " created");
            });
            return users;
        });
    }

    @Override
    public User createUser(final User user) throws BusinessException {


        return this.jpa.txExpr(entityManager -> {
            entityManager.persist(user);
            this.logService.log(LogService.LOG_INFO, "User " + user.getFirstName() + " " + user.getName() + " created");
            entityManager.flush();
            return user;
        });
    }


    @Override
    public User updateUser(User user) {
        return this.jpa.txExpr(entityManager -> {
            User u = this.findUserByID(user.getId());
            if (u != null) {
                u.setFirstName(user.getFirstName());
                u.setName(user.getName());
                u.setEmail(user.getEmail());
                u.setExternalIDs(user.getExternalIDs());
            }
            entityManager.flush();
            this.logService.log(LogService.LOG_INFO, "User " + user.getEmail() + " updated.");
            return user;
        });

    }


    @Override
    public List<User> searchUserByEmail(final String prefix) {
        return this.jpa.txExpr(entityManager -> {
            TypedQuery<User> q = entityManager
                    .createQuery(
                            "select u from User u "
                                    + "where u.email LIKE CONCAT('%',:prefix,'%')",
                            User.class);
            q.setParameter("prefix", prefix);
            return q.getResultList();
        });
    }

    @Override
    public List<User> searchUserByEmail(final String prefix, final Long pID) {
        return this.jpa.txExpr(entityManager -> {
            Project project = entityManager.find(Project.class, pID);
            List<User> matchedUser = project.getMembers().stream()
                    .filter(projectMembership -> projectMembership
                            .getMember()
                            .getEmail().startsWith(prefix))
                    .map(projectMembership -> projectMembership.getMember())
                    .collect(Collectors.toList());
            return matchedUser;
        });
    }


    @Override
    public User findUserByID(final Long userID) {
        if (userID == null) {
            return null;
        }
        return jpa.txExpr(entityManager -> entityManager
                .find(User.class, userID));
    }

    @Override
    public User findUserByEmail(String email) {
        if (email == null) {
            return null;
        }
        return jpa.txExpr(entityManager -> {
            TypedQuery<User> q = entityManager.createQuery("from User u where u.email=:email", User.class);
            q.setParameter("email", email);
            User user;
            try {
                user = q.getSingleResult();
            } catch (NoResultException e) {
                user = null;
            }
            return user;
        });
    }

    @Override
    public User findUserBySubject(String remoteSubject){
        User u;
        try {
            u = this.jpa.txExpr(entityManager -> {
                Query q = entityManager
                        .createQuery("select * from User u where u.remoteSubject = :sub", User.class);
                q.setParameter(1, remoteSubject);
                return (User) q.getSingleResult();
            });
        } catch (NoResultException | NonUniqueResultException e) {
            u = null;
        }
        return u;
    }


    @Override
    public User findUserByExternalID(String origin, String userExternalID) {
        User u;
        try {
            u = this.jpa.txExpr(entityManager -> {
                Query q = entityManager // MYSQL native for JSON queries
                        .createNativeQuery("select * from User "
                                + "where JSON_EXTRACT(externalIDs, '$." + origin + "')" +
                                " = ?", User.class);
                q.setParameter(1, userExternalID);
                return (User) q.getSingleResult();
            });
        } catch (javax.persistence.NoResultException e) {
            u = null;
        }
        return u;
    }

    @Override
    public User userProvisionning(String sub, String email) throws BusinessException {

        User user = this.findUserBySubject(sub);
        if (user == null) {
            //Create user
            user = new User(null, null, email, new Date(), new Date());
            user.setRemoteSubject(sub);
            user = this.createUser(user);
        }
        return user;
    }

    private String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt(12));
    }

    private boolean checkPassword(String password, String hash) {
        return BCrypt.checkpw(password, hash);
    }
}
