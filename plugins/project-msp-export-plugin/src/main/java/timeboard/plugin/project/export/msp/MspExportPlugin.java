package timeboard.plugin.project.export.msp;

/*-
 * #%L
 * project-export-plugin
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

import net.sf.mpxj.ProjectFile;
import net.sf.mpxj.Task;
import net.sf.mpxj.mpx.MPXWriter;
import org.osgi.service.component.annotations.Component;
import org.springframework.beans.factory.annotation.Autowired;
import timeboard.core.api.ProjectExportService;
import timeboard.core.api.ProjectService;
import timeboard.core.api.exceptions.BusinessException;
import timeboard.core.model.Project;
import timeboard.core.model.User;

import java.io.IOException;
import java.io.OutputStream;

@Component(
        service = ProjectExportService.class,
        immediate = true
)
public class MspExportPlugin implements ProjectExportService {


    @Autowired
    private ProjectService projectService;

    @Override
    public String getMimeType() {
        return "application/vnd.ms-project";
    }

    @Override
    public String getName() {
        return "Microsoft Project";
    }

    @Override
    public void export(User actor, long projectID, OutputStream output) throws IOException, BusinessException {

        final Project project = this.projectService.getProjectByID(actor, projectID);
        MPXWriter writer = new MPXWriter();
        ProjectFile projectFile = new ProjectFile();

        this.projectService.listProjectTasks(actor, project).forEach(task -> {
            Task mspTask = projectFile.addTask();
            mspTask.setName(task.getName());
            mspTask.setStart(task.getStartDate());
            mspTask.setStop(task.getEndDate());
            mspTask.setBaselineCost(task.getOriginalEstimate());
        });

        writer.write(projectFile, output);

    }

    @Override
    public String getExtension() {
        return "mpp";
    }
}
