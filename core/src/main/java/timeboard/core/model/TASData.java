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

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class TASData {

    private String matriculeID;
    private String name;
    private String firstName;
    private int month;
    private int year;
    private String businessCode;

    private List<String> dayMonthNames;
    private List<Integer> dayMonthNums;
    private Map<Integer, Double> workedDays;
    private Map<Integer, Double> offDays;
    private Map<Integer, Double> otherDays;
    private Map<Integer, String> comments;

    public String getMatriculeID() {
        return this.matriculeID;
    }

    public void setMatriculeID(final String matriculeID) {
        this.matriculeID = matriculeID;
    }

    public String getName() {
        return this.name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getFirstName() {
        return this.firstName;
    }

    public void setFirstName(final String firstName) {
        this.firstName = firstName;
    }

    public int getMonth() {
        return this.month;
    }

    public void setMonth(final int month) {
        this.month = month;
    }

    public int getYear() {
        return this.year;
    }

    public void setYear(final int year) {
        this.year = year;
    }

    public String getBusinessCode() {
        return this.businessCode;
    }

    public void setBusinessCode(final String businessCode) {
        this.businessCode = businessCode;
    }

    public List<String> getDayMonthNames() {
        return this.dayMonthNames;
    }

    public void setDayMonthNames(final List<String> dayMonthNames) {
        this.dayMonthNames = dayMonthNames;
    }

    public List<Integer> getDayMonthNums() {
        return this.dayMonthNums;
    }

    public void setDayMonthNums(final List<Integer> dayMonthNums) {
        this.dayMonthNums = dayMonthNums;
    }

    public Map<Integer, Double> getWorkedDays() {
        return this.workedDays;
    }

    public void setWorkedDays(final Map<Integer, Double> workedDays) {
        this.workedDays = workedDays;
    }

    public Map<Integer, Double> getOffDays() {
        return this.offDays;
    }

    public void setOffDays(final Map<Integer, Double> offDays) {
        this.offDays = offDays;
    }

    public Map<Integer, Double> getOtherDays() {
        return this.otherDays;
    }

    public void setOtherDays(final Map<Integer, Double> otherDays) {
        this.otherDays = otherDays;
    }

    public Map<Integer, String> getComments() {
        return this.comments;
    }

    public void setComments(final Map<Integer, String> comments) {
        this.comments = comments;
    }

}
