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

import timeboard.core.api.ProjectService;
import timeboard.core.model.Project;
import timeboard.core.model.ProjectRole;
import timeboard.core.model.ValidatedTimesheet;

import java.util.Date;
import java.util.List;


public class TimesheetEvent extends TimeboardEvent {

    private ValidatedTimesheet timesheet;


    public TimesheetEvent(ValidatedTimesheet timesheet, ProjectService projectService) {
        super(new Date());

        this.timesheet = timesheet;

        List<Project> projects = projectService.listProjects(timesheet.getUser());

        projects.stream().forEach(project -> project.getMembers()
                .stream()
                .filter(member -> member.getRole() == ProjectRole.OWNER)
                .forEach(member -> this.usersToNotify.add(member.getMember())));

        usersToInform.add(timesheet.getUser());
    }


    public ValidatedTimesheet getTimesheet() {
        return timesheet;
    }
}
