package timeboard.reports;

/*-
 * #%L
 * reports
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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import timeboard.core.api.ProjectService;
import timeboard.core.api.UserService;
import timeboard.core.model.Account;
import timeboard.core.model.ProjectTag;
import timeboard.core.ui.UserInfo;

import java.util.List;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;


@Component
@RestController
@RequestMapping(value = "/api/reports", produces = MediaType.APPLICATION_JSON_VALUE)
public class ReportsRestAPI {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Autowired
    private UserInfo userInfo;

    @Autowired
    private UserService userService;

    @Autowired
    private ProjectService projectService;

    @PostMapping(value = "/refreshProjectSelection", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity refreshProjectSelection(@RequestBody String filterProjects, HttpServletRequest request, Model model) throws JsonProcessingException {
        Account actor = this.userInfo.getCurrentAccount();
        Account organization = this.userService.findUserByID(this.userInfo.getCurrentOrganizationID());

        ExpressionParser expressionParser = new SpelExpressionParser();
        Expression expression = expressionParser.parseExpression(filterProjects);

        List<ProjectWrapper> listProjectsConcerned = this.projectService.listProjects(organization)
                .stream()
                .map(project -> new ProjectWrapper(project.getId(), project.getName(), project.getComments(), project.getTags()))
                .filter(pw -> pw.getProjectTags()
                                .stream()
                                .map(t -> expression.getValue(t, Boolean.class) != null ? expression.getValue(t, Boolean.class) : Boolean.FALSE)
                                .reduce(false, (aBoolean, aBoolean2) -> aBoolean || aBoolean2)
                ).collect(Collectors.toList());


        return ResponseEntity.status(HttpStatus.OK).body(MAPPER.writeValueAsString(listProjectsConcerned.toArray()));
    }

    public static class ProjectWrapper {

        private final Long projectID;
        private final String projectName;
        private final String projectComments;
        private final List<ProjectTag> projectTags;

        public ProjectWrapper(Long projectID, String projectName, String projectComments, List<ProjectTag> projectTags) {
            this.projectID = projectID;
            this.projectName = projectName;
            this.projectComments = projectComments;
            this.projectTags = projectTags;
        }

        public Long getProjectID() {
            return projectID;
        }

        public String getProjectName() {
            return projectName;
        }

        public String getProjectComments() {
            return projectComments;
        }

        public List<ProjectTag> getProjectTags() {
            return projectTags;
        }
    }

}
