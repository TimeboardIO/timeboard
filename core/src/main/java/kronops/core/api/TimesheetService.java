package kronops.core.api;

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

import kronops.core.model.User;

public interface TimesheetService {

    /**
     * Validate user timesheet.
     *
     * @param actorID user PK who trigger this function.
     * @param userTimesheetID user PK which be used to build timehseet to validate
     * @param year timesheet year
     * @param week timesheet week
     * @return true if timesheet is validate else, false.
     */
    boolean validateTimesheet(long actorID, long userTimesheetID, int year, int week);

    /**
     *
     * @param userTimesheet user used to check timesheet validation state.
     * @param week timesheet week
     * @param year timesheet year
     * @return true if timesheet is already validated
     */
    boolean isTimesheetValidated(User userTimesheet, int year, int week);
}
