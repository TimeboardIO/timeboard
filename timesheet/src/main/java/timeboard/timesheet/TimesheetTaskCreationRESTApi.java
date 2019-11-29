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
import timeboard.core.api.exceptions.BusinessException;
import timeboard.core.model.Project;
import timeboard.core.model.User;

import timeboard.core.ui.TimeboardServlet;

import javax.servlet.Servlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Component(
        service = Servlet.class,
        property = {
                "osgi.http.whiteboard.servlet.pattern=/timesheet/api/task",
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
        response.setContentType("application/json");

        try{
            startDate = DATE_FORMAT.parse(request.getParameter("startDate"));
            endDate = DATE_FORMAT.parse(request.getParameter("endDate"));

        }catch(ParseException e) {
            MAPPER.writeValue(response.getWriter(), "Incorrect date format");
            return;
        }

        if(startDate.getTime()>endDate.getTime()){
            MAPPER.writeValue(response.getWriter(), "Start date must be before end date ");
            return;
        }

        String name = request.getParameter("taskName");
        String comment = request.getParameter("taskComments");
        if(comment == null) comment = "";

        double oe = Double.parseDouble(request.getParameter("originalEstimate"));
        if(oe <= 0.0){
            MAPPER.writeValue(response.getWriter(), "Original original estimate must be positive ");
            return;
        }

        User actor = getActorFromRequestAttributes(request);
        Long projectID = Long.parseLong(request.getParameter("projectID"));
        Project project = null;
        try {
            project = this.projectService.getProjectByID(actor, projectID);
        } catch (BusinessException e) {
            MAPPER.writeValue(response.getWriter(), e.getMessage());
            return;
        }

        String type = request.getParameter("typeID");
        Long typeID = Long.parseLong(type);

        Long taskID = Long.parseLong(request.getParameter("taskID"));
        if(!(taskID != null && taskID == 0 )){
            try {
                projectService.deleteTaskByID(getActorFromRequestAttributes(request), taskID);
            } catch (Exception e){
                MAPPER.writeValue(response.getWriter(), e.getMessage());
                return;
            }
        }
        try{
            projectService.createTask(getActorFromRequestAttributes(request), project,
                    name, comment, startDate, endDate, oe, typeID, actor, ProjectService.ORIGIN_TIMEBOARD, null,null,null );
        }catch (Exception e){
            MAPPER.writeValue(response.getWriter(), "Error in task creation please verify your inputs and retry");
            return;
        }

        MAPPER.writeValue(response.getWriter(), "DONE");

    }

}
