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


@Component(
        service = Servlet.class,
        scope = ServiceScope.PROTOTYPE,
        property = {
                "osgi.http.whiteboard.servlet.pattern=/projects/tasks/delete",
                "osgi.http.whiteboard.context.select=(osgi.http.whiteboard.context.name=kronops)"
        }
)
public class ProjectTaskDeleteServlet extends KronopsServlet {


    @Reference
    public ProjectServiceBP projectServiceBP;


    @Override
    protected ClassLoader getTemplateResolutionClassLoader() {
        return ProjectTaskDeleteServlet.class.getClassLoader();
    }

    @Override
    protected void handleGet(HttpServletRequest request, HttpServletResponse response, ViewModel viewModel) throws ServletException, IOException {
        long taskID = Long.parseLong(request.getParameter("taskID"));
        try {
            this.projectServiceBP.deleteTaskByID(taskID);
        } catch (BusinessException e) {
            viewModel.getErrors().add(e);
        }

        viewModel.setTemplate("details_project_tasks.html");
    }




}
