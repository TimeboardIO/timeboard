package kronops.core.api;

import kronops.core.model.Project;

import java.util.List;

public interface ProjectServiceBP {
     Project saveProject( Project project);

     List<Project> getProjects();

     Project getProject(  Long projectId);
}
