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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import kronops.core.api.ProjectServiceBP;
import kronops.core.model.Project;
import kronops.core.model.Task;
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
import java.text.SimpleDateFormat;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Component(
        service = Servlet.class,
        scope = ServiceScope.PROTOTYPE,
        property = {
                "osgi.http.whiteboard.servlet.pattern=/projects/blueprint",
                "osgi.http.whiteboard.context.select=(osgi.http.whiteboard.context.name=kronops)"
        }
)
public class ProjectBlueprintServlet extends KronopsServlet {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Reference
    private ProjectServiceBP projectServiceBP;

    @Override
    protected ClassLoader getTemplateResolutionClassLoader() {
        return ProjectBlueprintServlet.class.getClassLoader();
    }


    @Override
    protected void handleGet(HttpServletRequest request, HttpServletResponse response, ViewModel viewModel) throws ServletException, IOException {
        long id = Long.parseLong(request.getParameter("projectID"));

        Project project = this.projectServiceBP.getProject(id);

        List<Task> tasks = this.projectServiceBP.listProjectTasks(project);

        List<String> data = tasks.stream().map(task -> {
            GantTaskStruct struct = new GantTaskStruct();
            struct.setId(String.valueOf(task.getId()));
            struct.setName(task.getName());
            struct.setStart(task.getStartDate());
            struct.setEnd(task.getEndDate());
            struct.setProgress(20);
            return struct;
        })
                .map(gantTaskStruct -> {
                    String res = null;
                    try {
                        res = MAPPER.writeValueAsString(gantTaskStruct);
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                    }
                    return res;
                })
                .collect(Collectors.toList());

        viewModel.getViewDatas().put("project", project);
        viewModel.getViewDatas().put("tasks", data);

        viewModel.setTemplate("blueprint.html");

    }


    public static class GantTaskStruct {

        private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

        private String id;
        private String name;
        private Date start;
        private Date end;
        private double progress;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getStart() {
            return DATE_FORMAT.format(start);
        }

        public void setStart(Date start) {
            this.start = start;
        }

        public String getEnd() {
            return DATE_FORMAT.format(end);
        }

        public void setEnd(Date end) {
            this.end = end;
        }

        public double getProgress() {
            return progress;
        }

        public void setProgress(double progress) {
            this.progress = progress;
        }
    }


}
