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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import timeboard.core.api.UserService;
import timeboard.core.api.exceptions.BusinessException;
import timeboard.core.internal.rules.Rule;
import timeboard.core.internal.rules.RuleSet;
import timeboard.core.internal.rules.project.ActorIsProjectOwner;
import timeboard.core.model.Account;
import timeboard.core.model.Organization;
import timeboard.core.model.Project;

import javax.persistence.*;
import javax.transaction.Transactional;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


@Component
@Transactional
public class UserServiceImpl implements UserService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserServiceImpl.class);

    @Autowired
    private EntityManager em;

    @Override
    public List<Account> createUsers(final List<Account> accounts) {
        accounts.forEach(user -> {
            em.persist(user);
            LOGGER.info("User " + user.getFirstName() + " " + user.getName() + " created");
        });
        return accounts;
    }

    @Override
    public Account findUserByLogin(final String name) {
        if (name == null) {
            return null;
        }
        final TypedQuery<Account> q = em.createQuery("from Account u where u.localLogin=:name", Account.class);
        q.setParameter("name", name);
        Account account;
        try {
            account = q.getSingleResult();
        } catch (final NoResultException e) {
            account = null;
        }
        return account;
    }


    @Override
    @Transactional
    public Account createUser(final Account account) throws BusinessException {
        this.em.persist(account);
        LOGGER.info("User " + account.getFirstName() + " " + account.getName() + " created");
        this.em.flush();
        return account;
    }


    @Override
    public Account updateUser(final Account account) {
        final Account u = this.findUserByID(account.getId());
        if (u != null) {
            u.setFirstName(account.getFirstName());
            u.setName(account.getName());
            u.setEmail(account.getEmail());
            u.setExternalIDs(account.getExternalIDs());
        }
        em.flush();
        LOGGER.info("User " + account.getEmail() + " updated.");
        return account;

    }

    @Override
    public List<Account> searchUserByEmail(final Account actor, final String email) throws BusinessException {

        final TypedQuery<Account> q = em
                .createQuery(
                        "select u from Account u " +
                                "where u.email= :prefix ",
                        Account.class);
        q.setParameter("prefix", email);
        return q.getResultList();
    }

    @Override
    public List<Account> searchUserByEmail(final Account actor, final String email, final Organization org) throws BusinessException {

        final TypedQuery<Account> q = em
                .createQuery(
                        "select m.member from OrganizationMembership m " +
                                "where m.member.email LIKE CONCAT('%',:prefix,'%') and m.organization = :org",
                        Account.class);
        q.setParameter("prefix", email);
        q.setParameter("org", org);
        return q.getResultList();
    }

    @Override
    public List<Account> searchUserByEmail(final Account actor, final String prefix, final Project project) throws BusinessException {

        final RuleSet<Project> ruleSet = new RuleSet<>();
        ruleSet.addRule(new ActorIsProjectOwner());
        final Set<Rule> wrongRules = ruleSet.evaluate(actor, project);
        if (!wrongRules.isEmpty()) {
            throw new BusinessException(wrongRules);
        }

        final List<Account> matchedAccount = project.getMembers().stream()
                .filter(projectMembership -> projectMembership
                        .getMember()
                        .getEmail().startsWith(prefix))
                .map(projectMembership -> projectMembership.getMember())
                .collect(Collectors.toList());
        return matchedAccount;
    }


    @Override
    public Account findUserByID(final Long userID) {
        if (userID == null) {
            return null;
        }
        return em.find(Account.class, userID);
    }

    @Override
    public Account findUserByEmail(final String email) {
        if (email == null) {
            return null;
        }
        final TypedQuery<Account> q = em.createQuery("from Account u where u.email=:email", Account.class);
        q.setParameter("email", email);
        Account account;
        try {
            account = q.getSingleResult();
        } catch (final NoResultException e) {
            account = null;
        }
        return account;
    }

    @Override
    public Account findUserBySubject(final String remoteSubject) {
        final Account u;
        try {
            final Query q = em
                    .createQuery("select u from Account u where u.remoteSubject = :sub", Account.class);
            q.setParameter("sub", remoteSubject);
            return (Account) q.getSingleResult();
        } catch (final NoResultException | NonUniqueResultException e) {
            u = null;
        }
        return u;
    }


    @Override
    public Account findUserByExternalID(final String origin, final String userExternalID) {
        final Account u;
        try {
            final Query q = em // MYSQL native for JSON queries
                    .createNativeQuery("select * from Account "
                            + "where JSON_EXTRACT(externalIDs, '$." + origin + "')"
                            + " = ?", Account.class);
            q.setParameter(1, userExternalID);
            return (Account) q.getSingleResult();
        } catch (final PersistenceException e) {
            u = null;
        }
        return u;
    }

    @Override
    @Transactional
    public Account userProvisionning(final String sub, final String email) throws BusinessException {

        Account account = this.findUserBySubject(sub);
        if (account == null) {
            //Create user
            account = new Account(null, null, email, new Date());
            account.setRemoteSubject(sub);
            account = this.createUser(account);
        }
        return account;
    }


}
