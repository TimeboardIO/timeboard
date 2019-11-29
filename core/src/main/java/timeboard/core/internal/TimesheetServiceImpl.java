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

import org.apache.aries.jpa.template.JpaTemplate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceScope;
import org.osgi.service.log.LogService;
import timeboard.core.api.ProjectService;
import timeboard.core.api.TimeboardSubjects;
import timeboard.core.api.TimesheetService;
import timeboard.core.api.UserService;
import timeboard.core.api.exceptions.TimesheetException;
import timeboard.core.internal.events.TimesheetEvent;
import timeboard.core.model.User;
import timeboard.core.model.ValidatedTimesheet;

import javax.persistence.TypedQuery;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Component(
        service = TimesheetService.class
)
public class TimesheetServiceImpl implements TimesheetService {

    /**
     * Injected instance of timeboard persistence unit.
     */
    @Reference(
            target = "(osgi.unit.name=timeboard-pu)",
            scope = ReferenceScope.BUNDLE)
    private JpaTemplate jpa;

    @Reference
    private UserService userService;

    @Reference
    private ProjectService projectService;

    @Reference
    private LogService logService;

    @Override
    public void validateTimesheet(User actor, User userTimesheet, int year, int week) throws TimesheetException {


        //check if validation is possible

        //1 - last week is validated

        final Calendar c = Calendar.getInstance();
        c.set(Calendar.WEEK_OF_YEAR, week);
        c.set(Calendar.YEAR, year);
        c.setFirstDayOfWeek(Calendar.MONDAY);
        c.set(Calendar.HOUR_OF_DAY, 2);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);

        c.roll(Calendar.WEEK_OF_YEAR, -1); // remove 1 week

        boolean lastWeekValidated = this.isTimesheetValidated(userTimesheet, c.get(Calendar.YEAR), c.get(Calendar.WEEK_OF_YEAR));
        if(!lastWeekValidated) throw new TimesheetException("Can not validate this week, previous week is not validated");


        //2 - all imputation day sum == 1
        c.set(Calendar.WEEK_OF_YEAR, week);
        c.set(Calendar.YEAR, year);

        boolean allDailyImputationTotalsAreOne = this.jpa.txExpr(entityManager -> {

            Boolean result = true;
            c.set(Calendar.WEEK_OF_YEAR, week);
            c.set(Calendar.YEAR, year);
            c.setFirstDayOfWeek(Calendar.MONDAY);
            c.set(Calendar.DAY_OF_WEEK, 2);

            for(int i=1; i<=5; i++) {

                TypedQuery<Double> q = entityManager.createQuery("select sum(value) from Imputation i where i.user = :user and i.day = :day ", Double.class);
                q.setParameter("user", userTimesheet);
                q.setParameter("day", c.getTime());
                final List<Double> resultList = q.getResultList();
                result &= (resultList.get(0) == 1.0);
                c.roll(Calendar.DAY_OF_WEEK,1);
            }
            return result;

        });
        if(!allDailyImputationTotalsAreOne) throw new TimesheetException("Can not validate this week, all daily imputations totals are not equals to 1");

        ValidatedTimesheet validatedTimesheet = new ValidatedTimesheet();
        validatedTimesheet.setValidatedBy(actor);
        validatedTimesheet.setUser(userTimesheet);
        validatedTimesheet.setYear(year);
        validatedTimesheet.setWeek(week);

        this.jpa.tx(entityManager -> {
            entityManager.persist(validatedTimesheet);
        });

        TimeboardSubjects.TIMESHEET_EVENTS.onNext(new TimesheetEvent(validatedTimesheet, projectService));

        this.logService.log(LogService.LOG_INFO, "Week " + week + " validated for user" + userTimesheet.getName()+" by user "+actor.getName());

    }

    @Override
    public boolean isTimesheetValidated(User userTimesheet, int year, int week) {
        return this.jpa.txExpr(entityManager -> {
            TypedQuery<ValidatedTimesheet> q = entityManager.createQuery("select vt from ValidatedTimesheet vt where vt.user = :user and vt.year = :year and vt.week = :week", ValidatedTimesheet.class);
            q.setParameter("week", week);
            q.setParameter("year", year);
            q.setParameter("user", userTimesheet);

            try {
                q.getSingleResult();
                return true;
            } catch (Exception e) {
                return false;
            }
        });
    }

    @Override
    public double getSumImputationForWeek(Date firstDayOfWeek, Date lastDayOfWeek, User user) {
        return this.jpa.txExpr(entityManager -> {
            TypedQuery<Double> q = entityManager.createQuery(
                    "SELECT sum(i.value) \n" +
                            "FROM Imputation i\n" +
                            "WHERE i.user = :user \n" +
                            "AND i.day > :firstDayOfWeek\n" +
                            "AND i.day < :lastDayOfWeek"
                    , Double.class);
            q.setParameter("firstDayOfWeek", firstDayOfWeek);
            q.setParameter("lastDayOfWeek", lastDayOfWeek);
            q.setParameter("user", user);
            return q.getSingleResult();
        });
    }
    
}
