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

import timeboard.core.api.EncryptionService;
import timeboard.core.api.ProjectExportService;
import timeboard.core.api.ProjectImportService;
import timeboard.core.api.ProjectService;
import timeboard.core.model.Project;
import timeboard.core.model.ProjectAttributValue;
import timeboard.core.model.ProjectRole;
import timeboard.core.ui.TimeboardServlet;
import timeboard.core.ui.ViewModel;
import timeboard.security.SecurityContext;
import org.osgi.service.component.annotations.*;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
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
                "osgi.http.whiteboard.servlet.pattern=/projects/config",
                "osgi.http.whiteboard.context.select=(osgi.http.whiteboard.context.name=timeboard)"
        }
)
public class ProjectConfigServlet extends TimeboardServlet {

    @Reference(
            policyOption = ReferencePolicyOption.GREEDY,
            cardinality = ReferenceCardinality.MULTIPLE,
            collectionType = CollectionType.SERVICE
    )
    private List<ProjectExportService> projectExportServices;

    @Reference(
            policyOption = ReferencePolicyOption.GREEDY,
            cardinality = ReferenceCardinality.MULTIPLE,
            collectionType = CollectionType.SERVICE
    )
    private List<ProjectImportService> projectImportServices;

    @Reference
    public ProjectService projectService;

    @Reference
    public EncryptionService encryptionService;

    @Override
    protected ClassLoader getTemplateResolutionClassLoader() {
        return ProjectConfigServlet.class.getClassLoader();
    }


    private void prepareTemplateData(Project project, Map<String, Object> map) {

        map.put("project", project);
        map.put("members", project.getMembers());
        map.put("roles", ProjectRole.values());
        map.put("exports", this.projectExportServices);
        map.put("imports", this.projectImportServices);
        map.put("tasks", this.projectService.listProjectTasks(project));
    }


    @Override
    protected void handleGet(HttpServletRequest request, HttpServletResponse response, ViewModel viewModel) throws ServletException, IOException {
        viewModel.setTemplate("projects:details_project_config.html");
        long id = Long.parseLong(request.getParameter("projectID"));

        Project project = this.projectService.getProjectByID(SecurityContext.getCurrentUser(request), id);


        Map<String, Object> map = new HashMap<>();
        prepareTemplateData(project, map);
        viewModel.getViewDatas().putAll(map);
    }

    @Override
    protected void handlePost(HttpServletRequest request, HttpServletResponse response, ViewModel viewModel) throws Exception {
        viewModel.setTemplate("projects:details_project_config.html");
        Map<String, Object> map = new HashMap<>();

        //Extract project
        long id = Long.parseLong(request.getParameter("projectID"));
        Project project = this.projectService.getProjectByID(SecurityContext.getCurrentUser(request), id);
        project.setName(request.getParameter("projectName"));
        project.setComments(request.getParameter("projectDescription"));
        project.setQuotation(Double.parseDouble(request.getParameter("projectQuotation")));

        //Extract project configuration
        project.getAttributes().clear();

        //new attributes
        String newAttrKey = request.getParameter("newAttrKey");
        String newAttrValue = request.getParameter("newAttrValue");
        Boolean newAttrEncrypted = false;
        if (request.getParameter("newAttrEncrypted") != null && request.getParameter("newAttrEncrypted").equals("on")) {
            newAttrEncrypted = true;
        }

        if (!newAttrKey.isEmpty()) {
            if(newAttrEncrypted){
                newAttrValue = this.encryptionService.encryptAttribute(newAttrValue);
            }
            project.getAttributes().put(newAttrKey, new ProjectAttributValue(newAttrValue, newAttrEncrypted));
        }

        //Attribute update
        Enumeration<String> params1 = request.getParameterNames();
        while (params1.hasMoreElements()) {
            String param = params1.nextElement();
            if (param.startsWith("attr-")) {
                String key = param.substring(5, param.length());
                String value = request.getParameter(param);
                project.getAttributes().put(key, new ProjectAttributValue(value));
            }
            if (param.startsWith("attrenc-")) {
                String key = param.substring(8, param.length());
                String encrypted = request.getParameter(param);
                project.getAttributes().get(key).setEncrypted(true);
              //  project.getAttributes().get(key).setEncrypted(Boolean.getBoolean(encrypted));
            }
        }

        //Extract memberships from request
        Map<Long, ProjectRole> memberships = new HashMap<>();
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

        this.projectService.updateProject(project, memberships);

        prepareTemplateData(project, map);

        viewModel.getViewDatas().putAll(map);
    }
}
