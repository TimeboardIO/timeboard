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
import timeboard.core.api.events.TimesheetEvent;
import timeboard.core.api.exceptions.BusinessException;
import timeboard.core.api.exceptions.TimesheetException;
import timeboard.core.model.*;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.*;


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
    @CacheEvict(value = "accountTimesheet", key = "#accountTimesheet.getId()+'-'+#year+'-'+#week")
    public void submitTimesheet(Account actor, Account accountTimesheet, Organization currentOrg, int year, int week)
            throws BusinessException {


        final Calendar beginWorkDate = this.organizationService.findOrganizationMembership(actor, currentOrg).get().getCreationDate();

        int dayInFirstWeek = beginWorkDate.get(Calendar.DAY_OF_WEEK);
        boolean firstWeek = (beginWorkDate.get(Calendar.WEEK_OF_YEAR) == week) && (beginWorkDate.get(Calendar.YEAR) == year);

        Calendar previousWeek = Calendar.getInstance();
        previousWeek.set(Calendar.WEEK_OF_YEAR, week);
        previousWeek.set(Calendar.YEAR, year);
        previousWeek.setFirstDayOfWeek(Calendar.MONDAY);
        previousWeek.roll(Calendar.WEEK_OF_YEAR, -1); // remove 1 week

        boolean lastWeekSubmitted = this.isTimesheetSubmitted(
                accountTimesheet,
                previousWeek.get(Calendar.YEAR),
                previousWeek.get(Calendar.WEEK_OF_YEAR));

        if (!firstWeek && !lastWeekSubmitted) {
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

        long nbDays = ChronoUnit.DAYS.between(firstDay.toInstant(), lastDay.toInstant());
        final Double expectedSum = (nbDays+1.0d);

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

        em.persist(submittedTimesheet);

        TimeboardSubjects.TIMESHEET_EVENTS.onNext(new TimesheetEvent(submittedTimesheet, projectService, currentOrg));

        LOGGER.info("Timesheet for " + week + " submit for user" + accountTimesheet.getScreenName() + " by user " + actor.getScreenName());

    }

    @Override
    @Cacheable(value = "accountTimesheet", key = "#accountTimesheet.getId()+'-'+#year+'-'+#week")
    public boolean isTimesheetSubmitted(Account accountTimesheet, int year, int week) {
        final Long currentOrg = ThreadLocalStorage.getCurrentOrgId();
        final Optional<OrganizationMembership> organizationMembership =
                this.organizationService.findOrganizationMembership(accountTimesheet, currentOrg);

        if (organizationMembership.isPresent()) {

            Calendar beginWorkDate = organizationMembership.get().getCreationDate();
            Calendar currentDate = Calendar.getInstance();
            currentDate.set(Calendar.YEAR, year);
            currentDate.set(Calendar.WEEK_OF_YEAR, week);
            currentDate.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);

            if (currentDate.before(beginWorkDate) && !this.isSameWeek(currentDate, beginWorkDate)) {
                return true;
            }
        }

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

    private boolean isSameWeek(Calendar currentDate, Calendar beginWorkDate) {
        return
                currentDate.get(Calendar.YEAR) == beginWorkDate.get(Calendar.YEAR)
                        && currentDate.get(Calendar.WEEK_OF_YEAR) == beginWorkDate.get(Calendar.WEEK_OF_YEAR);
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
