package timeboard.core.notification.model;

import timeboard.core.model.Task;
import timeboard.core.model.User;

import java.io.Serializable;
import java.util.Date;

public class TaskEvent extends TimeboardEvent {
    private Task task;
    private User actor;

    public TaskEvent(Task task, User actor) {
        super(new Date());
        this.task = task;
        this.actor = actor;
    }

    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
    }

    public User getActor() {
        return actor;
    }

    public void setActor(User actor) {
        this.actor = actor;
    }
}
