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

import javax.persistence.*;

@Entity
public class SubmittedTimesheet extends OrganizationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column
    @Enumerated(EnumType.STRING)
    private ValidationStatus timesheetStatus;

    @OneToOne(targetEntity = Account.class)
    private Account account;

    @Column
    private int year;

    @Column
    private int week;

    public long getId() {
        return id;
    }

    public void setId(final long id) {
        this.id = id;
    }

    public ValidationStatus getTimesheetStatus() {
        return timesheetStatus;
    }

    public void setTimesheetStatus(final ValidationStatus timesheetStatus) {
        this.timesheetStatus = timesheetStatus;
    }

    public long getYear() {
        return year;
    }

    public void setYear(final int y) {
        this.year = y;
    }

    public long getWeek() {
        return week;
    }

    public void setWeek(final int w) {
        this.week = w;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(final Account account) {
        this.account = account;
    }

}
