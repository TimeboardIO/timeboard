package timeboard.plugin.project.sync.github;

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

import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.RepositoryId;
import org.eclipse.egit.github.core.service.IssueService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import timeboard.core.api.sync.ProjectSyncCredentialField;
import timeboard.core.api.sync.ProjectSyncPlugin;
import timeboard.core.api.sync.RemoteTask;
import timeboard.core.model.Account;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;


@Component
public class GithubImportPlugin implements ProjectSyncPlugin {

    private static final Logger LOGGER = LoggerFactory.getLogger(GithubImportPlugin.class);

    private static final String GITHUB_TOKEN_KEY = "github.token";
    private static final String GITHUB_REPO_OWNER_KEY = "github.repo.owner";
    private static final String GITHUB_REPO_NAME_KEY = "github.repo.name";
    public static final List<ProjectSyncCredentialField> FIELDS = Arrays.asList(
            new ProjectSyncCredentialField(GITHUB_TOKEN_KEY, "Github token", ProjectSyncCredentialField.Type.TEXT, 0),
            new ProjectSyncCredentialField(GITHUB_REPO_OWNER_KEY, "Github repo owner", ProjectSyncCredentialField.Type.TEXT, 1),
            new ProjectSyncCredentialField(GITHUB_REPO_NAME_KEY, "Github repo name", ProjectSyncCredentialField.Type.TEXT, 2)
    );

    @Override
    public String getServiceName() {
        return "Github Issues";
    }

    @Override
    public List<ProjectSyncCredentialField> getSyncCredentialFields() {
        return FIELDS;
    }


    @Override
    public List<RemoteTask> getRemoteTasks(final Account currentUser, final List<ProjectSyncCredentialField> credentials) {

        final List<RemoteTask> remoteTasks = new ArrayList<>();

        try {//handle github connexion issues

            final String githubRepoOwner = getFieldByKey(credentials, GITHUB_REPO_OWNER_KEY).getValue();
            final String githubRepoName = getFieldByKey(credentials, GITHUB_REPO_NAME_KEY).getValue();
            final String githubOAuthToken = getFieldByKey(credentials, GITHUB_TOKEN_KEY).getValue();

            final RepositoryId repositoryId = new RepositoryId(githubRepoOwner, githubRepoName);

            final IssueService issueService = new IssueService();
            issueService.getClient().setOAuth2Token(githubOAuthToken);
            final List<Issue> issues = issueService.getIssues(repositoryId, new HashMap<>());

            issues.forEach(issue -> {

                final RemoteTask rt = new RemoteTask();
                if (issue.getAssignee() != null) {
                    rt.setUserName(issue.getAssignee().getLogin());
                }

                rt.setId(String.valueOf(issue.getId()));
                rt.setTitle(issue.getTitle());
                rt.setOrigin(this.getServiceID());
                rt.setComments(issue.getBody());
                rt.setStopDate(issue.getClosedAt());
                rt.setStartDate(issue.getCreatedAt());

                remoteTasks.add(rt);
            });
        } catch (final Exception e) {
            LOGGER.error(e.getMessage());
        }

        LOGGER.info("Import {} tasks from github", remoteTasks.size());

        return remoteTasks;
    }

    private ProjectSyncCredentialField getFieldByKey(final List<ProjectSyncCredentialField> fields, final String key) {
        return fields.stream().filter(field -> field.getFieldKey().equals(key)).findFirst().get();
    }

}
