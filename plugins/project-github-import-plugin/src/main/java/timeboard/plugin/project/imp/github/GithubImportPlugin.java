package timeboard.plugin.project.imp.github;

/*-
 * #%L
 * project-export-plugin
 * %%
 * Copyright (C) 2019 Timeboard
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

import timeboard.core.api.ProjectExportService;
import timeboard.core.api.ProjectImportService;
import timeboard.core.api.ProjectService;
import timeboard.core.api.exceptions.BusinessException;
import timeboard.core.model.Project;
import timeboard.core.model.ProjectAttributValue;
import timeboard.core.model.Task;
import timeboard.core.model.User;
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

    private static final String GITHUB_ORIGIN_KEY = "github";

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

            final String githubOAuthToken = targetProject.getAttributes().get(GITHUB_TOKEN_KEY).getValue();
            if(githubOAuthToken == null){
                throw new BusinessException("Missing "+GITHUB_TOKEN_KEY+" in project configuration");
            }

            final String githubRepoOwner = targetProject.getAttributes().get(GITHUB_REPO_OWNER_KEY).getValue();
            if(githubRepoOwner == null){
                throw new BusinessException("Missing "+GITHUB_REPO_OWNER_KEY+" in project configuration");
            }

            final String githubRepoName = targetProject.getAttributes().get(GITHUB_REPO_NAME_KEY).getValue();
            if(githubRepoName == null){
                throw new BusinessException("Missing "+GITHUB_REPO_NAME_KEY+" in project configuration");
            }

            RepositoryId repositoryId = new RepositoryId(githubRepoOwner, githubRepoName);

            IssueService issueService = new IssueService();
            issueService.getClient().setOAuth2Token(githubOAuthToken);
            List<Issue> issues = issueService.getIssues(repositoryId, new HashMap<>());

            issues.stream().forEach(issue -> {
                Task t = this.projectService.createTask(actor, targetProject, issue.getTitle(),
                        issue.getUrl(), issue.getCreatedAt(), issue.getClosedAt(),
                        0, null, null,
                        GITHUB_ORIGIN_KEY, githubRepoOwner+"/"+githubRepoName, issue.getId());
            });

        } catch (IOException e) {
            throw new BusinessException(e);
        }
    }
}
