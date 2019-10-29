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

import org.eclipse.egit.github.core.client.RequestException;
import timeboard.core.api.ProjectExportService;
import timeboard.core.api.ProjectImportService;
import timeboard.core.api.ProjectService;
import timeboard.core.api.exceptions.BusinessException;
import timeboard.core.model.*;
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
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

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
    public String importTasksToProject(User actor, long projectID) throws BusinessException {

        final AtomicInteger nbTaskCreated = new AtomicInteger(0);
        final AtomicInteger nbTaskUpdated = new AtomicInteger(0);
        final AtomicInteger nbTaskRemoved = new AtomicInteger(0);

        try {
            final Project targetProject = this.projectService.getProjectByID(actor, projectID);

            final ProjectAttributValue githubOAuthToken = targetProject.getAttributes().get(GITHUB_TOKEN_KEY);
            if(githubOAuthToken == null || githubOAuthToken.getValue()== null){
                throw new BusinessException("Missing "+GITHUB_TOKEN_KEY+" in project configuration");
            }

            final ProjectAttributValue githubRepoOwner = targetProject.getAttributes().get(GITHUB_REPO_OWNER_KEY);
            if(githubRepoOwner == null || githubRepoOwner.getValue() == null){
                throw new BusinessException("Missing "+GITHUB_REPO_OWNER_KEY+" in project configuration");
            }

            final ProjectAttributValue githubRepoName = targetProject.getAttributes().get(GITHUB_REPO_NAME_KEY);
            if(githubRepoName == null || githubRepoName.getValue() == null){
                throw new BusinessException("Missing "+GITHUB_REPO_NAME_KEY+" in project configuration");
            }

            try {

                RepositoryId repositoryId = new RepositoryId(githubRepoOwner.getValue(), githubRepoName.getValue());

                IssueService issueService = new IssueService();
                issueService.getClient().setOAuth2Token(githubOAuthToken.getValue());
                List<Issue> issues = issueService.getIssues(repositoryId, new HashMap<>());

                Map<Long, Task> existingTasks = this.projectService.searchExistingTasksFromOrigin(targetProject, GITHUB_ORIGIN_KEY, githubRepoOwner.getValue() + "/" + githubRepoName.getValue());

                issues.stream().forEach(issue -> {
                    if (!existingTasks.containsKey(issue.getId())) {
                        // task does not exist, so create it
                        Task t = this.projectService.createTask(actor, targetProject, issue.getTitle(),
                                issue.getBodyHtml(), issue.getCreatedAt(), issue.getClosedAt(),
                                0, null, null,
                                GITHUB_ORIGIN_KEY, githubRepoOwner.getValue() + "/" + githubRepoName.getValue(), issue.getId());
                        nbTaskCreated.incrementAndGet();
                    } else {
                        // task already exist, so update it
                        Task task = existingTasks.get(issue.getId());
                        final TaskRevision latestRevision = task.getLatestRevision();
                        TaskRevision revision = new TaskRevision(actor,
                                task,
                                issue.getTitle(),
                                issue.getBodyHtml(),
                                issue.getCreatedAt(),
                                issue.getClosedAt(),
                                latestRevision.getEstimateWork(),
                                latestRevision.getRemainsToBeDone(),
                                latestRevision.getAssigned());
                        this.projectService.updateTask(actor, task, revision);
                        existingTasks.remove(task.getRemoteId(), task); //remove task in existing list to found the deleted at the end
                        nbTaskUpdated.incrementAndGet();
                    }

                });

                // Deleted task
                for (Task task : existingTasks.values()) { //remaining task have been delete from origin repository
                    this.projectService.deleteTaskByID(actor, task.getId()); //so delete it to be synchronized with origin
                    nbTaskRemoved.incrementAndGet();
                }

                return "<ul>" +
                        "<li>" + nbTaskCreated + " tasks created</li>" +
                        "<li>" + nbTaskUpdated + " tasks updated</li>" +
                        "<li>" + nbTaskRemoved + " tasks removed</li>" +
                        "</ul>";
            } catch (RequestException e) {
                throw new BusinessException("Github configuration is incorrect.");
            }
        } catch (IOException e) {
            throw new BusinessException(e);
        }
    }
}
