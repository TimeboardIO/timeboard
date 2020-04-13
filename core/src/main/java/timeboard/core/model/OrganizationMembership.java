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
import java.io.Serializable;
import java.util.Calendar;

@Entity
@Table(uniqueConstraints = {
        @UniqueConstraint(columnNames = {"member_id", "organization_id"})
})
public class OrganizationMembership implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    private Account member;

    @ManyToOne(fetch = FetchType.EAGER)
    private Organization organization;

    @Column()
    @Enumerated(EnumType.STRING)
    private MembershipRole role;

    @Column
    @Temporal(TemporalType.DATE)
    private Calendar creationDate;

    public OrganizationMembership() {
        this.creationDate = Calendar.getInstance();
    }


    public OrganizationMembership(final Organization organization, final Account owner, final MembershipRole role) {
        this.member = owner;
        this.role = role;
        this.organization = organization;
        this.creationDate = Calendar.getInstance();
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public Account getMember() {
        return member;
    }

    public void setMember(final Account member) {
        this.member = member;
        member.getOrganizationMemberships().add(this);
    }

    public Organization getOrganization() {
        return organization;
    }

    public void setOrganization(final Organization organization) {
        this.organization = organization;
        this.organization.getMembers().add(this);
    }

    public MembershipRole getRole() {
        return role;
    }

    public void setRole(final MembershipRole role) {
        this.role = role;
    }

    public Calendar getCreationDate() {
        return this.creationDate != null ? this.creationDate : Calendar.getInstance();
    }

    public void setCreationDate(final Calendar creationDate) {
        this.creationDate = creationDate;
    }
}
