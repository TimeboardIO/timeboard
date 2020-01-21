package timeboard.core.model;

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

public enum TimesheetWeekIndicator {
    SUBMISSION_DONE("Timesheet has been submitted by the user", "green"),
    SUBMISSION_PENDING("Timesheet's submission by the user is pending", "red"),
    SUBMISSION_PREVIOUS_PENDING("Some previous week are pending submission", "yellow"),
    //SUBMISSION_PENDING("", "grey"),
    VALIDATION_DONE("Timesheet has been validated by the manager", "green"),
    VALIDATION_PENDING("Timesheet's validation by the manager is pending", "red"),
    //VALIDATION_DONE("", "yellow"),
    VALIDATION_SUBMISSION_PENDING("Timesheet's submission by the user is pending", "grey");


    public final String label;
    public final String color;

    private TimesheetWeekIndicator(String label, String color) {
        this.label = label;
        this.color = color;
    }

    String getLabel() {
        return this.label;
    }

    String getColor() {
        return this.color;
    }
}