package kronops.core.model;


//import kronops.apigenerator.annotation.RPCEntity;

import kronops.apigenerator.annotation.RPCEntity;

import javax.persistence.*;
//import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


@Entity
@RPCEntity
public class Project implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column(length = 50)
    //@NotNull
    private String name;

    @Temporal(TemporalType.DATE)
    private Date startDate;

    @Column(length = 500)
    private String comments;

    @OneToMany(targetEntity = ProjectMembership.class, mappedBy = "project")
    private List<ProjectMembership> members;

    public Project() {
        members = new ArrayList<>();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public List<ProjectMembership> getMembers() {
        return members;
    }

    public void setMembers(List<ProjectMembership> members) {
        this.members = members;
    }
}
