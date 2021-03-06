package timeboard.projects;

/*-
 * #%L
 * projects
 * %%
 * Copyright (C) 2019 - 2020 Timeboard
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


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.ui.Model;
import timeboard.core.model.Project;
import timeboard.core.security.TimeboardAuthentication;
import timeboard.projects.api.ProjectNavigationProvider;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public abstract class ProjectBaseController {

    private static final String NAVIGATION_PROVIDERS = "projectNavProviders";

    @Autowired
    private PermissionEvaluator permissionEvaluator;

    @Autowired(required = false)
    private List<ProjectNavigationProvider> navigationProviderList;

    public List<ProjectNavigationProvider> getNavigationProviderList(TimeboardAuthentication authentication, Project project) {
        this.navigationProviderList.sort(Comparator.comparing(o -> (Integer) o.getNavigationWeight()));

        return navigationProviderList.stream()
                .filter(projectNavigationProvider ->
                        permissionEvaluator
                                .hasPermission(authentication, project, projectNavigationProvider.getNavigationAction()))
                .collect(Collectors.toList());
    }

    public void setNavigationProviderList(final List<ProjectNavigationProvider> navigationProviderList) {
        this.navigationProviderList = navigationProviderList;
    }

    protected void initModel(Model model, TimeboardAuthentication auth, Project project) {
        model.addAttribute(NAVIGATION_PROVIDERS, this.getNavigationProviderList(auth, project));
    }
}
