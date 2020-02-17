package timeboard.core.api;

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

import timeboard.core.api.exceptions.BusinessException;
import timeboard.core.model.*;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service for organizations management.
 */
public interface OrganizationService {


    Organization createOrganization(
            final Account actor,
            final String organizationName,
            final Map<String, String> properties) throws BusinessException;

    Optional<Organization> getOrganizationByID(
            final Account actor,
            final long id);

    Optional<Organization> getOrganizationByName(
            final String organisationName);

    Optional<Organization> updateOrganization(
            final Account actor,
            final Organization organization);

    Optional<Organization> removeMembership(
            final Account actor,
            final Organization organization,
            final Account member) throws BusinessException;

    Optional<Organization> addMembership(
            final Account actor,
            final Organization organization,
            final Account member,
            final MembershipRole role) throws BusinessException;

    Optional<Organization> updateMembership(
            final Account actor,
            final OrganizationMembership membership) throws BusinessException;

    Optional<OrganizationMembership> findOrganizationMembership(
            final Account actor,
            final Organization organization) throws BusinessException;


    /*
    == Default Tasks ==
    */
    List<DefaultTask> listDefaultTasks(
            final Organization org,
            final Date ds,
            final Date de);

    /**
     * Create a default task.
     *
     * @return DefaultTask
     */
    DefaultTask createDefaultTask(
            final Account actor,
            final Organization org,
            final String task) throws BusinessException;


    /**
     * default tasks can not be deleted, so they are set disabled and hidden from UI
     *
     * @param actor
     * @param taskID
     * @throws BusinessException
     */
    void disableDefaultTaskByID(
            final Account actor,
            final Organization org, final
            long taskID) throws BusinessException;


    DefaultTask updateDefaultTask(
            final Account actor,
            final DefaultTask task);

    DefaultTask getDefaultTaskByID(
            final Account actor,
            final long id);


    /**
     * Return task types.
     *
     * @return List all task types.
     */
    List<TaskType> listTaskType(
            final Organization org);

    TaskType findTaskTypeByID(
            final Long taskTypeID);

    TaskType updateTaskType(
            final Account actor,
            final TaskType type);

    TaskType createTaskType(
            final Account actor,
            final Organization org,
            final String name);

    void disableTaskType(
            final Account actor,
            final TaskType type);

    boolean checkOrganizationVacationTask(
            final String taskName);

}
