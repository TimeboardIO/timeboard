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

import java.util.List;
import timeboard.core.api.exceptions.BusinessException;
import timeboard.core.api.exceptions.UserException;
import timeboard.core.model.TaskColumns;
import timeboard.core.model.User;

/**
 * Service for users and accounts management.
 */
public interface UserService {
     /**
     * Search user by remote subject.
     *
     * @param remoteSubject prefix used to search user
     * @return list of users
     */
     User findUserBySubject(String remoteSubject);


    /**
     * Search user where name start with prefix.
     *
     * @param prefix prefix used to search user
     * @return list of users
     */
    List<User> searchUserByEmail(String prefix);

    /**
     * Search user where name start with prefix, limit to project with
     * primary Key projectID.
     *
     * @param prefix    prefix used to search user
     * @param projectID project primary key
     * @return list of users
     */
    List<User> searchUserByEmail(String prefix, Long projectID);


    /**
     * Find user by primary key.
     *
     * @param userID user primary key
     * @return user instance or null if not exist
     */
    User findUserByID(Long userID);

    /**
     * Find user by email.
     *
     * @param email user email
     * @return user instance or null if not exist
     */
    User findUserByEmail(String email);

    /**
     * Update user.
     *
     * @param user to update
     * @return user will with primary key
     * @throws BusinessException user does not exist
     */
    User updateUser(User user) throws UserException;

    User createUser(final User user) throws BusinessException;

    User findUserByExternalID(String origin, String userExternalID);

    User userProvisionning(String sub, String email) throws BusinessException;

    List<User> createUsers(List<User> usersList);
}
