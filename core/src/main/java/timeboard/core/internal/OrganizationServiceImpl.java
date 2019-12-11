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
import timeboard.core.api.OrganizationService;
import timeboard.core.api.exceptions.BusinessException;
import timeboard.core.model.Account;
import timeboard.core.model.AccountHierarchy;

import javax.persistence.*;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


@Component
@Transactional
public class OrganizationServiceImpl implements OrganizationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrganizationServiceImpl.class);

    @Autowired
    private EntityManager em;


    @Override
    public Account createOrganization(Account actor, Account organization) throws BusinessException {

        AccountHierarchy ah = new AccountHierarchy();
        ah.setMember(actor);
        ah.setOrganization(organization);
        ah.setStartDate(new Date());
        this.em.persist(ah);

        organization.setIsOrganization(true);
        this.em.persist(organization);

        LOGGER.info("User " + actor.getFirstName() + " " + actor.getName() + " created organization "+organization.getName());
        this.em.flush();
        return organization;
    }

    @Override
    public Account getOrganizationByID(Account actor, long id) {
        Account data = em.createQuery("select o from Account o  where o.id = :orgID", Account.class)
                .setParameter("orgID", id)
                .getSingleResult();
        return data;
    }

    @Override
    public Account updateOrganization(Account actor, Account organization) {

        em.merge(organization);
        em.flush();

        LOGGER.info("Project " + organization.getName() + " updated");
        return organization;
    }

    @Override
    public void removeMember(Account actor, Account organization, Account member) {

    }

    @Override
    public List<Account> getParents(Account actor, Account organization) {
        return this.em.createQuery("select o from AccountHierarchy h join h.member m join h.organization o where m = :member", Account.class)
                .setParameter("member", organization).getResultList();
    }

    @Override
    public List<Account> getMembers(Account actor, Account organization) {
        if(!organization.getIsOrganization()) return new ArrayList<>();
        return this.em.createQuery("select m from AccountHierarchy h join h.member m join h.organization o where o = :org", Account.class)
                .setParameter("org", organization).getResultList();

    }
}
