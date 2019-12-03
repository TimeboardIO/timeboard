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
import timeboard.core.api.ProjectImportService;
import timeboard.core.api.ProjectService;
import timeboard.core.api.exceptions.BusinessException;
import timeboard.core.model.Project;
import timeboard.core.model.User;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Component
public class JiraImportPlugin implements ProjectImportService {

    private static final String JIRA_USERNAME_KEY = "jira.username";
    private static final String JIRA_PASSWORD_KEY = "jira.password";
    private static final String JIRA_PROJECT_KEY = "jira.project";
    private static final String JIRA_SERVICE_NAME = "JIRA";

    @Autowired
    private ProjectService projectService;

    @Override
    public String getServiceName() {
        return JIRA_SERVICE_NAME;
    }


    @Override
    public List<String> getRequiredUserFields() {
        return Arrays.asList(JIRA_USERNAME_KEY, JIRA_PASSWORD_KEY, JIRA_PROJECT_KEY);
    }


    private JiraRestClient getJiraRestClient(Project project) throws URISyntaxException {
        String jiraUsername = project.getAttributes().get(JIRA_USERNAME_KEY).getValue();
        String jiraPassword = project.getAttributes().get(JIRA_PASSWORD_KEY).getValue();

        final JiraRestClientFactory factory = new AsynchronousJiraRestClientFactory();
        final URI uri = new URI("https://tsl-extranet.fr/jira");
        return factory.createWithBasicHttpAuthentication(uri, jiraUsername, jiraPassword);
    }

    @Override
    public List<RemoteTask> getRemoteTasks(User currentUser, long projectID) throws BusinessException {

        List<RemoteTask> remoteTaskList = new ArrayList<>();
        try {

            final Project project = this.projectService.getProjectByID(currentUser, projectID);
            final JiraRestClient client = getJiraRestClient(project);

            client.getSearchClient().searchJql(
                    "project=\""+project.getAttributes().get(JIRA_PROJECT_KEY).getValue()+"\"",
                    -1, 0, null
            ).get().getIssues().forEach(issue -> {
                RemoteTask rt = new RemoteTask();
                rt.setId(issue.getId());
                rt.setTitle(issue.getSummary());
                rt.setOrigin(this.getOriginLabel());
                rt.setStartDate(issue.getCreationDate().toDate());
                if(issue.getAssignee() != null) {
                    rt.setUserName(issue.getAssignee().getName());
                }
                remoteTaskList.add(rt);
            });

            client.close();

        } catch (URISyntaxException | InterruptedException | ExecutionException | IOException e) {
            e.printStackTrace();
        }

        return remoteTaskList;
    }

    private String getOriginLabel() {
        return JIRA_SERVICE_NAME;
    }


}
