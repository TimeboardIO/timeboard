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
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import timeboard.core.api.*;
import timeboard.core.api.exceptions.BusinessException;
import timeboard.core.api.exceptions.TimesheetException;
import timeboard.core.api.events.TimesheetEvent;
import timeboard.core.model.*;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
import java.util.*;
import java.util.Calendar;


@Component
@Transactional
public class TimesheetServiceImpl implements TimesheetService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TimesheetServiceImpl.class);

    @Autowired
    private EntityManager em;

    @Autowired
    private UserService userService;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private OrganizationService organizationService;



    @Override
    public int findLastWeekYear(Calendar c, int week, int year) {
        c.set(Calendar.YEAR, year);
        c.set(Calendar.WEEK_OF_YEAR, week);
        c.roll(Calendar.WEEK_OF_YEAR, -1); // remove 1 week
        if(c.get(Calendar.WEEK_OF_YEAR) > week){
            c.roll(Calendar.YEAR, -1);  // remove one year
        }
        return c.get(Calendar.YEAR);
    }

    @Override
    public int findLastWeek(Calendar c, int week, int year) {
        c.set(Calendar.YEAR, year);
        c.set(Calendar.WEEK_OF_YEAR, week);
        c.roll(Calendar.WEEK_OF_YEAR, -1); // remove 1 week
        return c.get(Calendar.WEEK_OF_YEAR);
    }

    @Override
    public Date findStartDate(Calendar c, int week, int year) {
        c.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        return c.getTime();
    }

    @Override
    public Date findEndDate(Calendar c, int week, int year) {
        c.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        return c.getTime();
    }


    @Override
    @CacheEvict(value = "accountTimesheet", key = "#accountTimesheet.getId()+'-'+#year+'-'+#week")
    public SubmittedTimesheet submitTimesheet(Account actor, Account accountTimesheet, Organization currentOrg, int year, int week)
            throws BusinessException {

        //check if submission is possible
        //1 - last week is submitted

        final Calendar c = Calendar.getInstance();

        c.setTime(this.organizationService.findOrganizationMembership(actor, currentOrg).get().getCreationDate());

        int dayInFirstWeek = c.get(Calendar.DAY_OF_WEEK);
        boolean firstWeek = (c.get(Calendar.WEEK_OF_YEAR) == week) && (c.get(Calendar.YEAR) == year);

        c.set(Calendar.WEEK_OF_YEAR, week);
        c.set(Calendar.YEAR, year);
        c.setFirstDayOfWeek(Calendar.MONDAY);
        c.set(Calendar.HOUR_OF_DAY, 2);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);

        c.roll(Calendar.WEEK_OF_YEAR, -1); // remove 1 week

        boolean lastWeekSubmitted = this.isTimesheetSubmitted(
                accountTimesheet,
                c.get(Calendar.YEAR),
                c.get(Calendar.WEEK_OF_YEAR));

        if (!firstWeek && !lastWeekSubmitted) {
            throw new TimesheetException("Can not submit this week, previous week is not submitted");
        }

        //2 - all imputation day sum == 1
        c.set(Calendar.WEEK_OF_YEAR, week);
        c.set(Calendar.YEAR, year);

        Boolean result = true;
        c.set(Calendar.WEEK_OF_YEAR, week);
        c.set(Calendar.YEAR, year);
        c.setFirstDayOfWeek(Calendar.MONDAY);
        c.set(Calendar.DAY_OF_WEEK, 2);
        int firstDay =2;
        if(firstWeek){
            c.set(Calendar.DAY_OF_WEEK, dayInFirstWeek);
            c.setFirstDayOfWeek(dayInFirstWeek);
            firstDay = dayInFirstWeek;
        }

        boolean allDailyImputationTotalsAreOne = checkDailyImputationTotal(firstDay, accountTimesheet, c, result);
        if (!allDailyImputationTotalsAreOne) {
            throw new TimesheetException("Can not submit this week, all daily imputations totals are not equals to 1");
        }

        SubmittedTimesheet submittedTimesheet = new SubmittedTimesheet();
        submittedTimesheet.setAccount(accountTimesheet);
        submittedTimesheet.setYear(year);
        submittedTimesheet.setWeek(week);
        submittedTimesheet.setValidationStatus(this.getWeekValidationStatusAfterSubmission(actor, c, year, week));

        em.persist(submittedTimesheet);

        TimeboardSubjects.TIMESHEET_EVENTS.onNext(new TimesheetEvent(submittedTimesheet, projectService, currentOrg));
        LOGGER.info("Week " + week + " submit for user" + accountTimesheet.getName() + " by user " + actor.getName());

        return submittedTimesheet;

    }

    Boolean checkDailyImputationTotal(int firstDay, Account accountTimesheet, Calendar c, Boolean result){
        for ( int i = firstDay -1 ; i <= 5; i++) {

            TypedQuery<Double> q = em.createQuery("select COALESCE(sum(value),0) " +
                    "from Imputation i where i.account = :user and i.day = :day ", Double.class);

            q.setParameter("user", accountTimesheet);
            q.setParameter("day", c.getTime());
            final List<Double> resultList = q.getResultList();
            if (resultList != null){
                result &= (resultList.get(0) == 1.0);
            }
            c.roll(Calendar.DAY_OF_WEEK, 1);
        }
        return result;
    }

    ValidationStatus getWeekValidationStatusAfterSubmission(Account actor, Calendar c, int year, int week){
        final int previousWeek = this.findLastWeek(c, week, year);
        final int previousWeekYear = this.findLastWeekYear(c, week, year);
        ValidationStatus previousWeekValidationStatus = this.getTimesheetValidationStatus(actor, previousWeekYear, previousWeek);
        return previousWeekValidationStatus == ValidationStatus.VALIDATED ?
                ValidationStatus.PENDING_VALIDATION :  ValidationStatus.PENDING_PREVIOUS_VALIDATION;
    }

    @Override
    @Cacheable(value = "accountTimesheet", key = "#accountTimesheet.getId()+'-'+#year+'-'+#week")
    public boolean isTimesheetSubmitted(Account accountTimesheet, int year, int week) {
        TypedQuery<SubmittedTimesheet> q = em.createQuery("select st from SubmittedTimesheet st "
                + "where st.account = :user and st.year = :year and st.week = :week", SubmittedTimesheet.class);
        q.setParameter("week", week);
        q.setParameter("year", year);
        q.setParameter("user", accountTimesheet);

        try {
            q.getSingleResult();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    @Cacheable(value = "accountTimesheet", key = "#accountTimesheet.getId()+'-'+#year+'-'+#week")
    public boolean isTimesheetValidated(Account accountTimesheet, int year, int week) {
        TypedQuery<ValidationStatus> q = em.createQuery("select st.validationStatus from SubmittedTimesheet st "
                + "where st.account = :user and st.year = :year and st.week = :week", ValidationStatus.class);
        q.setParameter("week", week);
        q.setParameter("year", year);
        q.setParameter("user", accountTimesheet);

        try {
            return q.getSingleResult() == ValidationStatus.VALIDATED;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public SubmissionStatus getTimesheetSubmissionStatus(Account currentAccount, Calendar calendar, int year, int week){
        TypedQuery<SubmittedTimesheet> q = em.createQuery("select st from SubmittedTimesheet st "
                + "where st.account = :user and st.year = :year and st.week = :week", SubmittedTimesheet.class);
        q.setParameter("week", week);
        q.setParameter("year", year);
        q.setParameter("user", currentAccount);

        try {
            q.getSingleResult();
            return SubmissionStatus.SUBMITTED;
        } catch (Exception e) {

            final int previousWeek = findLastWeek(calendar, week, year);
            final int previousWeekYear = findLastWeekYear(calendar, week, year);
            final boolean isPreviousTimesheetSubmitted = this.isTimesheetSubmitted(currentAccount, previousWeekYear, previousWeek);

            return isPreviousTimesheetSubmitted ? SubmissionStatus.PENDING_SUBMISSION: SubmissionStatus.PENDING_PREVIOUS_SUBMISSION;
        }
    }

    @Override
    public ValidationStatus getTimesheetValidationStatus(Account currentAccount, int year, int week){
        TypedQuery<ValidationStatus> q = em.createQuery("select st.validationStatus from SubmittedTimesheet st "
                + "where st.account = :user and st.year = :year and st.week = :week", ValidationStatus.class);
        q.setParameter("week", week);
        q.setParameter("year", year);
        q.setParameter("user", currentAccount);

        try {
            return q.getSingleResult();
        } catch (Exception e) {
            return ValidationStatus.PENDING_SUBMISSION;
        }
    }

    @Override
    public double getSumImputationForWeek(Date firstDayOfWeek, Date lastDayOfWeek, Account account) {
        TypedQuery<Double> q = em.createQuery(
                "SELECT COALESCE(sum(i.value),0) \n"
                        + "FROM Imputation i\n"
                        + "WHERE i.account = :user \n"
                        + "AND i.day >= :firstDayOfWeek\n"
                        + "AND i.day <= :lastDayOfWeek", Double.class);
        q.setParameter("firstDayOfWeek", firstDayOfWeek);
        q.setParameter("lastDayOfWeek", lastDayOfWeek);
        q.setParameter("user", account);
        return q.getSingleResult();
    }


    @Override
    public Map<Integer, Double> getProjectImputationSumForDate(Date startDate, Date endDate, Account user, Project project) {
        TypedQuery<Object[]> q = (TypedQuery<Object[]>) em.createNativeQuery(
                "SELECT DAY(day), COALESCE(sum(i.value),0) \n"
                        + "FROM Imputation i JOIN Task t ON i.task_id = t.id \n"
                        + "WHERE i.account_id = :user \n"
                        + "AND i.day >= :startDate\n"
                        + "AND i.day <= :endDate\n"
                        + "AND t.project_id = :project\n"
                        + "GROUP BY i.day");
        q.setParameter("project", project.getId());
        q.setParameter("startDate", startDate);
        q.setParameter("endDate", endDate);
        q.setParameter("user", user.getId());
        List<Object[]> dayImputations = q.getResultList();

        Map<Integer, Double> result = new HashMap<>();
        for (Object[] o : dayImputations) {
            result.put((int) o[0], (double) o[1]);
        }

        return result;

    }

    @Override
    public Map<Integer, Double> getTaskImputationForDate(Date startDate, Date endDate, Account user, AbstractTask task) {
        TypedQuery<Object[]> q = (TypedQuery<Object[]>) em.createNativeQuery(
                "SELECT DAY(day), COALESCE(i.value,0) \n"
                        + "FROM Imputation i\n"
                        + "WHERE i.account_id = :user \n"
                        + "AND i.day >= :startDate\n"
                        + "AND i.day <= :endDate\n"
                        + "AND i.task_id = :task\n"
                        + "GROUP BY i.day");
        q.setParameter("task", task.getId());
        q.setParameter("startDate", startDate);
        q.setParameter("endDate", endDate);
        q.setParameter("user", user.getId());
        List<Object[]> dayImputations = q.getResultList();

        Map<Integer, Double> result = new HashMap<>();

        for (Object[] o : dayImputations) {
            result.put((int) o[0], (double) o[1]);
        }

        return result;
    }


}
