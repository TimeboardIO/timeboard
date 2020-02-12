package timeboard.core.api;

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

import timeboard.core.api.exceptions.BusinessException;
import timeboard.core.api.exceptions.TimesheetException;
import timeboard.core.model.*;

import java.util.Calendar;
import java.util.*;


public interface TimesheetService {

    /**
     * Submit user timesheet.
     *
     * @param timesheetOwner user which be used to build timehseet to submit
     * @param year           timesheet year
     * @param week           timesheet week
     * @return true if timesheet is submit else, false.
     */
    SubmittedTimesheet submitTimesheet(
            final Organization currentOrg,
            final Account timesheetOwner,
            final int year,
            final int week) throws BusinessException;


    /**
     * Submit user timesheet.
     *
     * @param actor              user who trigger this function.
     * @param submittedTimesheet submittedTimesheet to validate
     * @return true if timesheet is submit else, false.
     */
    SubmittedTimesheet validateTimesheet(
            final Organization currentOrg,
            final Account actor,
            final SubmittedTimesheet submittedTimesheet) throws BusinessException;

    /**
     * Reject user timesheet.
     *
     * @param actor              user who trigger this function.
     * @param submittedTimesheet submittedTimesheet to reject
     * @return SubmittedTimesheet with status REJECTED
     */
    SubmittedTimesheet rejectTimesheet(final Organization currentOrg, final Account actor,
                                       final SubmittedTimesheet submittedTimesheet) throws BusinessException;

    Optional<SubmittedTimesheet> getSubmittedTimesheet(Organization org, Account timesheetOwner, int year, int week);


    /**
     * Get timesheet validation status.
     *
     * @param timesheetOwner user used to check timesheet sumbit state.
     * @param year           timesheet year
     * @param week           timesheet week
     * @return ValidationStatus, null current account has no timesheet validation request for current week
     */
    Optional<ValidationStatus> getTimesheetValidationStatus(
            final Organization org,
            final Account timesheetOwner,
            final int year,
            final int week);


    /**
     * Get the sum of all imputations by week by user.
     *
     * @param firstDayOfWeek first day of week
     * @param lastDayOfWeek  last day of week
     * @param filters        user used to check timesheet validation state.
     * @return the sum of all imputations of the week
     */
    Map<Integer, Double> getAllImputationsForAccountOnDateRange(
            final Organization org,
            final Date firstDayOfWeek,
            final Date lastDayOfWeek,
            final Account account,
            final TimesheetFilter... filters);

    UpdatedTaskResult updateTaskImputation(
            final Organization org,
            final Account actor,
            final AbstractTask task,
            final Date day,
            final double val) throws BusinessException;

    List<UpdatedTaskResult> updateTaskImputations(
            final Organization org,
            final Account actor,
            final List<Imputation> imputationsList);


    Map<Account, List<SubmittedTimesheet>> getProjectTimesheetByAccounts(
            final Organization org,
            final Account actor,
            final Project project);

    List<SubmittedTimesheet> getSubmittedTimesheets(final Long orgID, final Account actor, Account targetUser);


    /**
     * Force Validation of a list of weeks
     *
     * @param organizationID id of current organization
     * @param actor          project owner
     * @param target         timesheet owner (target of timesheet validation)
     * @param selectedYear   year of week selected in the "validation timesheet week" list
     * @param selectedWeek   week of week selected in the "validation timesheet week" list
     * @param olderYear      year of older week in the "validation timesheet week" list
     * @param olderWeek      week of older week in the "validation timesheet week" list
     */
    void forceValidationTimesheets(Organization organizationID, Account actor, Account target,
                                   int selectedYear, int selectedWeek, int olderYear, int olderWeek) throws TimesheetException;

    @Deprecated
    default long absoluteWeekNumber(SubmittedTimesheet t) {
        return absoluteWeekNumber((int) t.getYear(), (int) t.getWeek());
    }

    @Deprecated
    default long absoluteWeekNumber(int year, int week) {
        return (long) (year * 53) + week;
    }

    @Deprecated
    default long absoluteWeekNumber(java.util.Calendar c) {
        return absoluteWeekNumber(c.get(java.util.Calendar.YEAR), c.get(Calendar.WEEK_OF_YEAR));
    }

    class TimesheetFilter<T> {
        private T target;

        public TimesheetFilter(T target) {
            this.target = target;
        }

        public T getTarget() {
            return target;
        }
    }

}
