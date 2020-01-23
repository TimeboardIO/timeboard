package timeboard.core.internal.observers.emails;

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

import timeboard.core.api.events.TaskEvent;
import timeboard.core.model.Project;

import java.util.ArrayList;
import java.util.List;

public class ProjectEmailSummaryModel {

    private Project project;
    private List<TaskEvent> createdTasks = new ArrayList<>();

    private List<TaskEvent> deletedTasks = new ArrayList<>();

    private List<TaskEvent> approvedTasks = new ArrayList<>();

    private List<TaskEvent> deniedTasks = new ArrayList<>();

    public ProjectEmailSummaryModel(final Project p) {
        this.project = p;
    }

    public void addCreatedTask(final TaskEvent e) {
        createdTasks.add(e);
    }

    public void addDeletedTask(final TaskEvent e) {
        deletedTasks.add(e);
    }

    public void addApprovedTask(final TaskEvent e) {
        approvedTasks.add(e);
    }

    public void addDeniedTask(final TaskEvent e) {
        deniedTasks.add(e);
    }

    public Project getProject() {
        return project;
    }

    public List<TaskEvent> getCreatedTasks() {
        return createdTasks;
    }

    public List<TaskEvent> getDeletedTasks() {
        return deletedTasks;
    }

    public List<TaskEvent> getApprovedTasks() {
        return approvedTasks;
    }

    public List<TaskEvent> getDeniedTasks() {
        return deniedTasks;
    }
}
