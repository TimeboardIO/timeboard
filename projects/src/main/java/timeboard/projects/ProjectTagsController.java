package timeboard.projects;

/*-
 * #%L
 * projects
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


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import timeboard.core.api.ProjectService;
import timeboard.core.api.exceptions.BusinessException;
import timeboard.core.model.Account;
import timeboard.core.model.Project;
import timeboard.core.model.ProjectTag;
import timeboard.core.security.TimeboardAuthentication;

import java.util.List;
import java.util.stream.Collectors;


@Controller
@RequestMapping("/projects/{project}" + ProjectTagsController.URL)
public class ProjectTagsController extends ProjectBaseController {

    public static final String URL = "/tags";

    @Autowired
    private ProjectService projectService;

    @GetMapping
    public String display(final TimeboardAuthentication authentication,
                          @PathVariable final Project project, final Model model) throws BusinessException {

        model.addAttribute("project", project);
        this.initModel(model, authentication, project);
        return "project_tags";
    }

    @GetMapping(value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ProjectTagWrapper>> listTags(final TimeboardAuthentication authentication,
                                                            @PathVariable final Project project) throws BusinessException {
        return ResponseEntity.ok(project.getTags().stream().map(projectTag -> new ProjectTagWrapper(projectTag)).collect(Collectors.toList()));
    }

    @DeleteMapping(value = "/{tagID}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ProjectTagWrapper>> deleteTag(
            final TimeboardAuthentication authentication,
            @PathVariable final Project project,
            @PathVariable final Long tagID) throws BusinessException {

        final Account actor = authentication.getDetails();
        project.getTags().removeIf(projectTag -> projectTag.getId().equals(tagID));
        this.projectService.updateProject(actor, project);
        return this.listTags(authentication, project);
    }

    @PatchMapping(value = "/{tagID}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ProjectTagWrapper>> patchTag(final TimeboardAuthentication authentication,
                                                            @PathVariable final Project project,
                                                            @PathVariable final Long tagID,
                                                            @ModelAttribute final ProjectTag tag) throws BusinessException {

        final Account actor = authentication.getDetails();
        project.getTags().stream()
                .filter(projectTag -> projectTag.getId().equals(tagID))
                .forEach(projectTag -> {
                    projectTag.setTagKey(tag.getTagKey());
                    projectTag.setTagValue(tag.getTagValue());
                });
        this.projectService.updateProject(actor, project);
        return this.listTags(authentication, project);
    }

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ProjectTagWrapper>> createTag(final TimeboardAuthentication authentication,
                                                             @PathVariable final Project project,
                                                             @ModelAttribute final ProjectTag tag) throws BusinessException {
        final Account actor = authentication.getDetails();
        tag.setProject(project);
        project.getTags().add(tag);
        this.projectService.updateProject(actor, project);
        return this.listTags(authentication, project);
    }

    public static class ProjectTagWrapper {

        private String tagKey;
        private String tagValue;
        private Long id;

        public ProjectTagWrapper(final ProjectTag projectTag) {
            this.tagKey = projectTag.getTagKey();
            this.tagValue = projectTag.getTagValue();
            this.id = projectTag.getId();
        }

        public Long getId() {
            return id;
        }

        public void setId(final Long id) {
            this.id = id;
        }

        public String getTagKey() {
            return tagKey;
        }

        public void setTagKey(final String tagKey) {
            this.tagKey = tagKey;
        }

        public String getTagValue() {
            return tagValue;
        }

        public void setTagValue(final String tagValue) {
            this.tagValue = tagValue;
        }
    }

}
