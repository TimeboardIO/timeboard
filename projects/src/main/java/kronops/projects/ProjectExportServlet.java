package kronops.projects;

/*-
 * #%L
 * projects
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

import kronops.core.api.ProjectExportService;
import kronops.core.api.ProjectService;
import kronops.core.model.Project;
import kronops.security.SecurityContext;
import org.osgi.service.component.annotations.*;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

@Component(
        service = Servlet.class,
        scope = ServiceScope.PROTOTYPE,
        property = {
                "osgi.http.whiteboard.servlet.pattern=/projects/export",
                "osgi.http.whiteboard.context.select=(osgi.http.whiteboard.context.name=kronops)"
        }
)
public class ProjectExportServlet  extends HttpServlet {

    @Reference
    private ProjectService projectService;

    @Reference(
            policyOption = ReferencePolicyOption.GREEDY,
            cardinality = ReferenceCardinality.MULTIPLE,
            collectionType = CollectionType.SERVICE
    )
    private List<ProjectExportService> projectExportServices;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        final String type = req.getParameter("type");
        final long projectID = Long.parseLong(req.getParameter("projectID"));

        final Project project = this.projectService.getProjectByID(SecurityContext.getCurrentUser(req), projectID);
        project.setTasks(new HashSet<>(this.projectService.listProjectTasks(project)));

        final Optional<ProjectExportService> optionalService = this.projectExportServices.stream()
                .filter(projectExportService -> projectExportService.isCandidate(type))
                .findFirst();

        if(optionalService.isPresent()){
            try(ByteArrayOutputStream buf = new ByteArrayOutputStream()) {
                optionalService.get().export(SecurityContext.getCurrentUser(req), project.getId(), buf);
                resp.setContentLengthLong(buf.toByteArray().length);
                resp.setHeader("Expires:", "0");
                resp.setHeader("Content-Disposition", "attachment; filename="+project.getName()+"."+optionalService.get().getExtension());
                resp.setContentType(optionalService.get().getMimeType());

                resp.getOutputStream().write(buf.toByteArray());
                resp.getOutputStream().flush();

                resp.setStatus(201);
            }
        }else{
            resp.setStatus(404);
        }
    }
}