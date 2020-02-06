package timeboard.core.api;

/*-
 * #%L
 * core
 * %%
 * Copyright (C) 2019 - 2020 Timeboard
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
import timeboard.core.model.Project;
import timeboard.core.model.RecursiveVacationRequest;
import timeboard.core.model.VacationRequest;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface VacationService {

    String VACATION_LIST = "VACATION_LIST";
    String VACATION_TEAM_LIST = "VACATION_TEAM_LIST";
    String VACATION_VIEW = "VACATION_VIEW";

    Optional<VacationRequest> getVacationRequestByID(Account actor, Long requestID);

    VacationRequest createVacationRequest(Account actor, VacationRequest request);

    RecursiveVacationRequest createRecursiveVacationRequest(Account actor, RecursiveVacationRequest request);

    List<VacationRequest> listVacationRequestsByUser(Account user, long orgID);

    List<VacationRequest> listVacationRequestsByUser(Account user, long orgID, int year);

    List<VacationRequest> listVacationRequestsToValidateByUser(Account user, long orgID);

    Map<Account, List<VacationRequest>> listProjectMembersVacationRequests(Account actor, Project project, int month, int year);

    void deleteVacationRequest(Long orgID, Account actor, VacationRequest request) throws BusinessException;

    void deleteVacationRequest(Long orgID, Account actor, RecursiveVacationRequest request) throws BusinessException;

    VacationRequest approveVacationRequest(Long orgID, Account actor, VacationRequest request) throws BusinessException;

    RecursiveVacationRequest approveVacationRequest(Long orgID, Account actor, RecursiveVacationRequest request) throws BusinessException;

    VacationRequest rejectVacationRequest(Account actor, VacationRequest request);

    RecursiveVacationRequest rejectVacationRequest(Account actor, RecursiveVacationRequest request);
}
