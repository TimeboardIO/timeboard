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
import org.springframework.stereotype.Component;
import timeboard.core.api.OrganizationService;
import timeboard.core.api.ProjectService;
import timeboard.core.api.TimeboardSubjects;
import timeboard.core.api.TimesheetService;
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


    private static long absoluteWeekNumber(int year, int week) {
        return (long) (year * 53) + week;
    }


    @Override
    public SubmittedTimesheet submitTimesheet(
            final Long orgID,
            final Account actor,
            final Account accountTimesheet,
            final Organization currentOrg,
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
        previousWeek.setFirstDayOfWeek(Calendar.MONDAY);
        previousWeek.roll(Calendar.WEEK_OF_YEAR, -1); // remove 1 week

        final Optional<ValidationStatus> lastWeekValidatedOpt = this.getTimesheetValidationStatus(
                currentOrg.getId(), accountTimesheet, previousWeek.get(Calendar.YEAR),
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

        final SubmittedTimesheet submittedTimesheet = new SubmittedTimesheet();
        submittedTimesheet.setAccount(accountTimesheet);
        submittedTimesheet.setYear(year);
        submittedTimesheet.setWeek(week);
        submittedTimesheet.setTimesheetStatus(ValidationStatus.PENDING_VALIDATION);

        em.persist(submittedTimesheet);

        TimeboardSubjects.TIMESHEET_EVENTS.onNext(new TimesheetEvent(submittedTimesheet, projectService, currentOrg));

        LOGGER.info("Timesheet for " + week + " submit for user"
                + accountTimesheet.getScreenName() + " by user " + actor.getScreenName());

        return submittedTimesheet;

    }


    @Override
    public Optional<ValidationStatus> getTimesheetValidationStatus(
            final Long orgID,
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
            q.setParameter("orgID", orgID);

            validationStatus = q.getSingleResult();

        } catch (Exception e) {
            LOGGER.debug(e.getMessage(), e);
        }

        return Optional.ofNullable(validationStatus);
    }

    @Override
    public Map<Integer, Double> getAllImputationsForAccountOnDateRange(
            final Long orgID,
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
        q.setParameter("orgID", orgID);

        for (Map.Entry parameter : parameters.entrySet()) {
            q.setParameter((String) parameter.getKey(), parameter.getValue());
        }
        final List<Object[]> dayImputations = q.getResultList();

        final Map<Integer, Double> result = new HashMap<>();
        for (final Object[] o : dayImputations) {
            if(o[0] instanceof BigInteger){
                result.put(((BigInteger) o[0]).intValue(), (double) o[1]);
            }
            if(o[0] instanceof Integer){
                result.put((Integer) o[0], (double) o[1]);
            }
        }

        return result;
    }

    @Override
    public Map<Account, List<SubmittedTimesheet>> getProjectTimesheetByAccounts(Long orgID, Account actor, Project project) {

        final TypedQuery<SubmittedTimesheet> q = em.createQuery("select st from SubmittedTimesheet st JOIN st.account a "
                + "where st.account in :users and st.organizationID = :orgID", SubmittedTimesheet.class);

        q.setParameter("orgID", orgID);
        q.setParameter("users", project.getMembers().stream().map(ProjectMembership::getMember).collect(Collectors.toList()));

        final List<SubmittedTimesheet> resultList = q.getResultList();
        return resultList.stream()
                .collect(
                        Collectors.groupingBy(
                                SubmittedTimesheet::getAccount,
                                Collectors.mapping(r -> r, Collectors.toList())
                        ));

    }

    @Override
    public void forceValidateTimesheets(Long organizationID, Account actor, Account target, int selectedYear, int selectedWeek, int olderYear, int olderWeek) {

        Organization currentOrg = this.organizationService.getOrganizationByID(actor, organizationID).orElse(null);
        long selectedAbsoluteWeekNumber = absoluteWeekNumber(selectedYear, selectedWeek);

        final Calendar current = Calendar.getInstance();
        current.set(Calendar.WEEK_OF_YEAR, olderYear);
        current.set(Calendar.YEAR, olderWeek);

        while (absoluteWeekNumber(current.get(Calendar.YEAR), current.get(Calendar.WEEK_OF_YEAR)) <= selectedAbsoluteWeekNumber) {

            final int currentWeek = current.get(Calendar.WEEK_OF_YEAR);
            final int currentYear = current.get(Calendar.YEAR);

            final Optional<SubmittedTimesheet> submittedTimesheet =
                    this.getSubmittedTimesheet(organizationID, actor, target, currentYear, currentWeek);

            try {

                if (submittedTimesheet.isPresent()) {

                    this.validateTimesheet(actor, submittedTimesheet.get());

                } else {

                    // Timesheet Submission
                    final SubmittedTimesheet newSubmittedTimesheet = new SubmittedTimesheet();
                    newSubmittedTimesheet.setAccount(target);
                    newSubmittedTimesheet.setYear(currentYear);
                    newSubmittedTimesheet.setWeek(currentWeek);
                    newSubmittedTimesheet.setTimesheetStatus(ValidationStatus.PENDING_VALIDATION);
                    em.persist(newSubmittedTimesheet);

                    TimeboardSubjects.TIMESHEET_EVENTS.onNext(new TimesheetEvent(newSubmittedTimesheet, projectService, currentOrg));
                    LOGGER.info("Timesheet for " + currentWeek + " submit for user" + target.getScreenName() + " by user " + actor.getScreenName());


                    // Timesheet Validation
                    this.validateTimesheet(actor, newSubmittedTimesheet);

                }

            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }

            current.add(Calendar.WEEK_OF_YEAR, 1);
        }
    }

}
