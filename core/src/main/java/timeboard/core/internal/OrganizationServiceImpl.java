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
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.stereotype.Component;
import timeboard.core.api.OrganizationService;
import timeboard.core.api.exceptions.BusinessException;
import timeboard.core.model.Account;
import timeboard.core.model.MembershipRole;
import timeboard.core.model.Organization;
import timeboard.core.model.OrganizationMembership;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.util.Date;
import java.util.Optional;


@Component
@Transactional
public class OrganizationServiceImpl implements OrganizationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrganizationServiceImpl.class);

    @Autowired
    private EntityManager em;

    @Override
    public Organization createOrganization(final Account actor, String organizationName) throws BusinessException {

        this.em.merge(actor);

        final Organization organization = new Organization();
        organization.setName(organizationName);
        organization.setCreatedDate(new Date());
        this.em.persist(organization);

        final OrganizationMembership organizationMembership = new OrganizationMembership();
        organizationMembership.setMember(actor);
        organizationMembership.setRole(MembershipRole.OWNER);
        organizationMembership.setOrganization(organization);

        this.em.persist(organizationMembership);

        LOGGER.info("User " + actor.getFirstName() + " " + actor.getName() + " created organization " + organization.getName());
        return organization;
    }

    @Override
    @PostAuthorize("#actor.isMemberOf(returnObject)")
    public Optional<Organization> getOrganizationByID(Account actor, long id) {
        Organization data;
        try {
            data = em.find(Organization.class, id);
        } catch (Exception nre) {
            data = null;
        }
        return Optional.ofNullable(data);
    }

    @Override
    public Organization updateOrganization(Account actor, Organization organization) {

        em.merge(organization);
        em.flush();

        LOGGER.info("Project " + organization.getName() + " updated");
        return organization;
    }

    @Override
    public Optional<Organization> addMember(final Account actor, Organization organization, Account member) throws BusinessException {

        final Optional<Organization> org = this.getOrganizationByID(actor, organization.getId());

        if(org.isPresent()){

            final OrganizationMembership om = new OrganizationMembership();
            om.setOrganization(org.get());
            om.setMember(member);
            om.setRole(MembershipRole.CONTRIBUTOR);
            this.em.persist(om);

            org.get().getMembers().add(om);

            em.flush();

            LOGGER.info("Member " + actor.getName() + " is added to organisation "+org.get().getName());
        }


        return org;
    }

    @Override
    public Optional<Organization> removeMember(final Account actor, Organization organization, Account member) throws BusinessException {

        final Optional<Organization> org = this.getOrganizationByID(actor, organization.getId());

        if(org.isPresent()){

            org.get()
                    .getMembers()
                    .removeIf(om -> om.getMember().getId() == member.getId());

            em.flush();

            LOGGER.info("Member " + actor.getName() + " is removed from organisation "+org.get().getName());

        }

        return org;
    }


    @Override
    public Optional<Organization> updateMemberRole(final Account actor,
                                                   final Organization organization,
                                                   final Account member, final MembershipRole role) {

        final Optional<Organization> org = this.getOrganizationByID(actor, organization.getId());

        if(org.isPresent()){

            org.get()
                    .getMembers()
                    .stream()
                    .filter(om -> om.getMember().getId() == member.getId())
                    .forEach(om -> om.setRole(role));

            em.flush();

            LOGGER.info("Member " + actor.getName() + " has changed role from organization "+org.get().getName() + " to "+role);

        }

        return org;
    }



}
