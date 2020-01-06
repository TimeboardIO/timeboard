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
import timeboard.core.api.ProjectService;
import timeboard.core.api.TimeboardSubjects;
import timeboard.core.api.TimesheetService;
import timeboard.core.api.UserService;
import timeboard.core.api.exceptions.TimesheetException;
import timeboard.core.internal.events.TimesheetEvent;
import timeboard.core.model.AbstractTask;
import timeboard.core.model.Account;
import timeboard.core.model.Project;
import timeboard.core.model.ValidatedTimesheet;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
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


    @Override
    public void validateTimesheet(Account actor, Account accountTimesheet,  int year, int week) throws TimesheetException {


        //check if validation is possible

        //1 - last week is validated

        final Calendar c = Calendar.getInstance();

        c.setTime(accountTimesheet.getBeginWorkDate());

        boolean firstWeek = (c.get(Calendar.WEEK_OF_YEAR) == week) && (c.get(Calendar.YEAR) == year);

        c.set(Calendar.WEEK_OF_YEAR, week);
        c.set(Calendar.YEAR, year);
        c.setFirstDayOfWeek(Calendar.MONDAY);
        c.set(Calendar.HOUR_OF_DAY, 2);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);

        c.roll(Calendar.WEEK_OF_YEAR, -1); // remove 1 week

        boolean lastWeekValidated = this.isTimesheetValidated(
                accountTimesheet,
                c.get(Calendar.YEAR),
                c.get(Calendar.WEEK_OF_YEAR));

        if (!firstWeek && !lastWeekValidated) {
            throw new TimesheetException("Can not validate this week, previous week is not validated");
        }

        //2 - all imputation day sum == 1
        c.set(Calendar.WEEK_OF_YEAR, week);
        c.set(Calendar.YEAR, year);


        Boolean result = true;
        c.set(Calendar.WEEK_OF_YEAR, week);
        c.set(Calendar.YEAR, year);
        c.setFirstDayOfWeek(Calendar.MONDAY);
        c.set(Calendar.DAY_OF_WEEK, 2);

        for (int i = 1; i <= 5; i++) {

            TypedQuery<Double> q = em.createQuery("select sum(value) " +
                    "from Imputation i where i.account = :user and i.day = :day ", Double.class);

            q.setParameter("user", accountTimesheet);
            q.setParameter("day", c.getTime());
            final List<Double> resultList = q.getResultList();
            result &= (resultList.get(0) == 1.0);
            c.roll(Calendar.DAY_OF_WEEK, 1);
        }
        boolean allDailyImputationTotalsAreOne = result;

        if (!allDailyImputationTotalsAreOne) {
            throw new TimesheetException("Can not validate this week, all daily imputations totals are not equals to 1");
        }

        ValidatedTimesheet validatedTimesheet = new ValidatedTimesheet();
        validatedTimesheet.setValidatedBy(actor);
        validatedTimesheet.setAccount(accountTimesheet);
        validatedTimesheet.setYear(year);
        validatedTimesheet.setWeek(week);

        em.persist(validatedTimesheet);

        TimeboardSubjects.TIMESHEET_EVENTS.onNext(new TimesheetEvent(validatedTimesheet, projectService));

        LOGGER.info("Week " + week + " validated for user" + accountTimesheet.getName() + " by user " + actor.getName());

    }

    @Override
    public boolean isTimesheetValidated(Account accountTimesheet, int year, int week) {
        TypedQuery<ValidatedTimesheet> q = em.createQuery("select vt from ValidatedTimesheet vt "
                + "where vt.account = :user and vt.year = :year and vt.week = :week", ValidatedTimesheet.class);
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
