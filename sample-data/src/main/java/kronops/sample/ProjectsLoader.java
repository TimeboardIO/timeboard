package kronops.sample;

/*-
 * #%L
 * sample-data
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
import kronops.core.api.UserServiceBP;
import kronops.core.api.exceptions.BusinessException;
import kronops.core.model.*;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.Date;

@Component(
        service = ProjectsLoader.class,
        immediate = true
)
public class ProjectsLoader {

    @Reference
    ProjectServiceBP projectServiceBP;

    @Reference
    UserLoader userLoader;


    @Reference
    UserServiceBP userServiceBP;


    @Activate
    public void load() throws BusinessException {
        User u1 = this.userServiceBP.findUserByLogin("kronops1");
        User u2 = this.userServiceBP.findUserByLogin("kronops2");

        Project newProject = this.projectServiceBP.createProject(u1, "Test Project");

        Task t = new Task();
        t.setName("Hello Task");
        t.setStartDate(new Date());
        t.setEndDate(new Date());
        t.setComments("This is a sample task");
        t.setEstimateWork(5);
        this.projectServiceBP.createTask(newProject, t);

        Task t2 = new Task();
        t2.setEstimateWork(1);
        t2.setName("Do stuff");
        t2.setStartDate(new Date());
        t2.setEndDate(new Date());
        t2.setComments("This is another sample task");
        this.projectServiceBP.createTask(newProject, t2);

        ProjectMembership projectMembership = new ProjectMembership(newProject, u2, ProjectRole.CONTRIBUTOR);

        this.projectServiceBP.save(projectMembership);


        ProjectCluster root = new ProjectCluster();
        root.setName("Root Cluster");

        ProjectCluster customer = new ProjectCluster();
        customer.setName("Customer XYZ Cluster");

        this.projectServiceBP.saveProjectCluster(root);
        customer.setParent(root);
        this.projectServiceBP.saveProjectCluster(customer);

        this.projectServiceBP.addProjectToProjectCluster(customer, newProject);

    }


}
