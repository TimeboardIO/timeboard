package timeboard.core.internal;

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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import timeboard.core.api.*;
import timeboard.core.api.events.TimesheetEvent;
import timeboard.core.api.exceptions.BusinessException;
import timeboard.core.api.exceptions.TimesheetException;
import timeboard.core.model.*;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
import java.math.BigInteger;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.*;
import java.util.stream.Collectors;


@Component
@Transactional
public class TimesheetServiceImpl implements TimesheetService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TimesheetServiceImpl.class);


    @Autowired
    private EntityManager em;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private OrganizationService organizationService;


    @Override
    @PreAuthorize("hasPermission(#accountTimesheet,'" + TIMESHEET_SUBMIT + "')")
    public SubmittedTimesheet submitTimesheet(
            final Organization currentOrg,
            final Account actor,
            final Account accountTimesheet,
            final int year,
            final int week)
            throws BusinessException {

        final Calendar beginWorkDate = this.organizationService
                .findOrganizationMembership(actor, currentOrg).get().getCreationDate();

        final int dayInFirstWeek = beginWorkDate.get(Calendar.DAY_OF_WEEK);
        final boolean firstWeek = beginWorkDate.get(Calendar.WEEK_OF_YEAR)
                == week && beginWorkDate.get(Calendar.YEAR) == year;

        final Calendar previousWeek = Calendar.getInstance();
        previousWeek.set(Calendar.WEEK_OF_YEAR, week);
        previousWeek.set(Calendar.YEAR, year);
        previousWeek.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
        previousWeek.setFirstDayOfWeek(Calendar.MONDAY);
        previousWeek.add(Calendar.WEEK_OF_YEAR, -1); // remove 1 week

        final Optional<ValidationStatus> lastWeekValidatedOpt = this.getTimesheetValidationStatus(
                currentOrg, accountTimesheet, previousWeek.get(Calendar.YEAR),
                previousWeek.get(Calendar.WEEK_OF_YEAR));

        if (!firstWeek && lastWeekValidatedOpt.isEmpty()) {
            throw new TimesheetException("Can not submit this week, previous week is not submitted");
        }

        final Calendar firstDay = Calendar.getInstance();
        firstDay.set(Calendar.WEEK_OF_YEAR, week);
        firstDay.set(Calendar.YEAR, year);
        firstDay.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        firstDay.set(Calendar.HOUR_OF_DAY, 0);
        firstDay.set(Calendar.MINUTE, 0);
        firstDay.set(Calendar.SECOND, 0);
        firstDay.set(Calendar.MILLISECOND, 0);

        final Calendar lastDay = (Calendar) firstDay.clone();
        lastDay.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY);

        if (firstWeek) {
            firstDay.set(Calendar.DAY_OF_WEEK, dayInFirstWeek);
            firstDay.setFirstDayOfWeek(dayInFirstWeek);
        }

        final Calendar currentDay = (Calendar) firstDay.clone();

        final long nbDays = ChronoUnit.DAYS.between(firstDay.toInstant(), lastDay.toInstant());
        final Double expectedSum = nbDays + 1.0d;

        final List<Date> days = new ArrayList<>();

        for (int i = 0; i <= nbDays; i++) {
            days.add(currentDay.getTime());
            currentDay.roll(Calendar.DAY_OF_WEEK, 1);
        }

        final TypedQuery<Double> q = em.createNamedQuery(
                Imputation.SUM_IMPUTATIONS_BY_USER_AND_WEEK, Double.class);

        q.setParameter("user", accountTimesheet);
        q.setParameter("days", days);
        q.setParameter("orgID", currentOrg.getId());
        final Double result = q.getSingleResult();

        if (!result.equals(expectedSum)) {
            throw new TimesheetException("Can not submit this week, all daily imputations totals are not equals to 1");
        }

       return processSubmission(actor, accountTimesheet, year, week, currentOrg );
    }

    private SubmittedTimesheet processSubmission(Account actor, Account accountTimesheet, int year, int week, Organization currentOrg) {

        final Optional<SubmittedTimesheet> existingRejectedTimesheet = this.getSubmittedTimesheet(
        TimeboardSubjects.TIMESHEET_EVENTS.onNext(new TimesheetEvent(submittedTimesheet, projectService, currentOrg));

        LOGGER.info("Timesheet for " + week + " submit for user "
                + accountTimesheet.getScreenName() + " by user " + actor.getScreenName());
        return submittedTimesheet;

    }


    @Override
    @PreAuthorize("hasPermission(#submittedTimesheet,'" + TIMESHEET_VALIDATE + "')")
    public SubmittedTimesheet validateTimesheet(final Account actor,
                                                final SubmittedTimesheet submittedTimesheet) throws BusinessException {

        if (submittedTimesheet.getTimesheetStatus().equals(ValidationStatus.VALIDATED)) {
            //Do nothing
            return submittedTimesheet;
        }


        if (!submittedTimesheet.getTimesheetStatus().equals(ValidationStatus.PENDING_VALIDATION)) {
            throw new BusinessException("Can not validate unsubmitted weeks");
        }

        final Calendar previousWeek = Calendar.getInstance();
        previousWeek.set(Calendar.WEEK_OF_YEAR, submittedTimesheet.getWeek());
        previousWeek.set(Calendar.YEAR, submittedTimesheet.getYear());
        previousWeek.setFirstDayOfWeek(Calendar.MONDAY);
        previousWeek.add(Calendar.WEEK_OF_YEAR, -1); // remove 1 week

        final Optional<ValidationStatus> lastWeekValidatedOpt = this.getTimesheetValidationStatus(
                currentOrg.getId(), submittedTimesheet.getAccount(), previousWeek.get(Calendar.YEAR),
                previousWeek.get(Calendar.WEEK_OF_YEAR));

        if (lastWeekValidatedOpt.isEmpty() || !lastWeekValidatedOpt.get().equals(ValidationStatus.VALIDATED) ) {
            throw new TimesheetException("Can not validate this week, previous week is not validated");
        }

        submittedTimesheet.setTimesheetStatus(ValidationStatus.VALIDATED);

        em.merge(submittedTimesheet);

        final Optional<Organization> org = this.organizationService.getOrganizationByID(actor, submittedTimesheet.getOrganizationID());

        TimeboardSubjects.TIMESHEET_EVENTS.onNext(new TimesheetEvent(submittedTimesheet, projectService, org.get()));

        LOGGER.info("Timesheet for " + submittedTimesheet.getWeek() + " of " + submittedTimesheet.getYear() + " validated for user"
                + submittedTimesheet.getAccount().getScreenName() + " by user " + actor.getScreenName());


        return submittedTimesheet;

    }

    @Override
    @PreAuthorize("hasPermission(#submittedTimesheet,'" + TIMESHEET_REJECT + "')")
    public SubmittedTimesheet rejectTimesheet(final Account actor,
                                              final SubmittedTimesheet submittedTimesheet) throws BusinessException {

        if (!submittedTimesheet.getTimesheetStatus().equals(ValidationStatus.PENDING_VALIDATION)) {
            throw new BusinessException("Can not reject unsubmitted weeks");
        }


        final Calendar previousWeek = Calendar.getInstance();
        previousWeek.set(Calendar.WEEK_OF_YEAR, submittedTimesheet.getWeek());
        previousWeek.set(Calendar.YEAR, submittedTimesheet.getYear());
        previousWeek.setFirstDayOfWeek(Calendar.MONDAY);
        previousWeek.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
        previousWeek.add(Calendar.WEEK_OF_YEAR, -1); // remove 1 week

        final Optional<ValidationStatus> lastWeekValidatedOpt = this.getTimesheetValidationStatus(
                currentOrg.getId(), submittedTimesheet.getAccount(), previousWeek.get(Calendar.YEAR),
                previousWeek.get(Calendar.WEEK_OF_YEAR));

        if (lastWeekValidatedOpt.isEmpty() || !lastWeekValidatedOpt.get().equals(ValidationStatus.VALIDATED) ) {
            throw new TimesheetException("Can not validate this week, previous week is not validated");
        }


        submittedTimesheet.setTimesheetStatus(ValidationStatus.REJECTED);
        final Optional<Organization> org = this.organizationService.getOrganizationByID(actor, submittedTimesheet.getOrganizationID());

        em.merge(submittedTimesheet);

        TimeboardSubjects.TIMESHEET_EVENTS.onNext(new TimesheetEvent(submittedTimesheet, projectService, org.get()));

        LOGGER.info("Timesheet for " + submittedTimesheet.getWeek() + " of " + submittedTimesheet.getYear() + " rejected for user"
                + submittedTimesheet.getAccount().getScreenName() + " by user " + actor.getScreenName());


        return submittedTimesheet;

    }

    @Override
    @PreAuthorize("hasPermission(null,'" + TIMESHEET_IMPUTATION + "')")
    public List<UpdatedTaskResult> updateTaskImputations(final Organization org, final Account actor, final List<Imputation> imputationsList) {
        final List<UpdatedTaskResult> result = new ArrayList<>();
        for (final Imputation imputation : imputationsList) {
            UpdatedTaskResult updatedTaskResult = null;
            try {
                updatedTaskResult = this.updateTaskImputation(org, actor, (Task) imputation.getTask(), imputation.getDay(), imputation.getValue());
            } catch (final BusinessException e) {
                LOGGER.error(e.getMessage());
            }
            result.add(updatedTaskResult);
        }
        em.flush();
        return result;
    }

    @Override
    @PreAuthorize("hasPermission(null,'" + TIMESHEET_IMPUTATION + "')")
    public UpdatedTaskResult updateTaskImputation(
            final Organization org,
            final Account actor,
            final AbstractTask task,
            final Date day,
            final double val) throws BusinessException {
        final Calendar c = Calendar.getInstance();
        c.setTime(day);

        final ValidationStatus timesheetSubmitted = this.getTimesheetValidationStatus(
                org,
                actor, c.get(Calendar.YEAR), c.get(Calendar.WEEK_OF_YEAR)).orElse(null);

        if (task instanceof Task) {
            if (timesheetSubmitted != ValidationStatus.VALIDATED || timesheetSubmitted != ValidationStatus.PENDING_VALIDATION) {
                return this.updateProjectTaskImputation(actor, (Task) task, day, val, c);
            } else {
                final Task projectTask = (Task) task;
                return new UpdatedTaskResult(projectTask.getProject().getId(),
                        projectTask.getId(), projectTask.getEffortSpent(),
                        projectTask.getEffortLeft(), projectTask.getOriginalEstimate(),
                        projectTask.getRealEffort());
            }
        } else {
            if (timesheetSubmitted != ValidationStatus.VALIDATED || timesheetSubmitted != ValidationStatus.PENDING_VALIDATION) {
                return this.updateDefaultTaskImputation(actor, (DefaultTask) task, day, val, c);
            } else {
                return new UpdatedTaskResult(0, task.getId(), 0, 0, 0, 0);
            }
        }
    }


    @Override
    @PreAuthorize("hasPermission(null,'" + TIMESHEET_LIST + "')")
    public Optional<SubmittedTimesheet> getSubmittedTimesheet(Organization currentOrganization, Account actor, Account user, int year, int week) {

        final TypedQuery<SubmittedTimesheet> q = em.createQuery("select st from SubmittedTimesheet st "
                + "where st.account = :user and st.year = :year " +
                "and st.week = :week and st.organizationID = :orgID", SubmittedTimesheet.class);

        q.setParameter("week", week);
        q.setParameter("year", year);
        q.setParameter("user", user);
        q.setParameter("orgID", currentOrganization);

        try {
            return Optional.ofNullable(q.getSingleResult());
        }catch(Exception e){
            LOGGER.debug(e.getMessage(), e);
            return Optional.empty();
        }
    }

    @Override
    public Optional<ValidationStatus> getTimesheetValidationStatus(
            final Organization org,
            final Account currentAccount,
            final int year,
            final int week) {

        ValidationStatus validationStatus = null;

        try {

            final TypedQuery<ValidationStatus> q = em.createQuery("select st.timesheetStatus from SubmittedTimesheet st "
                    + "where st.account = :user and st.year = :year " +
                    "and st.week = :week and st.organizationID = :orgID", ValidationStatus.class);

            q.setParameter("week", week);
            q.setParameter("year", year);
            q.setParameter("user", currentAccount);
            q.setParameter("orgID", org.getId());

            validationStatus = q.getSingleResult();

        } catch (Exception e) {
            LOGGER.debug(e.getMessage(), e);
        }

        return Optional.ofNullable(validationStatus);
    }

    @Override
    public Map<Integer, Double> getAllImputationsForAccountOnDateRange(
            final Organization org,
            final Date startDate,
            final Date endDate,
            final Account account,
            final TimesheetService.TimesheetFilter[] filters) {

        final Map<String, Object> parameters = new HashMap<>();
        final StringBuilder sb = new StringBuilder();
        sb.append("SELECT DAY(day), COALESCE(SUM(i.value),0) ");
        sb.append("FROM Imputation i JOIN Task t ON i.task_id = t.id ");
        sb.append("WHERE  i.account_id = :user ");
        sb.append("AND i.organizationID = :orgID ");
        sb.append("AND i.day >= :startDate ");
        sb.append("AND i.day <= :endDate ");

        for (var filter : filters) {
            sb.append("AND ");
            if (filter.getTarget().getClass() == Project.class) {
                sb.append("t.project_id = :project");
                final Project project = (Project) filter.getTarget();
                parameters.put("project", project.getId());
            }
            if (AbstractTask.class.isAssignableFrom(filter.getTarget().getClass())) {
                sb.append("i.task_id = :task ");
                final AbstractTask task = (AbstractTask) filter.getTarget();
                parameters.put("task", task.getId());

            }
        }

        sb.append("GROUP BY i.day ");

        final TypedQuery<Object[]> q = (TypedQuery<Object[]>) em.createNativeQuery(sb.toString());
        q.setParameter("startDate", startDate);
        q.setParameter("endDate", endDate);
        q.setParameter("user", account.getId());
        q.setParameter("orgID", org.getId());

        for (Map.Entry parameter : parameters.entrySet()) {
            q.setParameter((String) parameter.getKey(), parameter.getValue());
        }
        final List<Object[]> dayImputations = q.getResultList();

        final Map<Integer, Double> result = new HashMap<>();
        for (final Object[] o : dayImputations) {
            if (o[0] instanceof BigInteger) {
                result.put(((BigInteger) o[0]).intValue(), (double) o[1]);
            }
            if (o[0] instanceof Integer) {
                result.put((Integer) o[0], (double) o[1]);
            }
        }

        return result;
    }

    @Override
    public Map<Account, List<SubmittedTimesheet>> getProjectTimesheetByAccounts(Organization org, Account actor, Project project) {

        final TypedQuery<SubmittedTimesheet> q = em.createQuery("select st from SubmittedTimesheet st JOIN st.account a "
                + "where st.account in :users and st.organizationID = :orgID", SubmittedTimesheet.class);

        q.setParameter("orgID", org.getId());
        q.setParameter("users", project.getMembers().stream().map(ProjectMembership::getMember).collect(Collectors.toList()));

        final List<SubmittedTimesheet> resultList = q.getResultList();
        return resultList.stream()
                .collect(
                        Collectors.groupingBy(
                                SubmittedTimesheet::getAccount,
                                Collectors.mapping(r -> r, Collectors.toList())
                        ));

    }

    private UpdatedTaskResult updateProjectTaskImputation(final Account actor,
                                                          final Task task,
                                                          final Date day,
                                                          final double val,
                                                          final Calendar calendar) throws BusinessException {

        final Task projectTask = (Task) this.projectService.getTaskByID(actor, task.getId());


        if (projectTask.getTaskStatus() != TaskStatus.PENDING) {
            final Imputation existingImputation = this.projectService.getImputationByDayByTask(em, calendar.getTime(), projectTask, actor);
            final double oldValue = existingImputation != null ? existingImputation.getValue() : 0;

            this.actionOnImputation(existingImputation, projectTask, actor, val, calendar.getTime());
            final Task updatedTask = em.find(Task.class, projectTask.getId());
            final double newEffortLeft = this.updateEffortLeftFromImputationValue(projectTask.getEffortLeft(), oldValue, val);
            updatedTask.setEffortLeft(newEffortLeft);

            LOGGER.info("User " + actor.getScreenName()
                    + " updated imputations for task "
                    + projectTask.getId() + " (" + day + ") in project "
                    + ((projectTask != null) ? projectTask.getProject().getName() : "default") + " with value " + val);

            em.merge(updatedTask);
            em.flush();

            return new UpdatedTaskResult(updatedTask.getProject().getId(),
                    updatedTask.getId(), updatedTask.getEffortSpent(),
                    updatedTask.getEffortLeft(), updatedTask.getOriginalEstimate(),
                    updatedTask.getRealEffort());
        }
        return null;
    }


    public UpdatedTaskResult updateDefaultTaskImputation(final Account actor,
                                                         final DefaultTask task,
                                                         final Date day, final double val, final Calendar calendar) throws BusinessException {

        final DefaultTask defaultTask = (DefaultTask) this.projectService.getTaskByID(actor, task.getId());

        // No matching imputations AND new value is correct (0.0 < val <= 1.0) AND task is available for imputations
        final Imputation existingImputation = this.projectService.getImputationByDayByTask(em, calendar.getTime(), defaultTask, actor);
        this.actionOnImputation(existingImputation, defaultTask, actor, val, calendar.getTime());

        em.flush();
        LOGGER.info("User " + actor.getScreenName() + " updated imputations for default task "
                + defaultTask.getId() + "(" + day + ") in project: default with value " + val);

        return new UpdatedTaskResult(0, defaultTask.getId(), 0, 0, 0, 0);

    }


    public void actionOnImputation(final Imputation imputation,
                                   final AbstractTask task,
                                   final Account actor,
                                   final double val,
                                   final Date date) {

        if (imputation == null) {
            //No imputation for current task and day
            final Imputation localImputation = new Imputation();
            localImputation.setDay(date);
            localImputation.setTask(task);
            localImputation.setAccount(actor);
            localImputation.setValue(val);
            em.persist(localImputation);
        } else {
            // There is an existing imputation for this day and task
            if (val == 0) {
                //if value equal to 0 then remove imputation
                final Long imputationID = imputation.getId();
                task.getImputations().removeIf(i -> i.getId() == imputationID);
                em.remove(imputation);
                em.merge(task);
            } else {
                imputation.setValue(val);
                // else save new value
                em.persist(imputation);
            }
        }
        em.flush();
    }

    private double updateEffortLeftFromImputationValue(
            final double currentEffortLeft,
            final double currentImputationValue,
            final double newImputationValue) {

        double newEL = currentEffortLeft; // new effort left
        final double diffValue = Math.abs(newImputationValue - currentImputationValue);

        if (currentImputationValue < newImputationValue) {
            newEL = currentEffortLeft - diffValue;
        }
        if (currentImputationValue > newImputationValue) {
            newEL = currentEffortLeft + diffValue;
        }

        return Math.max(newEL, 0);
    }


}
