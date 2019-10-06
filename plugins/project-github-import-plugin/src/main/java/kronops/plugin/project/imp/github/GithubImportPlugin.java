package kronops.plugin.project.imp.github;

/*-
 * #%L
 * project-export-plugin
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

import kronops.core.api.ProjectExportService;
import kronops.core.api.ProjectImportService;
import kronops.core.api.ProjectService;
import kronops.core.api.exceptions.BusinessException;
import kronops.core.model.Project;
import kronops.core.model.Task;
import kronops.core.model.User;
import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.RepositoryId;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.IssueService;
import org.eclipse.egit.github.core.service.RepositoryService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

@Component(
        service = ProjectImportService.class,
        immediate = true
)
public class GithubImportPlugin implements ProjectImportService {

    private static final String GITHUB_TOKEN_KEY = "github.token";
    private static final String GITHUB_REPO_OWNER_KEY = "github.repo.owner";
    private static final String GITHUB_REPO_NAME_KEY = "github.repo.name";

    @Reference
    private ProjectService projectService;

    @Override
    public String getServiceName() {
        return "Github issues";
    }

    @Override
    public void importTasksToProject(User actor, long projectID) throws BusinessException {
        try {
            final Project targetProject = this.projectService.getProjectByID(actor, projectID);

            final String githubOAuthToken = targetProject.getArguments().get(GITHUB_TOKEN_KEY);
            if(githubOAuthToken == null){
                throw new BusinessException("Missing "+GITHUB_TOKEN_KEY+" in project configuration");
            }

            final String githubRepoOwner = targetProject.getArguments().get(GITHUB_REPO_OWNER_KEY);
            if(githubRepoOwner == null){
                throw new BusinessException("Missing "+GITHUB_REPO_OWNER_KEY+" in project configuration");
            }

            final String githubRepoName = targetProject.getArguments().get(GITHUB_REPO_NAME_KEY);
            if(githubRepoName == null){
                throw new BusinessException("Missing "+GITHUB_REPO_NAME_KEY+" in project configuration");
            }

            RepositoryId repositoryId = new RepositoryId(githubRepoOwner, githubRepoName);

            IssueService issueService = new IssueService();
            issueService.getClient().setOAuth2Token(githubOAuthToken);
            List<Issue> issues = issueService.getIssues(repositoryId, new HashMap<>());

            issues.stream().forEach(issue -> {

                Task taskFromGithub = new Task();
                taskFromGithub.setName(issue.getTitle());
                taskFromGithub.setComments(issue.getUrl());
                taskFromGithub.setStartDate(issue.getCreatedAt());

                this.projectService.createTask(targetProject, taskFromGithub);
            });


        } catch (IOException e) {
            throw new BusinessException(e);
        }
    }
}
