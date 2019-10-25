package timeboard.ui;

/*-
 * #%L
 * webui
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
import timeboard.core.api.TimesheetService;
import timeboard.core.model.User;
import timeboard.core.ui.TimeboardServlet;
import timeboard.core.ui.ViewModel;
import timeboard.security.SecurityContext;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;
import timeboard.ui.model.Week;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


@Component(
        service = Servlet.class,
        scope = ServiceScope.PROTOTYPE,
        property = {
                "osgi.http.whiteboard.servlet.pattern=/",
                "osgi.http.whiteboard.context.select=(osgi.http.whiteboard.context.name=timeboard)"
        }
)
public class HomeServlet extends TimeboardServlet {

    @Reference
    private ProjectService projectService;

    @Reference
    private TimesheetService timesheetService;

    @Override
    protected ClassLoader getTemplateResolutionClassLoader() {
        return HomeServlet.class.getClassLoader();
    }

    @Override
    protected void handlePost(HttpServletRequest request, HttpServletResponse response, ViewModel viewModel) throws ServletException, IOException {
        viewModel.setTemplate("home.html");
    }

    @Override
    protected void handleGet(HttpServletRequest request, HttpServletResponse response, ViewModel viewModel) throws ServletException, IOException {

        //load previous weeks data
        Date d = new Date();
        Calendar c = Calendar.getInstance();
        c.setTime(d);
        List<Week> weeks = new ArrayList<>();
        User user = SecurityContext.getCurrentUser(request);
        int weeksToDisplay = 3; //TODO replace by a parameter ?
        for(int i=0; i<weeksToDisplay; i++){
            double weekSum = timesheetService.getWeekImputationSum(user, c.get(Calendar.YEAR), c.get(Calendar.WEEK_OF_YEAR));
            boolean weekIsValidated =timesheetService.isTimesheetValidated(user, c.get(Calendar.YEAR), c.get(Calendar.WEEK_OF_YEAR));

            Week week = new Week(c.get(Calendar.WEEK_OF_YEAR),c.get(Calendar.YEAR), weekSum, weekIsValidated);
            weeks.add(week);
            c.roll(Calendar.WEEK_OF_YEAR,-1);
        }

        viewModel.getViewDatas().put("nb_projects", this.projectService.listProjects(user).size());
        viewModel.getViewDatas().put("nb_tasks", this.projectService.listUserTasks(user).size());
        viewModel.getViewDatas().put("weeks", weeks);

        viewModel.setTemplate("home.html");
    }


}
