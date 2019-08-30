package kronops.core.service;

import kronops.apigenerator.annotation.RPCEndpoint;
import kronops.apigenerator.annotation.RPCMethod;
import kronops.apigenerator.annotation.RPCParam;
import kronops.core.model.Project;
import org.osgi.service.component.annotations.Component;

import java.util.ArrayList;
import java.util.List;

@RPCEndpoint
@Component(
        service = ProjectServiceBPImpl.class,
        immediate = true
)
public class ProjectServiceBPImpl implements kronops.core.api.ProjectServiceBP {

    @Override
    @RPCMethod()
    public Project saveProject(@RPCParam("project") Project project){
        return project;
    }

    @Override
    @RPCMethod(returnListOf = Project.class)
    public List<Project> getProjects(){
        return new ArrayList<>();
    }

    @Override
    @RPCMethod()
    public Project getProject(@RPCParam("projectId") Long projectId){
        return null;
    }

}
