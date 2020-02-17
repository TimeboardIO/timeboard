package timeboard.core.internal.organization;

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
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.stereotype.Component;
import timeboard.core.api.OrganizationService;
import timeboard.core.api.exceptions.BusinessException;
import timeboard.core.model.*;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
import java.util.Calendar;
import java.util.*;


@Component
@Transactional
public class OrganizationServiceImpl implements OrganizationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrganizationServiceImpl.class);

    @Value("${timeboard.tasks.default.vacation}")
    private String defaultVacationTaskName;

    @Autowired
    private EntityManager em;

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;


    @Override
    public Organization createOrganization(final Account account, final String organizationName, final Map<String, String> properties) {

        final Organization organization = new Organization();
        organization.setName(organizationName);
        organization.setCreatedDate(Calendar.getInstance());
        organization.setSetup(properties);

        this.em.persist(organization);


        this.applicationEventPublisher.publishEvent(
                new CreateOrganizationEvent(account, organization));

        LOGGER.info("Organization " + organization.getName() + " created");

        return organization;
    }


    @Override
    @PostAuthorize("#actor.isMemberOf(returnObject)")
    @Cacheable(value = "organizationsCache", key = "#id")
    public Optional<Organization> getOrganizationByID(final Account actor, final long id) {
        Organization data;
        try {
            data = em.find(Organization.class, id);
        } catch (final Exception nre) {
            data = null;
        }
        return Optional.ofNullable(data);
    }

    @Override
    public Optional<Organization> getOrganizationByName(final String orgName) {
        Organization org = null;
        try {
            final TypedQuery<Organization> q = this.em.createNamedQuery(Organization.FIND_BY_NAME, Organization.class);
            q.setParameter("name", orgName);
            org = q.getSingleResult();
        } catch (final Exception e) {
            org = null;
        }
        return Optional.ofNullable(org);
    }

    @Override
    @CacheEvict(value = "organizationsCache", key = "#organization.getId()")
    public Optional<Organization> updateOrganization(final Account actor, final Organization organization) {

        final Optional<Organization> orgdb = this.getOrganizationByID(actor, organization.getId());
        if (orgdb.isPresent()) {
            orgdb.get().setName(organization.getName());
            orgdb.get().setCreatedDate(organization.getCreatedDate());
            this.em.merge(orgdb.get());
            this.em.flush();
            LOGGER.info("Organization " + organization.getName() + " is updated");
            return Optional.ofNullable(orgdb.get());
        }

        return Optional.empty();
    }

    @Override
    @CacheEvict(value = "organizationsCache", key = "#organization.getId()")
    public Optional<Organization> addMembership(final Account actor,
                                                final Organization organization,
                                                final Account member,
                                                final MembershipRole role) {

        final Optional<Organization> org = this.getOrganizationByID(actor, organization.getId());

        if (org.isPresent()) {

            final OrganizationMembership om = new OrganizationMembership();
            om.setOrganization(org.get());
            om.setMember(member);
            om.setRole(role);

            em.persist(om);
            org.get().getMembers().add(om);
            em.flush();

            LOGGER.info("Member " + member.getScreenName() + " is added to organisation " + org.get().getName() + " by +" + actor.getScreenName());
        }


        return org;
    }

    @Override
    @CacheEvict(value = "organizationsCache", key = "#o.getId()")
    public Optional<Organization> removeMembership(final Account actor, final Organization o, final Account member) {

        final Optional<Organization> organization = this.getOrganizationByID(actor, o.getId());
        if (organization.isPresent()) {
            final Optional<OrganizationMembership> membership = organization.get().getMembers()
                    .stream()
                    .filter(om -> om.getMember().getId() == member.getId())
                    .findFirst();

            if (membership.isPresent()) {
                em.remove(membership.get());
                LOGGER.info("Member " + member.getScreenName() + " is removed from organization " + organization.get().getName()
                        + " by " + actor.getScreenName());
            }

        }

        return organization;
    }


    @Override
    @CacheEvict(value = "organizationsCache", key = "#membership.getOrganization().getId()")
    public Optional<Organization> updateMembership(final Account actor,
                                                   final OrganizationMembership membership) {

        this.em.merge(membership);

        LOGGER.info("Update membership for member {}", membership.getMember().getScreenName());

        return Optional.ofNullable(membership.getOrganization());
    }


    @Override
    public Optional<OrganizationMembership> findOrganizationMembership(final Account actor, final Organization org) {
        final Account localActor = this.em.find(Account.class, actor.getId());
        final Optional<OrganizationMembership> o = localActor.getOrganizationMemberships()
                .stream()
                .filter(om -> om.getOrganization().getId() == org.getId()).findFirst();

        if (o.isPresent()) {
            this.em.detach(o.get());
        }
        return o;
    }


    @Override
    public DefaultTask updateDefaultTask(final Account actor, final DefaultTask task) {
        final Optional<Organization> org = this.getOrganizationByID(actor, task.getOrganizationID());
        if (org.isPresent()) {
            em.merge(task);
            em.flush();
        }
        return task;
    }

    @Override
    public boolean checkOrganizationVacationTask(final String taskName) {
        boolean b = true;
        final TypedQuery<Organization> q = em
                .createQuery("select distinct o from Organization o", Organization.class);
        final List<Organization> orgs = q.getResultList();

        for (final Organization o : orgs) {
            final Optional vacationTask = o.getDefaultTasks().stream().filter(t -> t.getName().matches(defaultVacationTaskName)).findFirst();
            if (!vacationTask.isPresent()) {
                try {
                    this.createDefaultTask(o, defaultVacationTaskName);
                } catch (final Exception e) {
                    b = false;
                }
            }
        }

        return b;
    }


    @Override
    public List<DefaultTask> listDefaultTasks(final Organization org, final Date ds, final Date de) {
        final TypedQuery<DefaultTask> q = em
                .createQuery("select distinct t " +
                        " from DefaultTask t left join fetch t.imputations where "
                        + " t.organizationID = :orgID", DefaultTask.class);
        q.setParameter("orgID", org.getId());

        return q.getResultList();

    }

    @Override
    public DefaultTask createDefaultTask(final Account actor, final Organization orgID, final String name) throws BusinessException {
        return this.createDefaultTask(orgID, name);
    }


    private DefaultTask createDefaultTask(final Organization org, final String name) throws BusinessException {
        try {
            final DefaultTask task = new DefaultTask();
            task.setStartDate(org.getCreatedDate().getTime());
            task.setEndDate(null);
            task.setOrigin(org.getName() + "/" + System.nanoTime());
            task.setName(name);
            task.setOrganizationID(org.getId());
            task.setOrganization(org);
            em.persist(task);
            em.flush();
            LOGGER.info("Default task " + task.getName() + " is created.");
            return task;
        } catch (final Exception e) {
            throw new BusinessException(e);
        }
    }


    @Override
    public DefaultTask getDefaultTaskByID(final Account account, final long id) {
        final DefaultTask task = em.find(DefaultTask.class, id);
        return task;
    }

    @Override
    public void disableDefaultTaskByID(final Account actor, final Organization orgID, final long taskID) throws BusinessException {

        final DefaultTask task = em.find(DefaultTask.class, taskID);
        task.setEndDate(new Date());

        em.merge(task);
        em.flush();

        LOGGER.info("Default Task " + taskID + " deleted by " + actor.getName());
    }

    @Override
    public List<TaskType> listTaskType(final Organization orgID) {
        final TypedQuery<TaskType> q = em.createQuery("select tt from TaskType tt " +
                "where tt.enable = true and tt.organizationID = :orgID", TaskType.class);
        q.setParameter("orgID", orgID.getId());
        return q.getResultList();
    }

    @Override
    public TaskType findTaskTypeByID(final Long taskTypeID) {
        if (taskTypeID == null) {
            return null;
        }
        return em.find(TaskType.class, taskTypeID);
    }

    @Override
    public TaskType updateTaskType(final Account actor, final TaskType type) {
        final Optional<Organization> organization = this.getOrganizationByID(actor, type.getOrganizationID());
        if (organization.isPresent()) {
            em.merge(type);
            em.flush();
        }
        return type;
    }

    @Override
    public TaskType createTaskType(final Account actor, final Organization orgID, final String name) {
        final TaskType taskType = new TaskType();
        taskType.setTypeName(name);
        taskType.setOrganizationID(orgID.getId());
        em.persist(taskType);
        em.flush();
        LOGGER.info("User " + actor.getScreenName() + " create task type " + name);

        return taskType;
    }


    @Override
    public void disableTaskType(final Account actor, final TaskType type) {

        type.setEnable(false);

        em.merge(type);
        em.flush();
        LOGGER.info("User " + actor.getScreenName() + " disable task type " + type.getTypeName());

    }


}
