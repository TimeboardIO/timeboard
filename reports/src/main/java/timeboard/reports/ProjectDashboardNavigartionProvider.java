package timeboard.reports;

import org.springframework.stereotype.Component;
import timeboard.projects.api.ProjectNavigationProvider;

@Component
public class ProjectDashboardNavigartionProvider implements ProjectNavigationProvider {

    @Override
    public String getNavigationLabel() {
        return "report.project.dashboard";
    }

    @Override
    public String getNavigationPath() {
        return ProjectDashboardController.PATH;
    }


}
