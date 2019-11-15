package timeboard.timesheet;

/*-
 * #%L
 * kanban-project-plugin
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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicyOption;
import timeboard.core.api.ProjectService;
import timeboard.core.api.TimesheetService;
import timeboard.core.model.Project;
import timeboard.core.model.User;
import timeboard.core.ui.TimeboardServlet;
import timeboard.security.SecurityContext;

import javax.servlet.Servlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.http.HttpResponse;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Component(
        service = Servlet.class,
        property = {
                "osgi.http.whiteboard.servlet.pattern=/timesheet/api/create_task",
                "osgi.http.whiteboard.context.select=(osgi.http.whiteboard.context.name=timeboard)"
        }

)
public class TimesheetTaskCreationRESTApi extends TimeboardServlet {

    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Reference(cardinality = ReferenceCardinality.OPTIONAL, policyOption = ReferencePolicyOption.GREEDY)
    private ProjectService projectService;

    @Reference(cardinality = ReferenceCardinality.OPTIONAL, policyOption = ReferencePolicyOption.GREEDY)
    private TimesheetService timesheetService;

    @Override
    protected ClassLoader getTemplateResolutionClassLoader() {
        return TimesheetTaskCreationRESTApi.class.getClassLoader();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Date startDate = null;
        Date endDate = null;
        try{
            startDate = DATE_FORMAT.parse(request.getParameter("startdate"));
            endDate = DATE_FORMAT.parse(request.getParameter("enddate"));


        }catch(ParseException e) {
            response.setContentType("application/json");
            MAPPER.writeValue(response.getWriter(), "KO");
        }
        String name = request.getParameter("name");
        String comment = request.getParameter("comment");

        double oe = Double.parseDouble(request.getParameter("oe"));
        Long projectID = Long.parseLong(request.getParameter("projectID"));

        String type = request.getParameter("typeID");
        Long typeID = Long.parseLong(type);

        projectService.createTask(SecurityContext.getCurrentUser(request), projectID,
                name, comment, startDate, endDate, oe, typeID, SecurityContext.getCurrentUser(request) );

        response. setStatus(HttpServletResponse.SC_OK);
      //  MAPPER.writeValue(response.getWriter(), "ok");

    }

}
