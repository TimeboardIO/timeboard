package kronops.core.model;

import javax.persistence.*;

@Entity
public class ProjectMembership {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long membershipID;

    @OneToOne
    private User member;

    @OneToOne
    private Project project;

    private ProjectRole role;

    public ProjectMembership() {
    }

    public ProjectMembership(Project p, User u, ProjectRole r) {
        this.project = p;
        this.member = u;
        this.role = r;
    }

    public Long getMembershipID() {
        return membershipID;
    }

    public void setMembershipID(Long membershipID) {
        this.membershipID = membershipID;
    }

    public User getMember() {
        return member;
    }

    public void setMember(User member) {
        this.member = member;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public ProjectRole getRole() {
        return role;
    }

    public void setRole(ProjectRole role) {
        this.role = role;
    }
}
