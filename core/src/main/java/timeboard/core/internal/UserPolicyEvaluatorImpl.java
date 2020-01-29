package timeboard.core.internal;

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
