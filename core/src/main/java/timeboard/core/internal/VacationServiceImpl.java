package timeboard.core.internal;

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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import timeboard.core.api.VacationService;
import timeboard.core.model.*;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Component
@Transactional
public class VacationServiceImpl implements VacationService {

    @Autowired
    private EntityManager em;

    @Override
    public Optional<VacationRequest> getVacationRequestByID(Account actor, Long requestID) {
        VacationRequest data;
        try {
            data = em.find(VacationRequest.class, requestID);
        } catch (Exception nre) {
            data = null;
        }
        return Optional.ofNullable(data);

    }

    @Override
    public VacationRequest createVacationRequest(Account actor, VacationRequest request) {

        em.persist(request);
        em.flush();

        return request;
    }

    @Override
    public List<VacationRequest> listUserVacations(Account applicant) {

        TypedQuery<VacationRequest> q = em.createQuery("select v from VacationRequest v where v.applicant = :applicant and v.status = :status", VacationRequest.class);
        q.setParameter("applicant", applicant);
        q.setParameter("status", VacationRequestStatus.PENDING);

        return q.getResultList();
    }

    @Override
    public List<VacationRequest> listVacationsToValidateByUser(Account assignee) {

        TypedQuery<VacationRequest> q = em.createQuery("select v from VacationRequest v where v.assignee = :assignee", VacationRequest.class);
        q.setParameter("assignee", assignee);

        return q.getResultList();
    }

    @Override
    public void deleteVacationRequest(Account actor, VacationRequest request) {
        em.remove(request);
        em.flush();
    }

    @Override
    public VacationRequest approveVacationRequest(Account actor, VacationRequest request) {
        request.setStatus(VacationRequestStatus.ACCEPTED);
        em.merge(request);
        em.flush();
        return request;
    }

    @Override
    public VacationRequest rejectVacationRequest(Account actor, VacationRequest request) {
        request.setStatus(VacationRequestStatus.REJECTED);
        em.merge(request);
        em.flush();
        return request;
    }


}
