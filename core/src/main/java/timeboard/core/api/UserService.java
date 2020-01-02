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

import org.springframework.security.core.userdetails.UserDetailsService;
import timeboard.core.api.exceptions.BusinessException;
import timeboard.core.api.exceptions.UserException;
import timeboard.core.model.Account;
import timeboard.core.model.Project;

import java.util.List;

/**
 * Service for users and accounts management.
 */
public interface UserService extends UserDetailsService {
    /**
     * Search user by remote subject.
     *
     * @param remoteSubject prefix used to search user
     * @return list of users
     */
    Account findUserBySubject(String remoteSubject);


    /**
     * Search user where name start with prefix.
     *
     * @param prefix prefix used to search user
     * @return list of users
     */
    List<Account> searchUserByEmail(final Account actor, String prefix) throws BusinessException;


    /**
     * Search user where name start with prefix, limit to project with
     * primary Key projectID.
     *
     * @param prefix    prefix used to search user
     * @param project project
     * @return list of users
     */
    List<Account> searchUserByEmail(final Account actor, String prefix, Project project) throws BusinessException;

    /**
     * Search user where name start with prefix, limit to project with
     * primary Key projectID.
     *
     * @param prefix    prefix used to search user
     * @param organization organization
     * @return list of users
     */
    List<Account> searchUserByEmail(final Account actor, String prefix, Account organization) throws BusinessException;


    /**
     * Find user by primary key.
     *
     * @param userID user primary key
     * @return user instance or null if not exist
     */
    Account findUserByID(Long userID);

    /**
     * Find user by email.
     *
     * @param email user email
     * @return user instance or null if not exist
     */
    Account findUserByEmail(String email);

    /**
     * Update user.
     *
     * @param account to update
     * @return user will with primary key
     * @throws BusinessException user does not exist
     */
    Account updateUser(Account account) throws UserException;

    Account createUser(final Account account) throws BusinessException;

    Account findUserByExternalID(String origin, String userExternalID);

    Account userProvisionning(String sub, String email) throws BusinessException;

    List<Account> createUsers(List<Account> usersList);

    Account findUserByLogin(String name);
}
