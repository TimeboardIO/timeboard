package timeboard.core.model;

import javax.persistence.*;
import java.util.Date;

@Entity
public class VacationRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column
    private String description;

    @Column
    private boolean validated = true;

    @Temporal(TemporalType.DATE)
    private Date startDate;

    @Temporal(TemporalType.DATE)
    private Date endDate;

    @Column
    private HalfDay startHalfDay;

    @Column
    private HalfDay endHalfDay;

    @OneToOne
    private Account assignee;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isValidated() {
        return validated;
    }

    public void setValidated(boolean validated) {
        this.validated = validated;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public HalfDay getStartHalfDay() {
        return startHalfDay;
    }

    public void setStartHalfDay(HalfDay startHalfDay) {
        this.startHalfDay = startHalfDay;
    }

    public HalfDay getEndHalfDay() {
        return endHalfDay;
    }

    public void setEndHalfDay(HalfDay endHalfDay) {
        this.endHalfDay = endHalfDay;
    }

    public Account getAssignee() {
        return assignee;
    }

    public void setAssignee(Account assignee) {
        this.assignee = assignee;
    }


    enum HalfDay {
        MORNING,
        AFTERNOON
    }

}




