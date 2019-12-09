package timeboard.core.model;

import timeboard.core.api.exceptions.BusinessException;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(uniqueConstraints={
        @UniqueConstraint(columnNames = {"parent_id", "child_id"})
})
public class AccountHierarchy {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @ManyToOne(targetEntity = Account.class)
    private Account parent;

    @ManyToOne(targetEntity = Account.class)
    private Account child;

    @Column(nullable = false)
    private Date startDate;

    @Column
    private Date endDate;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Account getParent() {
        return parent;
    }

    public void setParent(Account parent) throws BusinessException {
        if(this.child != null && this.parent.getId() == this.child.getId()){
            throw  new BusinessException("An parent account can't refer to itself");
        }
        this.parent = parent;
    }

    public Account getChild() {
        return child;
    }

    public void setChild(Account child) throws BusinessException {
        if(this.parent != null && this.child.getId() == this.parent.getId()){
            throw  new BusinessException("An child account can't refer to itself");
        }
        this.child = child;
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
}
