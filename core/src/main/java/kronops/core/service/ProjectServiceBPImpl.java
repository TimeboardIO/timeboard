package kronops.core.service;

/*-
 * #%L
 * core
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

import kronops.core.api.ProjectDAO;
import kronops.core.api.ProjectServiceBP;
import kronops.core.model.Project;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.Date;
import java.util.List;

@Component(
        service = ProjectServiceBP.class,
        immediate = true
)
public class ProjectServiceBPImpl implements ProjectServiceBP {

    @Reference
    public ProjectDAO projectDAO;

    @Override
    public Project createProject(String projectName, String projectType) {

        Project newProject = new Project();
        newProject.setName(projectName);
        newProject.setType(projectType);
        newProject.setStartDate(new Date());

        this.projectDAO.save(newProject);

        return newProject;
    }

    @Override
    public Project saveProject(Project project){
        return this.projectDAO.save(project);
    }

    @Override
    public List<Project> getProjects(){
        return this.projectDAO.findAll();
    }

    @Override
    public Project getProject(Long projectId){
        return this.projectDAO.findById(projectId);
    }

    @Override
    public Project deleteProjectByID(Long projectID) {
        final Project project = this.getProject(projectID);
        this.projectDAO.delete(project);
        return project;
    }

}
