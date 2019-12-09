package timeboard.core.internal.events;

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

import timeboard.core.model.Project;
import timeboard.core.model.ProjectRole;
import timeboard.core.model.Task;
import timeboard.core.model.Account;

import java.util.Date;


public class TaskEvent extends TimeboardEvent {
    private Task task;
    private Account actor;
    private TimeboardEventType eventType;


    public TaskEvent(TimeboardEventType eventType, Task task, Account actor) {
        super(new Date());
        this.eventType = eventType;
        this.task = task;
        this.actor = actor;

        this.constructUsersList();
    }

    public TimeboardEventType getEventType() {
        return eventType;
    }

    public void setEventType(TimeboardEventType eventType) {
        this.eventType = eventType;
    }

    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
    }

    public Account getActor() {
        return actor;
    }

    public void setActor(Account actor) {
        this.actor = actor;
    }



    private void constructUsersList() {
        Project project = task.getProject();
        Account assignedAccount = task.getAssigned();

        project.getMembers()
                .stream()
                .filter(member -> member.getRole() == ProjectRole.OWNER)
                .forEach(member -> this.usersToNotify.add(member.getMember()));

        if (assignedAccount != null) {
            usersToInform.add(assignedAccount);
        }
        usersToInform.add(actor);

    }

}
