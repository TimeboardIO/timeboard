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
public class ProjectMembership extends OrganizationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long membershipID;

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.PERSIST)
    private Account member;

    @OneToOne()
    private Project project;

    @Column()
    @Enumerated(EnumType.STRING)
    private MembershipRole role;

    public ProjectMembership() {
    }


    public ProjectMembership(final Project project, final Account owner, final MembershipRole role) {
        this.member = owner;
        this.role = role;
        this.project = project;

    }

    public Long getMembershipID() {
        return membershipID;
    }

    public void setMembershipID(final Long membershipID) {
        this.membershipID = membershipID;
    }

    public Account getMember() {
        return member;
    }

    public void setMember(final Account member) {
        this.member = member;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(final Project project) {
        this.project = project;
    }

    public MembershipRole getRole() {
        return role;
    }

    public void setRole(final MembershipRole role) {
        this.role = role;
    }
}
