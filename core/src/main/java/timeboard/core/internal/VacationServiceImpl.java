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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import timeboard.core.api.OrganizationService;
import timeboard.core.api.ProjectService;
import timeboard.core.api.TimeboardSubjects;
import timeboard.core.api.VacationService;
import timeboard.core.api.exceptions.BusinessException;
import timeboard.core.internal.events.TimeboardEventType;
import timeboard.core.internal.events.VacationEvent;
import timeboard.core.model.*;

import javax.persistence.EntityManager;
import javax.persistence.TemporalType;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
import java.util.*;
import java.util.Calendar;
import java.util.stream.Collectors;

@Component
@Transactional
public class VacationServiceImpl implements VacationService {

    @Autowired
    private EntityManager em;


    @Value("${timeboard.tasks.default.vacation}")
    private String defaultVacationTaskName;

    @Autowired
    private OrganizationService organizationService;

    @Autowired
    private ProjectService projectservice;

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
        request.setStartDate(new Date(request.getStartDate().getTime()+(2 * 60 * 60 * 1000) +1));
        request.setEndDate(new Date(request.getEndDate().getTime()+(2 * 60 * 60 * 1000) +1));

        em.persist(request);
        em.flush();

        return request;
    }


    @Override
    public RecursiveVacationRequest createRecursiveVacationRequest(Account actor, RecursiveVacationRequest request) {
        request.setStartDate(new Date(request.getStartDate().getTime()+(2 * 60 * 60 * 1000) +1));
        request.setEndDate(new Date(request.getEndDate().getTime()+(2 * 60 * 60 * 1000) +1));

        Calendar start = Calendar.getInstance();
        start.setTime(request.getStartDate());

        Calendar end = Calendar.getInstance();
        end.setTime(request.getEndDate());

        while (start.before(end)) {
            start.set(Calendar.DAY_OF_WEEK, (request.getRecurrenceDay() + 1)%7 ); //Calendar first day of week is sunday
            if (start.getTime().after(request.getStartDate()) && start.getTime().before(request.getEndDate())) {
                VacationRequest child = new VacationRequest(request);
                child.setParent(request);
                child.setStartDate(start.getTime());
                child.setEndDate(start.getTime());
                request.getChildren().add(child);
            }
            start.add(Calendar.WEEK_OF_YEAR, 1);
        }

        em.persist(request);
        em.flush();

        return request;
    }

    @Override
    public List<VacationRequest> listVacationRequestsByUser(Account applicant) {

        TypedQuery<VacationRequest> q = em.createQuery(
                "select v from VacationRequest v where v.applicant = :applicant and v.parent IS NULL"
                , VacationRequest.class);
        q.setParameter("applicant", applicant);

        return q.getResultList();

    }

    @Override
    public List<VacationRequest> listVacationRequestsToValidateByUser(Account assignee) {

        TypedQuery<VacationRequest> q = em.createQuery("select v from VacationRequest v " +
                        "where v.assignee = :assignee and v.status = :status and v.parent IS NULL",
                VacationRequest.class);
        q.setParameter("assignee", assignee);
        q.setParameter("status", VacationRequestStatus.PENDING);

        return q.getResultList();
    }

    @Override
    public Map<Account, List<VacationRequest>> listProjectMembersVacationRequests(Account actor, Project project, int month, int year) {

        // include next and previous to enhance month loading

        Calendar c = Calendar.getInstance();
        c.set(Calendar.MONTH, month+1);
        c.set(Calendar.YEAR, year);
        c.set(Calendar.DAY_OF_MONTH, 1);
        c.add(Calendar.MONTH, -2);

        Date startDate = c.getTime();
        c.set(Calendar.DAY_OF_MONTH, 31);
        c.add(Calendar.MONTH, 2);
        Date endDate = c.getTime();

        TypedQuery<VacationRequest> q = em.createQuery(
                "select v from VacationRequest v where v.applicant IN :applicants " +
                        "AND (" +
                            "   (v.startDate BETWEEN :start AND :end) " +
                            "OR (v.endDate   BETWEEN :start AND :end) " +
                            "OR ( (:start BETWEEN v.startDate AND v.endDate) AND (:end BETWEEN v.startDate AND v.endDate) )" +
                        ")"
                , VacationRequest.class);

        q.setParameter("start", startDate);
        q.setParameter("end", endDate);

        // push project members list to request
        q.setParameter("applicants", project.getMembers().stream().map(ProjectMembership::getMember).collect(Collectors.toList()));

        // group vacation request by account and return map account -> requests list
        return q.getResultList().stream().filter(r -> !(r instanceof RecursiveVacationRequest))
                .collect(Collectors.groupingBy(VacationRequest::getApplicant,
                Collectors.mapping(r -> r,Collectors.toList())));
    }

    @Override
    public List<VacationRequest> listVacationRequestsByPeriod(Account applicant, VacationRequest request) {

        TypedQuery<VacationRequest> q = em.createQuery("select v from VacationRequest v " +
                        "where v.applicant = :applicant " +
                        "and ( " +
                            "(v.startDate >= :startDate and :startDate <= v.endDate)" +
                            " or " +
                            "(v.startDate >= :endDate and :endDate <= v.endDate)" +
                        ")",
                VacationRequest.class);

        q.setParameter("applicant", applicant);
        q.setParameter("startDate", request.getStartDate(), TemporalType.DATE);
        q.setParameter("endDate", request.getEndDate(), TemporalType.DATE);

        List<VacationRequest> resultList = q.getResultList();
        List<VacationRequest> copyList = new ArrayList<>(resultList);

        for (VacationRequest r :resultList) {
            if (
                (r.getStartDate().compareTo(request.getEndDate()) == 1)
                    && request.getStartHalfDay() == VacationRequest.HalfDay.AFTERNOON
                    &&  r.getStartHalfDay() == VacationRequest.HalfDay.MORNING
            ) {
                    copyList.remove(r);
            }

            if (
                (r.getEndDate().compareTo(request.getStartDate()) == 1)
                && request.getEndHalfDay() == VacationRequest.HalfDay.AFTERNOON
                && r.getEndHalfDay() == VacationRequest.HalfDay.MORNING
            ) {
                    copyList.remove(r);
            }
        }

        return copyList;
    }


    @Override
    public void deleteVacationRequest(Account actor, VacationRequest request) throws BusinessException {

        if(request.getStatus() == VacationRequestStatus.ACCEPTED) {
            this.updateImputations(actor, request,0);
        }

        em.remove(request);
        em.flush();

        TimeboardSubjects.VACATION_EVENTS.onNext(new VacationEvent(TimeboardEventType.DELETE, request));

    }

    @Override
    public void deleteVacationRequest(Account actor, RecursiveVacationRequest request) throws BusinessException {


        for(VacationRequest r : request.getChildren()) {
            this.deleteVacationRequest(actor, r);
        }

        if(request.getStatus() == VacationRequestStatus.ACCEPTED) {
            this.updateImputations(actor, request,0);
        }

        em.remove(request);
        em.flush();


        TimeboardSubjects.VACATION_EVENTS.onNext(new VacationEvent(TimeboardEventType.DELETE, request));

    }

    @Override
    public VacationRequest approveVacationRequest(Account actor, VacationRequest request) throws BusinessException {
        request.setStatus(VacationRequestStatus.ACCEPTED);
        em.merge(request);
        em.flush();

        this.updateImputations(actor, request,1);
        TimeboardSubjects.VACATION_EVENTS.onNext(new VacationEvent(TimeboardEventType.APPROVE, request));

        return request;
    }

    @Override
    public RecursiveVacationRequest approveVacationRequest(Account actor, RecursiveVacationRequest request) throws BusinessException {
        request.setStatus(VacationRequestStatus.ACCEPTED);

        for(VacationRequest r : request.getChildren()) {
            this.approveVacationRequest(actor, r);
        }

        em.merge(request);
        em.flush();

        this.updateImputations(actor, request,1);
        TimeboardSubjects.VACATION_EVENTS.onNext(new VacationEvent(TimeboardEventType.APPROVE, request));

        return request;
    }

    private void updateImputations(Account actor, VacationRequest request, double sign ) throws BusinessException {

        DefaultTask vacationTask = this.getVacationTask(actor, request);

        java.util.Calendar c1 = java.util.Calendar.getInstance();
        c1.setTime(request.getStartDate());
        c1.set(Calendar.HOUR_OF_DAY, 2);
        c1.set(Calendar.MINUTE, 0);
        c1.set(Calendar.SECOND, 0);
        c1.set(Calendar.MILLISECOND, 0);
        c1.setFirstDayOfWeek(Calendar.MONDAY);

        double value = 1 * sign;
        java.util.Calendar c2 = java.util.Calendar.getInstance();
        c2.setTime(request.getEndDate());

        if(request.getStartHalfDay().equals(VacationRequest.HalfDay.AFTERNOON)) {
            value = 0.5 * sign;
        }

        while (c1.before(c2)) {
            if (c1.get(Calendar.DAY_OF_WEEK) <=6  && c1.get(Calendar.DAY_OF_WEEK) > 1) {
                this.updateTaskImputation(request.getApplicant(), vacationTask, c1.getTime(), value);
            }
            value = 1 * sign;
            c1.add(Calendar.DATE, 1);
        }
        if(request.getEndHalfDay().equals(VacationRequest.HalfDay.MORNING)) {
            value = 0.5 * sign;
        }
        if (c1.get(Calendar.DAY_OF_WEEK)  <=6  && c1.get(Calendar.DAY_OF_WEEK) > 1 ) {
            this.updateTaskImputation(request.getApplicant(), vacationTask, c1.getTime(), value);
        }

    }

    private DefaultTask getVacationTask(Account actor, VacationRequest request) {
        Optional<Organization> organization = this.organizationService.getOrganizationByID(actor, request.getOrganizationID());

        if(organization.isPresent()) {
            Optional<DefaultTask> vacationTask = organization.get().getDefaultTasks().stream()
                    .filter(t -> t.getName().matches(this.defaultVacationTaskName)).findFirst();

            if(vacationTask.isPresent()) {
                return vacationTask.get();
            }
        }

        return null;
    }

    private void updateTaskImputation(Account user, DefaultTask task, Date day, double val) throws BusinessException {
      this.projectservice.updateTaskImputation(user, task, day, val);

    }


    @Override
    public VacationRequest rejectVacationRequest(Account actor, VacationRequest request) {
        request.setStatus(VacationRequestStatus.REJECTED);
        em.merge(request);
        em.flush();

        TimeboardSubjects.VACATION_EVENTS.onNext(new VacationEvent(TimeboardEventType.DENY, request));

        return request;
    }

    @Override
    public RecursiveVacationRequest rejectVacationRequest(Account actor, RecursiveVacationRequest request) {

        for(VacationRequest r : request.getChildren()) {
            this.rejectVacationRequest(actor, r);
        }

        request.setStatus(VacationRequestStatus.REJECTED);
        em.merge(request);
        em.flush();

        TimeboardSubjects.VACATION_EVENTS.onNext(new VacationEvent(TimeboardEventType.DENY, request));

        return request;
    }



}
