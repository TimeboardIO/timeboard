package timeboard.projects;

/*-
 * #%L
 * webui
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.support.RequestContextUtils;
import timeboard.core.api.AccountService;
import timeboard.core.api.ProjectService;
import timeboard.core.api.exceptions.BusinessException;
import timeboard.core.model.*;
import timeboard.core.security.TimeboardAuthentication;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Controller
@RequestMapping("/projects/{projectID}" + ProjectSetupController.URL)
public class ProjectSetupController extends ProjectBaseController {

    public static final String URL = "/setup";

    @Autowired
    private ProjectService projectService;

    @Autowired
    private AccountService accountService;


    @GetMapping
    protected String setupProject(
            final TimeboardAuthentication authentication,
            @PathVariable final long projectID,
            final Model model,
            HttpServletRequest request) throws BusinessException {

        final Account actor = authentication.getDetails();
        final Project project = this.projectService.getProjectByID(actor, authentication.getCurrentOrganization(), projectID);
        final Map<String, Object> map = new HashMap<>();
        this.prepareTemplateData(authentication.getCurrentOrganization(), project, map, request);
        model.addAllAttributes(map);
        this.initModel(model, authentication, project);
        return "project_setup.html";
    }

    @PostMapping("/memberships")
    @ResponseBody
    protected ResponseEntity updateProjectMembers(final TimeboardAuthentication authentication,
                                                  @PathVariable final long projectID, final HttpServletRequest request) throws Exception {
        final Account actor = authentication.getDetails();
        final Account targetMember = this.accountService.findUserByID(Long.parseLong(request.getParameter("memberID")));
        final Project project = this.projectService.getProjectByID(actor, authentication.getCurrentOrganization(), projectID);

        if (project.isMember(targetMember)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("This user is already member of this project");
        }

        project.getMembers().add(new ProjectMembership(project, targetMember, MembershipRole.CONTRIBUTOR));
        this.projectService.updateProject(actor, project);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PatchMapping("/memberships/{membershipID}/{role}")
    protected ResponseEntity updateProjectMembers(final TimeboardAuthentication authentication,
                                                  @PathVariable final Long projectID,
                                                  @PathVariable final Long membershipID,
                                                  @PathVariable final MembershipRole role) throws Exception {

        final Account actor = authentication.getDetails();
        final Project project = this.projectService.getProjectByID(actor, authentication.getCurrentOrganization(), projectID);
        project.getMembers().stream()
                .filter(projectMembership -> projectMembership.getMembershipID().equals(membershipID))
                .forEach(projectMembership -> projectMembership.setRole(role));
        this.projectService.updateProject(actor, project);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @DeleteMapping("/memberships/{membershipID}")
    protected ResponseEntity deleteProjectMembers(final TimeboardAuthentication authentication,
                                                  @PathVariable final Long projectID,
                                                  @PathVariable final Long membershipID) throws Exception {

        final Account actor = authentication.getDetails();
        final Project project = this.projectService.getProjectByID(actor, authentication.getCurrentOrganization(), projectID);
        project.getMembers().removeIf(projectMembership -> {
            return projectMembership.getMembershipID() == membershipID && projectMembership.getMember().getId() != actor.getId();
        });

        this.projectService.updateProject(actor, project);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PostMapping("/informations")
    public String updateProjectConfiguration(final TimeboardAuthentication authentication,
                                             @PathVariable final long projectID,
                                             @ModelAttribute final ProjectConfigForm projectConfigForm,
                                             final RedirectAttributes attributes) throws Exception {

        if (projectConfigForm.getComments().length() > 500) {
            attributes.addFlashAttribute("error", "Your comment is too long (500 characters max)");
            attributes.addFlashAttribute("editedComments", projectConfigForm.getComments());
        } else {

            final Account actor = authentication.getDetails();

            final Project project = this.projectService.getProjectByID(actor, authentication.getCurrentOrganization(), projectID);
            project.setName(projectConfigForm.getName());
            project.setComments(projectConfigForm.getComments());
            project.setQuotation(projectConfigForm.getQuotation());

            try {
                this.projectService.updateProject(actor, project);
                attributes.addFlashAttribute("success", "Project config updated successfully.");
            } catch (final BusinessException e) {
                attributes.addFlashAttribute("error", e.getMessage());
            }
        }
        return "redirect:/projects/" + projectID + URL;
    }

    private void prepareTemplateData(final Organization org, final Project project, final Map<String, Object> map, HttpServletRequest request) {

        final ProjectConfigForm pcf = new ProjectConfigForm();
        pcf.setName(project.getName());

        if (RequestContextUtils.getInputFlashMap(request) != null && RequestContextUtils.getInputFlashMap(request).containsKey("editedComments")) {
            // Keep last edited comment
            pcf.setComments((String) RequestContextUtils.getInputFlashMap(request).get("editedComments"));
        } else {
            pcf.setComments(project.getComments());
        }
        pcf.setQuotation(project.getQuotation());

        final ProjectMembersForm pmf = new ProjectMembersForm();
        pmf.setMemberships(new ArrayList<>(project.getMembers()));

        map.put("project", project);
        map.put("orgID", org.getId());
        map.put("projectConfigForm", pcf);
        map.put("projectMembersForm", pmf);
        map.put("roles", MembershipRole.values());

    }

    public static class ProjectMembersForm {

        private List<ProjectMembership> memberships = new ArrayList<>();

        public MembershipRole[] getRoles() {
            return MembershipRole.values();
        }

        public List<ProjectMembership> getMemberships() {
            return memberships;
        }

        public void setMemberships(final List<ProjectMembership> memberships) {
            this.memberships = memberships;
        }
    }

    public static class ProjectConfigForm {

        private String name;
        private double quotation;
        private String comments;

        public String getName() {
            return name;
        }

        public void setName(final String name) {
            this.name = name;
        }

        public double getQuotation() {
            return quotation;
        }

        public void setQuotation(final double quotation) {
            this.quotation = quotation;
        }

        public String getComments() {
            return comments;
        }

        public void setComments(final String comments) {
            this.comments = comments;
        }


    }


}
