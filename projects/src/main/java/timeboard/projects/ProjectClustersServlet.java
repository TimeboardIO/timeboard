package timeboard.projects;

/*-
 * #%L
 * projects
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
import timeboard.core.api.TreeNode;
import timeboard.core.model.Project;
import timeboard.core.ui.TimeboardServlet;
import timeboard.core.ui.ViewModel;
import timeboard.security.SecurityContext;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component(
        service = Servlet.class,
        scope = ServiceScope.PROTOTYPE,
        property = {
                "osgi.http.whiteboard.servlet.pattern=/projects/clusters",
                "osgi.http.whiteboard.context.select=(osgi.http.whiteboard.context.name=timeboard)"
        }
)
public class ProjectClustersServlet extends TimeboardServlet {


    @Reference
    public ProjectService projectService;


    @Override
    protected ClassLoader getTemplateResolutionClassLoader() {
        return ProjectClustersServlet.class.getClassLoader();
    }

    @Override
    protected void handleGet(HttpServletRequest request, HttpServletResponse response, ViewModel viewModel) throws ServletException, IOException {
        if (request.getParameter("projectID") != null) {
            final Project project = this.projectService.getProjectByID(SecurityContext.getCurrentUser(request), Long.parseLong(request.getParameter("projectID")));

            prepareDatas(viewModel, project);

        }

        viewModel.setTemplate("projects:details_project_cluster_config.html");
    }

    private void prepareDatas(ViewModel viewModel, Project project) {
        viewModel.getViewDatas().put("project", project);

        final List<TreeNode> node = this.projectService.computeClustersTree();
        final Map<Long, String> paths = new HashMap<>();
        node.forEach(treeNode -> {
            paths.putAll(treeNode.getPaths());
        });

        viewModel.getViewDatas().put("selected_clusters", project.getClusters().stream().map(projectCluster -> projectCluster.getId()).collect(Collectors.toList()));
        viewModel.getViewDatas().put("clusters", paths);
    }

    @Override
    protected void handlePost(HttpServletRequest request, HttpServletResponse response, ViewModel viewModel) throws Exception {

        final Project project = this.projectService.getProjectByID(SecurityContext.getCurrentUser(request), Long.parseLong(request.getParameter("projectID")));

        //Extract cluster
        String[] clusterID = request.getParameterValues("cluster");
        project.getClusters().clear();
        if (clusterID != null) {
            Arrays.asList(clusterID).stream().forEach(s -> {
                project.getClusters().add(this.projectService.findProjectsClusterByID(Long.parseLong(s)));
            });
        }

        this.projectService.updateProject(project);

        prepareDatas(viewModel, project);

        viewModel.setTemplate("projects:details_project_cluster_config.html");

    }
}
