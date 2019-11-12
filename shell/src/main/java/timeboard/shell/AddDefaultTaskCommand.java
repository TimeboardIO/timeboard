package timeboard.shell;

/*-
 * #%L
 * shell
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

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import timeboard.core.api.ProjectService;
import timeboard.core.api.UserService;
import timeboard.core.model.DefaultTask;
import timeboard.core.model.Project;
import timeboard.core.model.Task;
import timeboard.core.model.User;

import java.util.Calendar;
import java.util.Date;

@Service
@Command(scope = "timeboard", name = "add-default-task", description = "Create a new default task")
public class AddDefaultTaskCommand implements Action {

    @Option(name = "-n", aliases = {"--name"}, description = "Task name", required = true, multiValued = false)
    String name;

    @Option(name = "-c", aliases = {"--comment"}, description = "Task comment", required = false, multiValued = false)
    String comment;

    @Option(name = "-sd", aliases = {"--start-date"}, description = "Start date", required = false, multiValued = false)
    Date startDate;

    @Option(name = "-ed", aliases = {"--end-date"}, description = "End date", required = false, multiValued = false)
    Date endDate;


    @Override
    public Object execute() throws Exception {

        BundleContext bnd = FrameworkUtil.getBundle(AddDefaultTaskCommand.class).getBundleContext();
        ServiceReference<ProjectService> projectServiceRef = bnd.getServiceReference(ProjectService.class);
        ProjectService projectService =  bnd.getService(projectServiceRef);

        DefaultTask task = new DefaultTask();
        task.setName(name);
        if(comment != null) {
            task.setComments(comment);
        } else{
            task.setComments(name);
        }
        if(startDate != null) {
            task.setStartDate(startDate);
        } else{
            Calendar c = Calendar.getInstance();
            c.set(1,Calendar.JANUARY,1);
            task.setStartDate(c.getTime());
        }
        if(endDate != null) {
            task.setEndDate(endDate);
        } else{
            Calendar c = Calendar.getInstance();
            c.set(9999,Calendar.DECEMBER,31);
            task.setEndDate(c.getTime());
        }

        task.setOrigin("timeboard");

        projectService.createdDefaultTask(task);

        return null;
    }
}
