package timeboard.home.model;

/*-
 * #%L
 * home
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

import org.springframework.format.annotation.DateTimeFormat;
import timeboard.core.model.ValidationStatus;

import java.util.Date;

public class WeekWrapper {

    private int number;
    private int year;
    private double imputationSum;
    private ValidationStatus validationStatus;
    @DateTimeFormat(pattern = "dd-MM-yyyy")
    private Date firstDay;
    @DateTimeFormat(pattern = "dd-MM-yyyy")
    private Date lastDay;

    public WeekWrapper(final int number, final int year, final double imputationSum, final ValidationStatus vs, final Date firstDay, final Date lastDay) {
        this.number = number;
        this.year = year;
        this.imputationSum = imputationSum;
        this.validationStatus = vs;
        this.firstDay = firstDay;
        this.lastDay = lastDay;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(final int number) {
        this.number = number;
    }

    public int getYear() {
        return year;
    }

    public void setYear(final int year) {
        this.year = year;
    }

    public double getImputationSum() {
        return imputationSum;
    }

    public void setImputationSum(final double imputationSum) {
        this.imputationSum = imputationSum;
    }

    public ValidationStatus getValidationStatus() {
        return validationStatus;
    }

    public void setValidationStatus(final ValidationStatus validationStatus) {
        this.validationStatus = validationStatus;
    }

    public Date getFirstDay() {
        return firstDay;
    }

    public void setFirstDay(Date firstDay) {
        this.firstDay = firstDay;
    }

    public Date getLastDay() {
        return lastDay;
    }

    public void setLastDay(Date lastDay) {
        this.lastDay = lastDay;
    }
}
