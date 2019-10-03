package kronops.core.internal;

/*-
 * #%L
 * core
 * %%
 * Copyright (C) 2019 Kronops
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

import kronops.core.api.TimesheetService;
import kronops.core.api.UserService;
import kronops.core.model.User;
import kronops.core.model.ValidatedTimesheet;
import org.apache.aries.jpa.template.JpaTemplate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceScope;

import javax.persistence.TypedQuery;
import java.util.Calendar;

@Component(
        service = TimesheetService.class
)
public class TimesheetServiceImpl implements TimesheetService {

    /**
     * Injected instance of kronops persistence unit.
     */
    @Reference(
            target = "(osgi.unit.name=kronops-pu)",
            scope = ReferenceScope.BUNDLE)
    private JpaTemplate jpa;

    @Reference
    private UserService userService;

    @Override
    public boolean validateTimesheet(long actorID, long userTimesheetID, int year, int week) {

        User actor = this.userService.findUserByID(actorID);
        User userTimesheet = this.userService.findUserByID(userTimesheetID);

        Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR, year);
        c.set(Calendar.WEEK_OF_YEAR, week);


        ValidatedTimesheet validatedTimesheet = new ValidatedTimesheet();
        validatedTimesheet.setValidatedBy(actor);
        validatedTimesheet.setUser(userTimesheet);
        validatedTimesheet.setYear(year);
        validatedTimesheet.setWeek(week);

        this.jpa.tx(entityManager -> {
            entityManager.persist(validatedTimesheet);
        });

        return true;
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
}
