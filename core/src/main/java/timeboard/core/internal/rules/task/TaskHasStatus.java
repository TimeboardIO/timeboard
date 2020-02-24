package timeboard.core.internal.rules.task;

import timeboard.core.internal.rules.Rule;
import timeboard.core.model.Account;
import timeboard.core.model.Task;
import timeboard.core.model.TaskStatus;

public class TaskHasStatus implements Rule<Task> {

    private final TaskStatus status;

    public TaskHasStatus(TaskStatus status) {
        this.status= status;
    }

    @Override
    public String ruleDescription() {
        return "Task with imputations cannot be removed";
    }

    @Override
    public boolean isSatisfied(final Account u, final Task thing) {
        return thing.getTaskStatus().equals(this.status);
    }

}