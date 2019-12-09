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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import timeboard.core.api.UserService;
import timeboard.core.api.exceptions.BusinessException;
import timeboard.core.model.Project;
import timeboard.core.model.User;

import javax.persistence.*;
import javax.transaction.Transactional;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;



@Component
@Transactional
public class UserServiceImpl implements UserService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserServiceImpl.class);

    @Autowired
    private EntityManager em;

    @Override
    public List<User> createUsers(List<User> users) {
             users.forEach(user -> {
                em.persist(user);
                 LOGGER.info("User " + user.getFirstName() + " " + user.getName() + " created");
            });
            return users;
     }

    @Override
    @Transactional
    public User createUser(final User user) throws BusinessException {
        this.em.persist(user);
        LOGGER.info("User " + user.getFirstName() + " " + user.getName() + " created");
        this.em.flush();
        return user;
     }


    @Override
    public User updateUser(User user) {
             User u = this.findUserByID(user.getId());
            if (u != null) {
                u.setFirstName(user.getFirstName());
                u.setName(user.getName());
                u.setEmail(user.getEmail());
                u.setExternalIDs(user.getExternalIDs());
            }
            em.flush();
            LOGGER.info("User " + user.getEmail() + " updated.");
            return user;

    }


    @Override
    public List<User> searchUserByEmail(final String prefix) {
             TypedQuery<User> q = em
                    .createQuery(
                            "select u from User u "
                                    + "where u.email LIKE CONCAT('%',:prefix,'%')",
                            User.class);
            q.setParameter("prefix", prefix);
            return q.getResultList();
     }

    @Override
    public List<User> searchUserByEmail(final String prefix, final Long projectId) {
             Project project = em.find(Project.class, projectId);
            List<User> matchedUser = project.getMembers().stream()
                    .filter(projectMembership -> projectMembership
                            .getMember()
                            .getEmail().startsWith(prefix))
                    .map(projectMembership -> projectMembership.getMember())
                    .collect(Collectors.toList());
            return matchedUser;
     }


    @Override
    public User findUserByID(final Long userID) {
        if (userID == null) {
            return null;
        }
        return em.find(User.class, userID);
    }

    @Override
    public User findUserByEmail(String email) {
        if (email == null) {
            return null;
        }
             TypedQuery<User> q = em.createQuery("from User u where u.email=:email", User.class);
            q.setParameter("email", email);
            User user;
            try {
                user = q.getSingleResult();
            } catch (NoResultException e) {
                user = null;
            }
            return user;
     }

    @Override
    public User findUserBySubject(String remoteSubject) {
        User u;
        try {
                 Query q = em
                        .createQuery("select u from User u where u.remoteSubject = :sub", User.class);
                q.setParameter("sub", remoteSubject);
                return (User) q.getSingleResult();
         } catch (NoResultException | NonUniqueResultException e) {
            u = null;
        }
        return u;
    }


    @Override
    public User findUserByExternalID(String origin, String userExternalID) {
        User u;
        try {
                 Query q = em // MYSQL native for JSON queries
                        .createNativeQuery("select * from User "
                                + "where JSON_EXTRACT(externalIDs, '$." + origin + "')"
                                + " = ?", User.class);
                q.setParameter(1, userExternalID);
                return (User) q.getSingleResult();
         } catch (javax.persistence.NoResultException e) {
            u = null;
        }
        return u;
    }

    @Override
    @Transactional
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
