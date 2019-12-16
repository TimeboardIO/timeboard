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
import timeboard.core.model.MembershipRole;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;


@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
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
        ah.setRole(MembershipRole.OWNER);
        ah.setStartDate(new Date());
        this.em.persist(ah);

        organization.setIsOrganization(true);
        this.em.persist(organization);

        LOGGER.info("User " + actor.getFirstName() + " " + actor.getName() + " created organization "+organization.getName());
        this.em.flush();
        return organization;
    }

    @Override
    public Optional<Account> getOrganizationByID(Account actor, long id) {
        Account data;
        try {
            data = em.createQuery("select o from Account o  where o.id = :orgID", Account.class)
                    .setParameter("orgID", id)
                    .getSingleResult();
        }catch (NoResultException nre){
            data = null;
        }
        return Optional.ofNullable(data);
    }

    @Override
    public Account updateOrganization(Account actor, Account organization) {

        em.merge(organization);
        em.flush();

        LOGGER.info("Project " + organization.getName() + " updated");
        return organization;
    }

    @Override
    public AccountHierarchy addMember(final Account actor, Account organization, Account member) throws BusinessException {

        List<AccountHierarchy> existingAH = em.createQuery("select h from AccountHierarchy h join h.organization o " +
                "where h.member = :member and h.organization = :organization " +
                "and o.isOrganization = true", AccountHierarchy.class)
                .setParameter("member", member)
                .setParameter("organization", organization)
                .getResultList();
        if (!existingAH.isEmpty()) throw new BusinessException("Organization "+organization.getScreenName()+" already have a parent");

        AccountHierarchy ah = new AccountHierarchy();

        ah.setMember(member);
        ah.setOrganization(organization);
        ah.setRole(MembershipRole.CONTRIBUTOR);
        ah.setStartDate(new Date());
        this.em.persist(ah);
        em.flush();

        return ah;
    }

    @Override
    public AccountHierarchy removeMember(final Account actor, Account organization, Account member) throws BusinessException{

        AccountHierarchy ah = em.createQuery("select h from AccountHierarchy h where h.member = :member and h.organization = :organization", AccountHierarchy.class)
                .setParameter("member", member)
                .setParameter("organization", organization)
                .getSingleResult();

        if(ah.getRole() == MembershipRole.OWNER){
            throw new BusinessException("Can not remove an organization owner");
        }
        em.remove(ah);
        organization.getMembers().remove(ah);
        em.merge(organization);
        em.flush();

        return ah;
    }


    @Override
    public AccountHierarchy updateMemberRole(final Account actor, Account organization, Account member, MembershipRole role) {
        AccountHierarchy ah = em.createQuery("select h from AccountHierarchy h where h.member = :member and h.organization = :organization", AccountHierarchy.class)
                .setParameter("member", member)
                .setParameter("organization", organization)
                .getSingleResult();

        ah.setRole(role);
        em.merge(ah);
        em.flush();

        return ah;
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
