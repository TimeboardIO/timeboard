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

import kronops.core.api.ProjectService;
import kronops.core.model.ProjectCluster;
import kronops.core.ui.KronopsServlet;
import kronops.core.ui.ViewModel;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component(
        service = Servlet.class,
        scope = ServiceScope.PROTOTYPE,
        property = {
                "osgi.http.whiteboard.servlet.pattern=/clusters/*",
                "osgi.http.whiteboard.context.select=(osgi.http.whiteboard.context.name=kronops)"
        }
)
public class ClustersServlet extends KronopsServlet {

    @Reference
    private ProjectService projectService;

    @Override
    protected ClassLoader getTemplateResolutionClassLoader() {
        return ClustersServlet.class.getClassLoader();
    }

    @Override
    protected void handlePost(HttpServletRequest request, HttpServletResponse response, ViewModel viewModel) throws ServletException, IOException {
        viewModel.setTemplate("clusters.html");
        Map<String, Object> datas = this.prepareData();

        String action = request.getParameter("action");

        if (action.equals("create")) {
            String clusterName = request.getParameter("clusterName");
            ProjectCluster pc = new ProjectCluster();
            pc.setName(clusterName);
            this.projectService.saveProjectCluster(pc);
        }

        if (action.equals("update")) {

            final Map<Long, String> clusterName = new HashMap<>();
            final Map<Long, Long> clusterParent = new HashMap<>();

            request.getParameterMap().forEach((k, v) -> {
                if (k.startsWith("clustername")) {
                    Long id = getID(k);
                    clusterName.put(id, v[0]);
                }
                if (k.startsWith("clusterparent")) {
                    Long id = getID(k);
                    String value = v[0];
                    if (!value.isEmpty()) {
                        clusterParent.put(id, Long.valueOf(value));
                    }
                }
            });

            List<ProjectCluster> updatedProjectCluster = new ArrayList<>();
            clusterName.forEach((aLong, s) -> {
                ProjectCluster pc = new ProjectCluster();
                pc.setId(aLong);
                pc.setName(s);
                updatedProjectCluster.add(pc);
            });

            this.projectService.updateProjectClusters(updatedProjectCluster, clusterParent);

        }

        response.sendRedirect("/clusters");

    }

    @Override
    protected void handleGet(HttpServletRequest request, HttpServletResponse response, ViewModel viewModel) throws ServletException, IOException {
        viewModel.setTemplate("clusters.html");
        if (request.getRequestURI().equals("/clusters/delete")) {
            deleteCluster(Long.parseLong(request.getParameter("projectID")));
        }
        viewModel.getViewDatas().putAll(prepareData());
    }


    private void deleteCluster(Long clusterID) {
        this.projectService.deleteProjectClusterByID(clusterID);
    }


    private Long getID(String k) {
        return Long.valueOf(k.substring(k.indexOf("[") + 1, k.indexOf("]")));
    }

    private Map<String, Object> prepareData() {
        final Map<String, Object> templateDatas = new HashMap<>();
        List<ProjectCluster> clusters = this.projectService.listProjectClusters();
        templateDatas.put("clusters", clusters);
        return templateDatas;
    }
}
