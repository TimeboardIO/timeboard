package kronops.core;

import kronops.core.api.ProjectDAO;
import kronops.core.model.Project;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(immediate = true)
public class Activator {

    @Reference
    ProjectDAO projectDAO;

    @Activate
    public void init(){
        for(int i=0; i<10; i++){
            Project project = new Project();
            project.setName("HelloWorld "+i);
            this.projectDAO.save(project);
        }
    }
}
