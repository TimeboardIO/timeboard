package timeboard.plugin.project.imp.jira;

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

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.JiraRestClientFactory;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import timeboard.core.api.ProjectService;
import timeboard.core.api.sync.ProjectSyncCredentialField;
import timeboard.core.api.sync.ProjectSyncPlugin;
import timeboard.core.api.sync.RemoteTask;
import timeboard.core.model.Account;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component("JiraImportPlugin")
public class JiraSyncPlugin implements ProjectSyncPlugin {

    private static final String JIRA_USERNAME_KEY = "jira.username";
    private static final String JIRA_PASSWORD_KEY = "jira.password";
    private static final String JIRA_PROJECT_KEY = "jira.project";
    private static final String JIRA_PROJECT_URL = "jira.url";
    public static final List<ProjectSyncCredentialField> FIELDS = Arrays.asList(
            new ProjectSyncCredentialField(JIRA_USERNAME_KEY, "JIRA Username", ProjectSyncCredentialField.Type.TEXT, 0),
            new ProjectSyncCredentialField(JIRA_PASSWORD_KEY, "JIRA Password", ProjectSyncCredentialField.Type.PASSWORD, 1),
            new ProjectSyncCredentialField(JIRA_PROJECT_URL, "JIRA URL", ProjectSyncCredentialField.Type.TEXT, 2),
            new ProjectSyncCredentialField(JIRA_PROJECT_KEY, "JIRA Project name", ProjectSyncCredentialField.Type.TEXT, 3)
    );
    private static final String JIRA_SERVICE_NAME = "JIRA";
    @Autowired
    private ProjectService projectService;

    @Override
    public String getServiceName() {
        return JIRA_SERVICE_NAME;
    }


    @Override
    public List<ProjectSyncCredentialField> getSyncCredentialFields() {
        return FIELDS;
    }

    private ProjectSyncCredentialField getFieldByKey(final List<ProjectSyncCredentialField> fields, final String key) {
        return fields.stream().filter(field -> field.getFieldKey().equals(key)).findFirst().get();
    }


    private JiraRestClient getJiraRestClient(final List<ProjectSyncCredentialField> fields) throws URISyntaxException {

        final String jiraUsername = this.getFieldByKey(fields, JIRA_USERNAME_KEY).getValue();
        final String jiraPassword = this.getFieldByKey(fields, JIRA_PASSWORD_KEY).getValue();
        final String jiraURL = this.getFieldByKey(fields, JIRA_PROJECT_URL).getValue();

        final JiraRestClientFactory factory = new AsynchronousJiraRestClientFactory();
        final URI uri = new URI(jiraURL);
        return factory.createWithBasicHttpAuthentication(uri, jiraUsername, jiraPassword);
    }

    @Override
    public List<RemoteTask> getRemoteTasks(final Account currentAccount,
                                           final List<ProjectSyncCredentialField> crendentials) throws Exception {

        final List<RemoteTask> remoteTaskList = new ArrayList<>();

        final JiraRestClient client = getJiraRestClient(crendentials);

        final String project = this.getFieldByKey(crendentials, JIRA_PROJECT_KEY).getValue();

        client.getSearchClient().searchJql(
                "project=\"" + project + "\"",
                -1, 0, null
        ).get().getIssues().forEach(issue -> {
            final RemoteTask rt = new RemoteTask();
            rt.setId(String.valueOf(issue.getId()));
            rt.setTitle(issue.getSummary());
            rt.setOrigin(this.getOriginLabel());
            rt.setStartDate(issue.getCreationDate().toDate());
            if (issue.getAssignee() != null) {
                rt.setUserName(issue.getAssignee().getName());
            }
            remoteTaskList.add(rt);
        });

        client.close();


        return remoteTaskList;
    }

    private String getOriginLabel() {
        return JIRA_SERVICE_NAME;
    }


}
