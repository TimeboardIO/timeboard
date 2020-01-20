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


    Organization createOrganization(String organizationName, Map<String, String> properties);

    Organization createOrganization(Account actor,
                                    String organizationName, Map<String, String> properties) throws BusinessException;

    Optional<Organization> getOrganizationByID(final Account actor,
                                               long id);

    Optional<Organization> getOrganizationByName(String organisationName);

    Organization updateOrganization(final Account actor,
                                    Organization organization);

    Optional<Organization> removeMember(Account actor,
                                        Organization organization,
                                        Account member) throws BusinessException;

    Optional<Organization> addMember(Account actor,
                                     Organization organization,
                                     Account member,
                                     MembershipRole role) throws BusinessException;

    Optional<Organization> updateMemberRole(Account actor,
                                            Organization organization,
                                            Account member,
                                            MembershipRole role) throws BusinessException;

    /*
    == Default Tasks ==
    */
    List<DefaultTask> listDefaultTasks(Long orgID, Date ds, Date de);
    List<DefaultTask> listDefaultTasks(Long orgID);

    /**
     * Create a default task.
     *
     * @return DefaultTask
     */
    DefaultTask createDefaultTask(Account actor, Long orgID, String task) throws BusinessException;



    /**
     * default tasks can not be deleted, so they are set disabled and hidden from UI
     *
     * @param actor
     * @param taskID
     * @throws BusinessException
     */
    void disableDefaultTaskByID(Account actor, Long orgID, long taskID) throws BusinessException;


    DefaultTask updateDefaultTask(Account actor, DefaultTask task);

    DefaultTask getDefaultTaskByID(Account actor, long id);


    /**
     * Return task types.
     *
     * @return List all task types.
     */
    List<TaskType> listTaskType(Long orgID);

    TaskType findTaskTypeByID(Long taskTypeID);

    TaskType updateTaskType(Account actor, TaskType type);

    TaskType createTaskType(Account actor, Long orgID, String name);

    void disableTaskType(Account actor, TaskType type);

    //TODO remove when migration is ok
    boolean checkOrganizationVacationTask(String taskName) ;

}
