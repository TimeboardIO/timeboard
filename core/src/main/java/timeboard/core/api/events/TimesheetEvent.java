package timeboard.core.api.events;

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
import timeboard.core.model.MembershipRole;
import timeboard.core.model.Organization;
import timeboard.core.model.Project;
import timeboard.core.model.SubmittedTimesheet;

import java.util.Date;
import java.util.List;


public class TimesheetEvent extends TimeboardEvent {

    private SubmittedTimesheet timesheet;


    public TimesheetEvent(final SubmittedTimesheet timesheet, final ProjectService projectService, final Organization currentOrg) {
        super(new Date());

        this.timesheet = timesheet;

        final List<Project> projects = projectService.listProjects(timesheet.getAccount(), currentOrg.getId());

        projects.forEach(project -> project.getMembers()
                .stream()
                .filter(member -> member.getRole() == MembershipRole.OWNER)
                .forEach(member -> this.usersToNotify.add(member.getMember())));

        usersToInform.add(timesheet.getAccount());
    }


    public SubmittedTimesheet getTimesheet() {
        return timesheet;
    }
}
