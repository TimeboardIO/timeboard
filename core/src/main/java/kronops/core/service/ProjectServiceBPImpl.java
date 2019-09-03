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

import kronops.apigenerator.annotation.RPCEndpoint;
import kronops.apigenerator.annotation.RPCMethod;
import kronops.apigenerator.annotation.RPCParam;
import kronops.core.api.ProjectDAO;
import kronops.core.api.ProjectServiceBP;
import kronops.core.model.Project;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.List;

@RPCEndpoint
@Component(
        service = ProjectServiceBP.class,
        immediate = true
)
public class ProjectServiceBPImpl implements ProjectServiceBP {

    @Reference
    public ProjectDAO projectDAO;

    @Override
    @RPCMethod()
    public Project saveProject(@RPCParam("project") Project project){
        return this.projectDAO.save(project);
    }

    @Override
    @RPCMethod(returnListOf = Project.class)
    public List<Project> getProjects(){
        return this.projectDAO.findAll();
    }

    @Override
    @RPCMethod()
    public Project getProject(@RPCParam("projectId") Long projectId){
        return null;
    }

}
