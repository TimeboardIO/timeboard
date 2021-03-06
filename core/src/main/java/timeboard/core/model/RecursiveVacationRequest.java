package timeboard.core.model;

/*-
 * #%L
 * core
 * %%
 * Copyright (C) 2019 - 2020 Timeboard
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
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

@Entity
@PrimaryKeyJoinColumn(name = "id")
public class RecursiveVacationRequest extends VacationRequest {

    @Column
    private int recurrenceDay;

    @OneToMany(targetEntity = VacationRequest.class,
            mappedBy = "parent", fetch = FetchType.EAGER,
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private Set<VacationRequest> children;

    public RecursiveVacationRequest() {
        super();
        this.children = new HashSet<>();
    }

    public RecursiveVacationRequest(final VacationRequest other) {
        super(other);
        this.children = new HashSet<>();
    }

    @Override
    public RecursiveVacationRequest getParent() {
        return null;
    }

    @Override
    public void setParent(final RecursiveVacationRequest parent) {
        //Do nothing
    }

    @Transient
    public void generateChildren() {
        final java.util.Calendar start = java.util.Calendar.getInstance();
        start.setTime(this.getStartDate());
        start.set(Calendar.HOUR, 0);

        final java.util.Calendar currentDate = java.util.Calendar.getInstance();
        currentDate.setTime(this.getStartDate());
        currentDate.set(Calendar.HOUR, 0);

        final java.util.Calendar end = java.util.Calendar.getInstance();
        end.setTime(this.getEndDate());
        end.set(Calendar.HOUR, 0);

        while (currentDate.compareTo(end) <= 0) {
            currentDate.set(java.util.Calendar.DAY_OF_WEEK, this.getRecurrenceDay() % 7); //Calendar first day of week is sunday
            // >= means afterOrEquals //  <= means beforeOrEquals
            if (currentDate.compareTo(start) >= 0 && currentDate.compareTo(end) <= 0) {
                final VacationRequest child = new VacationRequest(this);
                child.setParent(this);
                child.setStartDate(currentDate.getTime());
                child.setEndDate(currentDate.getTime());
                this.getChildren().add(child);
            }
            currentDate.add(Calendar.WEEK_OF_YEAR, 1);
        }

    }

    public int getRecurrenceDay() {
        return recurrenceDay;
    }

    public void setRecurrenceDay(final int recurrenceRule) {
        this.recurrenceDay = recurrenceRule;
    }

    public Set<VacationRequest> getChildren() {
        return children;
    }

    public void setChildren(final Set<VacationRequest> children) {
        this.children = children;
    }


}




