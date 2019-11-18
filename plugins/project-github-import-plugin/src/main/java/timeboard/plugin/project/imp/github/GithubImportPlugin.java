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
import timeboard.core.api.*;
import timeboard.core.api.exceptions.BusinessException;
import timeboard.core.model.*;
import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.RepositoryId;
import org.eclipse.egit.github.core.service.IssueService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.io.IOException;
import java.rmi.Remote;
import java.util.*;
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
    private static final String GITHUB_USER_FIELD = GITHUB_ORIGIN_KEY;

    private static final List<String> GITHUB_USER_FIELDS = new ArrayList<>();

    @Reference
    private ProjectService projectService;

    @Reference
    private UserService userService;

    @Reference
    public EncryptionService encryptionService;


    @Override
    public String getServiceName() {
        return "Github Issues";
    }

    @Override
    public List<String> getRequiredUserFields() {
        return Arrays.asList(GITHUB_TOKEN_KEY, GITHUB_REPO_OWNER_KEY, GITHUB_REPO_NAME_KEY);

    }

    @Override
    public List<RemoteTask> getRemoteTasks(User currentUser, long projectID) {
        final Project targetProject = this.projectService.getProjectByID(currentUser, projectID);
        final List<RemoteTask> remoteTasks = new ArrayList<>();

        try {//handle github connexion issues

            final String githubOAuthToken = encryptionService.getProjectAttribute(targetProject, GITHUB_TOKEN_KEY);
            if(githubOAuthToken == null || githubOAuthToken.equals("")){
                throw new BusinessException("Missing "+GITHUB_TOKEN_KEY+" in project configuration");
            }

            final String githubRepoOwner = encryptionService.getProjectAttribute(targetProject, GITHUB_REPO_OWNER_KEY);
            if(githubRepoOwner == null || githubRepoOwner.equals("")){
                throw new BusinessException("Missing "+GITHUB_REPO_OWNER_KEY+" in project configuration");
            }

            final String githubRepoName = encryptionService.getProjectAttribute(targetProject, GITHUB_REPO_NAME_KEY);
            if(githubRepoName == null || githubRepoName.equals("")){
                throw new BusinessException("Missing "+GITHUB_REPO_NAME_KEY+" in project configuration");
            }

            RepositoryId repositoryId = new RepositoryId(githubRepoOwner, githubRepoName);

            IssueService issueService = new IssueService();
            issueService.getClient().setOAuth2Token(githubOAuthToken);
            List<Issue> issues = issueService.getIssues(repositoryId, new HashMap<>());

            issues.stream().forEach(issue -> {

                RemoteTask rt = new RemoteTask();
                if(issue.getAssignee() != null) {
                    rt.setUserName(issue.getAssignee().getLogin());
                }
                rt.setTitle(issue.getTitle());
                rt.setComments(issue.getBodyHtml());
                rt.setStopDate(issue.getClosedAt());
                rt.setStartDate(issue.getCreatedAt());
                remoteTasks.add(rt);
            });
        }catch (Exception e){
            e.printStackTrace();
        }

        return remoteTasks;
    }
}
