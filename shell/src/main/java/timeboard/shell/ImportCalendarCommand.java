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
import timeboard.core.api.CalendarService;
import timeboard.core.api.ProjectService;
import timeboard.core.api.UserService;
import timeboard.core.model.AbstractTask;
import timeboard.core.model.Project;
import timeboard.core.model.User;

import java.util.ArrayList;
import java.util.List;

@Service
@Command(scope = "timeboard", name = "import-ics", description = "Create new user account")
public class ImportCalendarCommand implements Action {

    @Option(name = "-f", aliases = {"--file"}, description = "ICS file path", required = true, multiValued = false)
    String file;

    @Option(name = "-n", aliases = {"--name"}, description = "Calendar name", required = false, multiValued = false)
    String name;

    @Option(name = "-a", aliases = {"--actor"}, description = "Action actor", required = false, multiValued = true)
    List<String> usernames;

    @Option(name = "-p", aliases = {"--project"}, description = "Import as project tasks", required = false, multiValued = false)
    String projectArg;

    @Option(name = "-t", aliases = {"--task"}, description = "Import as imputations in a Task ", required = false, multiValued = false)
    String taskArg;

    @Option(name = "-v", aliases = {"--imputation-value"}, description = "Imputation value", required = false, multiValued = false)
    String imputationValue;

    @Option(name = "-all", aliases = {"--all-users"}, description = "Action actor", required = false, multiValued = false)
    boolean allUsers;

    @Option(name = "-do", aliases = {"--delete-orphan"}, description = "Delete orphan", required = false, multiValued = false)
    boolean deleteOrphan;


    @Override
    public Object execute() throws Exception {

        if(!(taskArg != null || projectArg != null)){
            throw new Exception("You must specify either project -p or default task");
        }

        BundleContext bnd = FrameworkUtil.getBundle(ImportCalendarCommand.class).getBundleContext();

        ServiceReference<CalendarService> calendarServiceRef = bnd.getServiceReference(CalendarService.class);
        CalendarService calendarService =  bnd.getService(calendarServiceRef);

        ServiceReference<UserService> userServiceRef = bnd.getServiceReference(UserService.class);
        UserService userService =  bnd.getService(userServiceRef);

        ServiceReference<ProjectService> projectServiceRef = bnd.getServiceReference(ProjectService.class);
        ProjectService projectService =  bnd.getService(projectServiceRef);

        if(name == null){
            // user filename as name
            this.name = file;
        }

        User actor = userService.findUserByLogin("timeboard"); //TODO dirty
        if(taskArg != null){
            //treat as imputation

            if(!allUsers && (usernames == null || usernames.isEmpty())) throw new Exception("You must specify a user list or use option -all to apply imputations to all users");

            final List<AbstractTask> tasksByName = projectService.getTasksByName(taskArg);
            final List<User> userList= new ArrayList<>();
            for(String username : usernames){
                userList.add(userService.findUserByLogin(username));
            }
            calendarService.importCalendarAsImputationsFromICS(actor, file, tasksByName.get(0), userList, Double.parseDouble(imputationValue), deleteOrphan);
        }else{

            final Project project = projectService.getProjectByName(projectArg);
            calendarService.importCalendarAsTasksFromICS(actor, name, file, project, deleteOrphan);

        }

        return null;
    }
}
