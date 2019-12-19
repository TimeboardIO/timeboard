package timeboard.reports;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.stereotype.Service;
import timeboard.core.api.ProjectService;
import timeboard.core.model.Account;
import timeboard.core.model.Project;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ReportsService {

    @Autowired
    private ProjectService projectService;

    public Set<ProjectWrapper> findProjects(Account actor, List<String> expressions){

        final ExpressionParser expressionParser = new SpelExpressionParser();

        final List<Expression> spelExp = expressions
                .stream().map(filter -> expressionParser.parseExpression(filter))
                .collect(Collectors.toList());

        final Set<ProjectWrapper> listProjectsConcerned = this.projectService.listProjects(actor)
                .stream()
                .map(project -> wrapProjectTags(project))
                .filter(projectWrapper -> {

                    final Boolean match = spelExp.stream()
                            .map(exp -> applyFilterOnProject(exp, projectWrapper))
                            .allMatch(aBoolean -> aBoolean == true);

                    return match;

                }).collect(Collectors.toSet());

        return listProjectsConcerned;

    }

    private boolean applyFilterOnProject(final Expression exp, final ProjectWrapper projectWrapper) {
        return projectWrapper.getProjectTags()
                .stream()
                .map(tagWrapper -> exp.getValue(tagWrapper, Boolean.class))
                .anyMatch(aBoolean -> aBoolean == true);
    }

    private ProjectWrapper wrapProjectTags(Project project) {
        List<TagWrapper> tags = project.getTags()
                .stream()
                .map(tag -> new TagWrapper(tag.getTagKey(), tag.getTagValue()))
                .collect(Collectors.toList());
        return new ProjectWrapper(project.getId(), project.getName(), project.getComments(), tags);
    }

    public static class ProjectWrapper {

        private final Long projectID;
        private final String projectName;
        private final String projectComments;
        private final List<TagWrapper> projectTags;

        public ProjectWrapper(Long projectID, String projectName, String projectComments, List<TagWrapper> projectTags) {
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

        public List<TagWrapper> getProjectTags() {
            return projectTags;
        }
    }

    public static class TagWrapper {

        private final String tagKey;
        private final String tagValue;

        public TagWrapper(String tagKey, String tagValue) {
            this.tagKey = tagKey;
            this.tagValue = tagValue;
        }

        public String getTagKey() {
            return tagKey;
        }

        public String getTagValue() {
            return tagValue;
        }
    }
}
