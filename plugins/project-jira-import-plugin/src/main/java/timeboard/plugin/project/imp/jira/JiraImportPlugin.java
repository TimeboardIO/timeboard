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
import timeboard.core.api.ProjectImportService;
import timeboard.core.api.ProjectService;
import timeboard.core.api.exceptions.BusinessException;
import timeboard.core.model.Project;
import timeboard.core.model.User;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Component(
        service = ProjectImportService.class,
        immediate = true
)
public class JiraImportPlugin implements ProjectImportService {

    private static final String JIRA_USERNAME_KEY = "jira.username";
    private static final String JIRA_PASSWORD_KEY = "jira.password";
    private static final String JIRA_USER_FIELD = "jira";
    private static final List<String> JIRA_USER_FIELDS = new ArrayList<>();

    @Reference
    private ProjectService projectService;

    @Override
    public String getServiceName() {
        return "Jira";
    }


    @Override
    public List<String> getRequiredUserFields() {
        if(JIRA_USER_FIELDS.size() != 1) JIRA_USER_FIELDS.add(JIRA_USER_FIELD);
        return JIRA_USER_FIELDS;
    }

    @Override
    public String importTasksToProject(User actor, long projectID) throws BusinessException {
        try {

            final Project project = this.projectService.getProjectByID(actor, projectID);

            String jiraUsername = project.getAttributes().get(JIRA_USERNAME_KEY).getValue();
            String jiraPassword = project.getAttributes().get(JIRA_PASSWORD_KEY).getValue();

            final JiraRestClientFactory factory = new AsynchronousJiraRestClientFactory();
            final URI uri = new URI("https://tsl-extranet.fr/jira");
            JiraRestClient client = factory.createWithBasicHttpAuthentication(uri, jiraUsername, jiraPassword);

            client.getProjectClient().getAllProjects().get().forEach(basicProject -> {
                System.out.println(basicProject.getName());
            });
            client.close();
        } catch (URISyntaxException | IOException | InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        return "";
    }


}
