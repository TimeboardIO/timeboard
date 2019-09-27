package kronops.core.internal;

/*-
 * #%L
 * core
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
import kronops.core.model.Project;
import kronops.core.model.User;
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
     * Injected instance of kronops persistence unit.
     */
    @Reference(
            target = "(osgi.unit.name=kronops-pu)",
            scope = ReferenceScope.BUNDLE)
    private JpaTemplate jpa;


    @Override
    public User createUser(final User user) throws BusinessException {
        return this.jpa.txExpr(entityManager -> {
            entityManager.persist(user);
            return user;
        });
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
        return jpa.txExpr(entityManager -> entityManager
                .find(User.class, userID));
    }
}
