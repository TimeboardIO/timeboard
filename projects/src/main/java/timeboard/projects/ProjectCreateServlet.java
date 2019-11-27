package timeboard.projects;

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

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;
import timeboard.core.api.ProjectService;
import timeboard.core.api.exceptions.BusinessException;
import timeboard.core.ui.TimeboardServlet;
import timeboard.core.ui.ViewModel;
import timeboard.security.SecurityContext;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


@Component(
        service = Servlet.class,
        scope = ServiceScope.PROTOTYPE,
        property = {
                "osgi.http.whiteboard.servlet.pattern=/projects/create",
                "osgi.http.whiteboard.context.select=(osgi.http.whiteboard.context.name=timeboard)"
        }
)
public class ProjectCreateServlet extends TimeboardServlet {


    @Reference
    public ProjectService projectService;

    @Override
    protected ClassLoader getTemplateResolutionClassLoader() {
        return ProjectCreateServlet.class.getClassLoader();
    }

    @Override
    protected void handlePost(HttpServletRequest request, HttpServletResponse response, ViewModel viewModel) throws ServletException, IOException {
        viewModel.setTemplate("projects:create_project.html");
        Map<String, Object> result = new HashMap<>();
        result.put("projectName", request.getParameter("projectName"));


        try {
            this.projectService.createProject(SecurityContext.getCurrentUser(request.getSession()), result.get("projectName").toString());
            response.sendRedirect("/projects");
        } catch (BusinessException e) {
            result.put("error", "Project " + result.get("projectName").toString() + " already exist");
        }

        viewModel.getViewDatas().putAll(result);
    }

    @Override
    protected void handleGet(HttpServletRequest request, HttpServletResponse response, ViewModel viewModel) throws ServletException, IOException {
        viewModel.setTemplate("projects:create_project.html");

    }

}
