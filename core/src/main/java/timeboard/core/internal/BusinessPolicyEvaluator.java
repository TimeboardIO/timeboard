package timeboard.core.internal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import timeboard.core.api.exceptions.CommercialException;
import timeboard.core.model.Account;
import timeboard.core.model.Project;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;

@Component(value = "bpe")
@Transactional
public class BusinessPolicyEvaluator  {

    @Autowired
    private EntityManager entityManager;

    @Value("${timeboard.projects.user.limit}")
    private int limitProjectsByUser;

    @Value("${timeboard.tasks.project.limit}")
    private int limitTasksByProject;

    public void checkProjectByUserLimit(Account actor) throws CommercialException {
        int numberProjectByUser = this.getNumberProjectsByUser(actor);
        if (numberProjectByUser >= limitProjectsByUser) {
            throw new CommercialException("Limit reached :\n" +
                "Project's creation impossible for " + actor.getScreenName() + "!\n" +
                "Too many projects in this account !");
        }
    }

    public void checkTaskByProjectLimit(Account actor, Project project) throws CommercialException {
        int numberTasksByProject = this.getNumberProjectsByUser(actor);
        if (numberTasksByProject >= limitTasksByProject) {
            throw new CommercialException("Limit reached :\n" +
                "Task's creation impossible for " + actor.getScreenName() + "!\n" +
                "Too many task in project " + project.getName() + "in this account !");
        }
    }

    private int getNumberProjectsByUser(Account account) {
        TypedQuery<Object> query = this.entityManager.createQuery(
                "select count(p) from Project p join p.members memberships where memberships.member = :account", Object.class);
        query.setParameter("account", account);
        return Integer.parseInt(query.getSingleResult().toString());
    }

    private int getNumberTasksByProject(Account account, Project project) {
        TypedQuery<Object> q = this.entityManager.createQuery(
                "select count(t) from Task t where t.project = :project", Object.class);
        q.setParameter("project", project);
        return Integer.parseInt(q.getSingleResult().toString());
    }
}
