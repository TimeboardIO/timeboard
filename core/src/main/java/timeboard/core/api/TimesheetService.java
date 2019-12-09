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

import timeboard.core.api.exceptions.TimesheetException;
import timeboard.core.model.AbstractTask;
import timeboard.core.model.Project;
import timeboard.core.model.User;
import timeboard.core.model.Account;

import java.util.Date;
import java.util.Map;


public interface TimesheetService {

    /**
     * Validate user timesheet.
     * @param actor         user who trigger this function.
     * @param accountTimesheet user which be used to build timehseet to validate
     * @param year            timesheet year
     * @param week            timesheet week
     * @return true if timesheet is validate else, false.
     */
    void validateTimesheet(Account actor, Account accountTimesheet, int year, int week) throws TimesheetException;

    /**
     * Is timesheet validated.
     * @param accountTimesheet user used to check timesheet validation state.
     * @param week          timesheet week
     * @param year          timesheet year
     * @return true if timesheet is already validated
     */
    boolean isTimesheetValidated(Account accountTimesheet, int year, int week);


    /**
     * Get the sum of all imputations by week by user.
     * @param firstDayOfWeek first day of week
     * @param lastDayOfWeek last day of week
     * @param account user used to check timesheet validation state.
     * @return the sum of all imputations of the week
     */
    double getSumImputationForWeek(Date firstDayOfWeek, Date lastDayOfWeek, Account account);

    Map<Integer, Double> getProjectImputationSumForDate(Date startDate, Date endDate, User user, Project project);

    Map<Integer, Double> getTaskImputationForDate(Date startDate, Date endDate, User user, AbstractTask task);
}
