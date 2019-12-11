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

import timeboard.core.api.exceptions.BusinessException;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(uniqueConstraints={
        @UniqueConstraint(columnNames = {"organization_id", "member_id"})
})
public class AccountHierarchy {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @ManyToOne(targetEntity = Account.class)
    private Account organization;

    @ManyToOne(targetEntity = Account.class)
    private Account member;

    @Column(nullable = false)
    private Date startDate;

    @Column
    private Date endDate;

    @Column()
    @Enumerated(EnumType.STRING)
    private MembershipRole role;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Account getOrganization() {
        return organization;
    }

    public void setOrganization(Account organization) throws BusinessException {
        if(this.member != null && organization.getId() == this.member.getId()){
            throw  new BusinessException("An parent account can't refer to itself");
        }
        this.organization = organization;
    }

    public Account getMember() {
        return member;
    }

    public void setMember(Account member) throws BusinessException {
        if(this.organization != null && member.getId() == this.organization.getId()){
            throw  new BusinessException("An child account can't refer to itself");
        }
        this.member = member;
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


    public MembershipRole getRole() {
        return role;
    }

    public void setRole(MembershipRole role) {
        this.role = role;
    }

}
