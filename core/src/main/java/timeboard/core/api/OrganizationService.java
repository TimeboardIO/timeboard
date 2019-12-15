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
import timeboard.core.model.Account;
import timeboard.core.model.AccountHierarchy;
import timeboard.core.model.MembershipRole;

import java.util.List;
import java.util.Optional;

/**
 * Service for organizations management.
 */
public interface OrganizationService {

    Account createOrganization(final Account actor, final Account organization) throws BusinessException;

    Optional<Account> getOrganizationByID(final Account actor, long id);

    Account updateOrganization(final Account actor, Account organization);

    List<Account> getParents(final Account actor, Account organization);

    List<Account> getMembers(final Account actor, Account organization);

    AccountHierarchy removeMember(Account actor, Account organization, Account member) throws BusinessException;

    AccountHierarchy addMember(Account actor, Account organization, Account member) throws BusinessException;

    AccountHierarchy updateMemberRole(Account actor, Account organization, Account member, MembershipRole role) throws BusinessException;
}
