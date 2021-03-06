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
import java.util.Date;

@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class VacationRequest extends OrganizationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column
    private String label;

    @Column
    private VacationRequestStatus status;

    @Temporal(TemporalType.DATE)
    private Date startDate;

    @Temporal(TemporalType.DATE)
    private Date endDate;

    @Column
    private HalfDay startHalfDay;

    @Column
    private HalfDay endHalfDay;

    @OneToOne
    private Account applicant;

    @OneToOne
    private Account assignee;

    @ManyToOne(targetEntity = RecursiveVacationRequest.class)
    private RecursiveVacationRequest parent;

    public VacationRequest() {
    }

    public VacationRequest(final VacationRequest other) {
        this.label = other.label;
        this.status = other.status;
        this.startDate = other.startDate;
        this.endDate = other.endDate;
        this.startHalfDay = other.startHalfDay;
        this.endHalfDay = other.endHalfDay;
        this.applicant = other.applicant;
        this.assignee = other.assignee;
        this.organizationID = other.organizationID;
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(final String description) {
        this.label = description;
    }

    public VacationRequestStatus getStatus() {
        return status;
    }

    public void setStatus(final VacationRequestStatus status) {
        this.status = status;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(final Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(final Date endDate) {
        this.endDate = endDate;
    }

    public HalfDay getStartHalfDay() {
        return startHalfDay;
    }

    public void setStartHalfDay(final HalfDay startHalfDay) {
        this.startHalfDay = startHalfDay;
    }

    public HalfDay getEndHalfDay() {
        return endHalfDay;
    }

    public void setEndHalfDay(final HalfDay endHalfDay) {
        this.endHalfDay = endHalfDay;
    }

    public Account getAssignee() {
        return assignee;
    }

    public void setAssignee(final Account assignee) {
        this.assignee = assignee;
    }

    public Account getApplicant() {
        return applicant;
    }

    public void setApplicant(final Account applicant) {
        this.applicant = applicant;
    }

    public RecursiveVacationRequest getParent() {
        return parent;
    }

    public void setParent(final RecursiveVacationRequest parent) {
        this.parent = parent;
    }

    public enum HalfDay {
        MORNING,
        AFTERNOON,
    }

}




