package kronops.core.service;

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
