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

    @Value("${timeboard.quotas.account.projects}")
    private int limitProjectsByUser;

    @Value("${timeboard.quotas.project.tasks}")
    private int limitTasksByProject;

    public boolean checkProjectByUserLimit(Account actor) throws CommercialException {
        int numberProjectByUser = this.getNumberProjectsByUser(actor);
        if (numberProjectByUser >= limitProjectsByUser) {
            throw new CommercialException("Limit reached",
                "Project's creation impossible for " + actor.getScreenName() + "!\n" +
                "Too many projects in this account !");
        }
        return true;
    }

    public boolean checkTaskByProjectLimit(Account actor, Project project) throws CommercialException {
        int numberTasksByProject = this.getNumberTasksByProject(actor, project);
        if (numberTasksByProject >= limitTasksByProject) {
            throw new CommercialException("Limit reached",
                "Task's creation impossible for " + actor.getScreenName() + "!\n" +
                "Too many task in project " + project.getName() + "in this account !");
        }
        return true;
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
