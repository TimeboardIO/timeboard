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
import timeboard.core.model.*;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;


public interface TimesheetService {

    /**
     * Submit user timesheet.
     *
     * @param actor            user who trigger this function.
     * @param accountTimesheet user which be used to build timehseet to submit
     * @param year             timesheet year
     * @param week             timesheet week
     * @return true if timesheet is submit else, false.
     */
    SubmittedTimesheet submitTimesheet(
            final Organization currentOrg,
            final Account actor,
            final Account accountTimesheet,
            final int year,
            final int week) throws BusinessException;


    /**
     * Get timesheet validation status.
     *
     * @param currentAccount user used to check timesheet sumbit state.
     * @param year           timesheet year
     * @param week           timesheet week
     * @return ValidationStatus, null current account has no timesheet validation request for current week
     */
    Optional<ValidationStatus> getTimesheetValidationStatus(
            final Long orgID,
            final Account currentAccount,
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
            final Long orgID,
            final Date firstDayOfWeek,
            final Date lastDayOfWeek,
            final Account account,
            final TimesheetFilter... filters);


    Map<Account, List<SubmittedTimesheet>> getProjectTimesheetByAccounts(
            final Long orgID,
            final Account actor,
            final Project project);

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
