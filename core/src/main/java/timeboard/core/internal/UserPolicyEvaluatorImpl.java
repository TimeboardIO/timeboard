package timeboard.core.internal;

/*-
 * #%L
 * core
 * %%
 * Copyright (C) 2019 - 2020 Timeboard
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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import timeboard.core.api.UserPolicyEvaluator;
import timeboard.core.model.Account;
import timeboard.core.model.MembershipRole;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;

@Component
@Transactional
public class UserPolicyEvaluatorImpl implements UserPolicyEvaluator {

    @Autowired
    private EntityManager em;

    public boolean isOwnerOfAnyUserProject(Account owner, Account user) {
        final TypedQuery<Account> q = em.createQuery("SELECT DISTINCT m2.member " +
                "FROM ProjectMembership m1 JOIN ProjectMembership m2 " +
                "ON m1.project = m2.project " +
                "WHERE m1.member = :user " +
                "AND m2.member = :owner " +
                "AND m2.role = :role", Account.class);
        q.setParameter("user", user);
        q.setParameter("owner", owner);
        q.setParameter("role", MembershipRole.OWNER);


        try {
            final Account singleResult = q.getSingleResult();
            if(singleResult != null ) {
                return true;
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }
}
