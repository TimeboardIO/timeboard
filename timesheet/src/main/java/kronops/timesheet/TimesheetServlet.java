package kronops.timesheet;

/*-
 * #%L
 * kanban-project-plugin
 * %%
 * Copyright (C) 2019 Kronops
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

import kronops.core.api.ProjectService;
import kronops.core.api.ProjectTasks;
import kronops.core.model.User;
import kronops.core.ui.KronopsServlet;
import kronops.core.ui.ViewModel;
import kronops.security.SecurityContext;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicyOption;

import javax.servlet.Servlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

@Component(
        service = Servlet.class,
        property = {
                "osgi.http.whiteboard.servlet.pattern=/timesheet",
                "osgi.http.whiteboard.context.select=(osgi.http.whiteboard.context.name=kronops)"
        }

)
public class TimesheetServlet extends KronopsServlet {

    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    @Reference(cardinality = ReferenceCardinality.OPTIONAL, policyOption = ReferencePolicyOption.GREEDY)
    private ProjectService projectService;


    @Override
    protected ClassLoader getTemplateResolutionClassLoader() {
        return TimesheetServlet.class.getClassLoader();
    }


    @Override
    protected void handleGet(HttpServletRequest request, HttpServletResponse response, ViewModel viewModel) throws Exception {
        Set<ProjectTasks> tasksByProject = new HashSet<>();
        int week = Integer.parseInt(request.getParameter("week"));
        int year = Integer.parseInt(request.getParameter("year"));

        Calendar c = Calendar.getInstance();
        c.set(Calendar.WEEK_OF_YEAR, week);
        c.set(Calendar.YEAR, year);

        c.setFirstDayOfWeek(Calendar.MONDAY);
        c.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);

        Date ds = c.getTime();
        c.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        Date de = c.getTime();


        if (this.projectService != null) {
            User actor = SecurityContext.getCurrentUser(request);
            tasksByProject.addAll(this.projectService.listTasksByProject(actor, ds, de));
        }

        List<DateWrapper> days = new ArrayList<>();
        c.setTime(ds);
        for(int i=0; i<7; i++){
            DateWrapper dw = new DateWrapper();
            dw.setDay(c.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.ENGLISH));
            dw.setDate(c.getTime());
            days.add(dw);
            c.add(Calendar.DAY_OF_YEAR, 1);
        }

        viewModel.getViewDatas().put("days", days);
        viewModel.getViewDatas().put("week", week);
        viewModel.getViewDatas().put("year", year);
        viewModel.getViewDatas().put("projectTasks", tasksByProject);
        viewModel.setTemplate("timesheet.html");
    }


    private class DateWrapper {

        private String day;
        private Date date;

        public String getDay() {
            return day;
        }

        public void setDay(String day) {
            this.day = day;
        }

        public Date getDate() {
            return date;
        }

        public void setDate(Date date) {
            this.date = date;
        }
    }
}
