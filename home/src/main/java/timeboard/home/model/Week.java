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

public class Week {

    private int number;
    private int year;
    private double imputationSum;
    private Boolean isSubmitted;

    public Week(final int number, final int year, final double imputationSum, final Boolean isSubmitted) {
        this.number = number;
        this.year = year;
        this.imputationSum = imputationSum;
        this.isSubmitted = isSubmitted;
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

    public Boolean getSubmitted() {
        return isSubmitted;
    }

    public void setSubmitted(final Boolean isSubmitted) {
        this.isSubmitted = isSubmitted;
    }

}
