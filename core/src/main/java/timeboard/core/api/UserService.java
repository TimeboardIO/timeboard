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


import timeboard.core.api.exceptions.UserException;
import timeboard.core.api.exceptions.WrongPasswordException;
import timeboard.core.model.User;
import timeboard.core.api.exceptions.BusinessException;

import java.util.List;


/**
 * Service for users and accounts management.
 */
public interface UserService {

    /**
     * Create new user.
     *
     * @param user to create
     * @return user will with primary key
     * @throws BusinessException user already exist
     */
    User createUser(User user) throws BusinessException;

    /**
     * Same as createUser but for a batch of users.
     *
     * @param users user list to create
     * @return user will with primary key
     * @throws BusinessException user already exist
     */
    List<User> createUsers(List<User> users) throws BusinessException;

    /**
     * Search user where name start with prefix.
     *
     * @param prefix prefix used to search user
     * @return list of users
     */
    List<User> searchUserByName(String prefix);

    /**
     * Search user where name start with prefix, limit to project with
     * primary Key projectID.
     *
     * @param prefix    prefix used to search user
     * @param projectID project primary key
     * @return list of users
     */
    List<User> searchUserByName(String prefix, Long projectID);

    /**
     * Method used to check if a user with username exist and if password match.
     *
     * @param username username to check
     * @param password password to chekc
     * @return user if username and password match with database entry,
     * else return null.
     */
    User autenticateUser(String username, String password);

    /**
     * Find user by login field.
     *
     * @param login login value
     * @return user instance or null if not exist
     */
    User findUserByLogin(String login);

    /**
     * Find user by primary key.
     *
     * @param userID user primary key
     * @return user instance or null if not exist
     */
    User findUserByID(Long userID);


    /**
     * Update user
     *
     * @param user to update
     * @return user will with primary key
     * @throws BusinessException user does not exist
     */
    User updateUser(User user);


    /**
     * Update user
     *
     * @param userID user primary key
     * @param oldPassword old password
     * @param newPassword new password
     * @throws WrongPasswordException old password is incorrect
     */
    void updateUserPassword(Long userID, String oldPassword, String newPassword) throws WrongPasswordException, UserException;

    User findUserByExternalID(String origin, String userExternalID);
}
