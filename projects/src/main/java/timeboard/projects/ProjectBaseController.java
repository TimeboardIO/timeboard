package timeboard.projects;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import timeboard.projects.api.ProjectNavigationProvider;

import java.util.List;

public abstract class ProjectBaseController  {

    private static final String NAVIGATION_PROVIDERS = "projectNavProviders";

    @Autowired(required = false)
    protected List<ProjectNavigationProvider> navigationProviderList;


    protected void initModel(Model model){
        model.addAttribute(NAVIGATION_PROVIDERS, this.navigationProviderList);
    }
}
