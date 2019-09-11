package kronops.projects;

/*-
 * #%L
 * webui
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

import kronops.core.api.ProjectServiceBP;
import kronops.core.api.exceptions.BusinessException;
import kronops.core.api.TreeNode;
import kronops.core.model.Project;
import kronops.core.model.ProjectRole;
import kronops.core.ui.KronopsServlet;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;


/**
 * Display project details form
 * <p>
 * ex : /projects/details?id=
 */
@Component(
        service = Servlet.class,
        scope = ServiceScope.PROTOTYPE,
        property = {
                "osgi.http.whiteboard.servlet.pattern=/projects/details",
                "osgi.http.whiteboard.context.select=(osgi.http.whiteboard.context.name=kronops)"
        }
)
public class DetailsProjectServlet extends KronopsServlet {

    @Reference
    public ProjectServiceBP projectServiceBP;

    @Override
    protected String getTemplate(String path) {
        return "details_project.html";
    }

    @Override
    protected ClassLoader getTemplateResolutionClassLoader() {
        return DetailsProjectServlet.class.getClassLoader();
    }

    @Override
    protected Map<String, Object> handleGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        long id = Long.parseLong(request.getParameter("id"));

        Project project = this.projectServiceBP.getProject(id);


        Map<String, Object> map = new HashMap<>();
        prepareTemplateData(project, map);

        return map;
    }

    private void prepareTemplateData(Project project, Map<String, Object> map) {
        TreeNode node = this.projectServiceBP.listProjectCluster();

        map.put("clusters", node.getPaths());
        map.put("project", project);
        map.put("members", project.getMembers());
        map.put("roles", ProjectRole.values());
    }

    @Override
    protected Map<String, Object> handlePost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Map<String, Object> map = new HashMap<>();

        long id = Long.parseLong(request.getParameter("id"));


        Map<Long, ProjectRole> memberships = new HashMap<>();

        //Extract memberships from request
        Enumeration<String> params = request.getParameterNames();
        while (params.hasMoreElements()) {
            String param = params.nextElement();
            if (param.startsWith("members")) {
                String key = param.substring(param.indexOf('[') + 1, param.indexOf(']'));
                String value = request.getParameter(param);
                if (!value.isEmpty()) {
                    memberships.put(Long.parseLong(key), ProjectRole.valueOf(value));
                } else {
                    memberships.put(Long.parseLong(key), ProjectRole.CONTRIBUTOR);
                }
            }
        }
        Project project = this.projectServiceBP.getProject(id);
        project.setName(request.getParameter("projectName"));

        try {
            this.projectServiceBP.updateProject(project, memberships);
        } catch (BusinessException e) {
            map.put("error", e.getLocalizedMessage());
        }

        prepareTemplateData(project, map);


        return map;
    }
}
