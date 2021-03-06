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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import timeboard.core.api.exceptions.CommercialException;
import timeboard.core.model.Account;
import timeboard.core.model.Organization;
import timeboard.core.model.Project;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;

@Component(value = "bpe")
@Transactional
public class BusinessPolicyEvaluatorImpl implements timeboard.core.api.BusinessPolicyEvaluator {

    private static Logger LOGGER = LoggerFactory.getLogger(BusinessPolicyEvaluatorImpl.class);

    @Autowired
    private EntityManager entityManager;

    @Value("${timeboard.quotas.account.projects}")
    private int limitProjectsByUser;

    @Value("${timeboard.quotas.project.tasks}")
    private int limitTasksByProject;

    @Value("${timeboard.quotas.organization}")
    private int limitOrganizationsInApp;

    @Value("${timeboard.quotas.organization.projects}")
    private int limitProjectsByOrganization;

    public BusinessPolicyEvaluatorImpl() {
    }

    @Override
    public boolean checkOrganizationLimit(final Account actor) throws CommercialException {
        final int numberOrganizationInApp = this.getNumberOrganizationInApp(actor);
        if (numberOrganizationInApp >= limitOrganizationsInApp) {
            final String message = "Limit reached " + numberOrganizationInApp + "/" + limitOrganizationsInApp +
                    " Organization's creation impossible for " + actor.getScreenName() + "!";
            LOGGER.warn(message);
            throw new CommercialException(message);
        }
        return true;
    }

    @Override
    public boolean checkProjectsByOrganizationLimit(final Account actor, final Organization organization) throws CommercialException {
        final int numberProjectsByOrganization = this.getNumberProjectsByOrganization(organization);
        if (numberProjectsByOrganization >= limitProjectsByOrganization) {
            final String message = "Limit reached " + numberProjectsByOrganization + "/" + limitProjectsByOrganization +
                    " Project's creation in this organization impossible for " + actor.getScreenName() + "!";
            LOGGER.warn(message);
            throw new CommercialException(message);
        }
        return true;
    }

    @Override
    public boolean checkProjectByUserLimit(final Account actor) throws CommercialException {
        final int numberProjectByUser = this.getNumberProjectsByUser(actor);
        if (numberProjectByUser >= limitProjectsByUser) {
            final String message = "Limit reached " + numberProjectByUser + "/" + limitProjectsByUser +
                    " Project's creation impossible for " + actor.getScreenName() + "!";
            LOGGER.warn(message);
            throw new CommercialException(message);
        }
        return true;
    }

    @Override
    public boolean checkTaskByProjectLimit(final Account actor, final Project project) throws CommercialException {
        final int numberTasksByProject = this.getNumberTasksByProject(actor, project);
        if (numberTasksByProject >= limitTasksByProject) {
            final String message = "Limit reached " + numberTasksByProject + "/" + limitTasksByProject +
                    " Project's creation impossible for " + actor.getScreenName() + "!";

            LOGGER.warn(message);
            throw new CommercialException(message);
        }
        return true;
    }

    @Override
    public int getNumberProjectsByUser(final Account account) {
        final TypedQuery<Object> query = this.entityManager.createQuery(
                "select count(p) from " +
                        "Project p join p.members memberships " +
                        "where memberships.member = :account", Object.class);
        query.setParameter("account", account);
        return Integer.parseInt(query.getSingleResult().toString());
    }

    @Override
    public int getNumberTasksByProject(final Account account, final Project project) {
        final TypedQuery<Object> q = this.entityManager.createQuery(
                "select count(t) from Task t where t.project = :project", Object.class);
        q.setParameter("project", project);
        return Integer.parseInt(q.getSingleResult().toString());
    }

    @Override
    public int getNumberOrganizationInApp(final Account actor) {
        final TypedQuery<Object> q = this.entityManager.createQuery(
                "select count(o) " +
                        "from Organization o join o.members members " +
                        "where members.member = :actor", Object.class);
        q.setParameter("actor", actor);
        return Integer.parseInt(q.getSingleResult().toString());
    }

    @Override
    public int getNumberProjectsByOrganization(final Organization organization) {
        final TypedQuery<Object> q = this.entityManager.createQuery(
                "select count(p) " +
                        "from Project p " +
                        "where p.organizationID = :organizationID", Object.class);
        q.setParameter("organizationID", organization.getId());
        return Integer.parseInt(q.getSingleResult().toString());
    }
}
