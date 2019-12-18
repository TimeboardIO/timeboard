package timeboard.core.api.sync;

import timeboard.core.model.Task;

import java.util.Date;

public class RemoteTask {

    private Long id;
    private String title;
    private String userName;
    private String comments;
    private String origin;
    private Date startDate;
    private Date stopDate;
    private Long localUserID;

    public Task toTask() {
        return null;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String summary) {
        this.title = summary;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String o) {
        this.origin = o;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date date) {
        this.startDate = date;
    }

    public Long getLocalUserID() {
        return localUserID;
    }

    public void setLocalUserID(Long localUserID) {
        this.localUserID = localUserID;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public Date getStopDate() {
        return stopDate;
    }

    public void setStopDate(Date stopDate) {
        this.stopDate = stopDate;
    }
}