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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import timeboard.core.api.ProjectExportService;
import timeboard.core.api.ProjectImportService;
import timeboard.core.api.ProjectService;
import timeboard.core.api.UserService;
import timeboard.core.api.exceptions.BusinessException;
import timeboard.core.model.*;
import timeboard.core.ui.UserInfo;
import timeboard.core.ui.ViewModel;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;


@Controller
public class ProjectOperationsController {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Autowired(
            required = false
    )
    private List<ProjectExportService> projectExportServices;

    @Autowired(
            required = false
    )
    private List<ProjectImportService> projectImportServices;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private UserService userService;

    @Autowired
    private UserInfo userInfo;

    @PostMapping("/projects/create")
    protected String handlePost(HttpServletRequest request, HttpServletResponse response, ViewModel viewModel) throws ServletException, IOException, BusinessException {
        final Account actor = this.userInfo.getCurrentAccount();
        this.projectService.createProject(actor, request.getParameter("projectName"));
        return "redirect:/projects";
    }

    @GetMapping("/projects/create")
    protected String createFrom() throws ServletException, IOException {
        return "create_project";
    }

    @GetMapping("/projects/{projectID}/setup")
    protected String configProject(@PathVariable long projectID, Model model) throws BusinessException, JsonProcessingException {
        final Account actor = this.userInfo.getCurrentAccount();
        final Project project = this.projectService.getProjectByIdWithAllMembers(actor, projectID);
        final Map<String, Object> map = new HashMap<>();
        this.prepareTemplateData(actor, project, map);
        model.addAllAttributes(map);
        return "details_project_config";
    }

    @PostMapping("/projects/{projectID}/setup/memberships")
    @ResponseBody
    protected ResponseEntity updateProjectMembers(@PathVariable long projectID, HttpServletRequest request) throws Exception {
        final Account actor = this.userInfo.getCurrentAccount();
        final Account targetMember = this.userService.findUserByID(Long.parseLong(request.getParameter("memberID")));
        final Project project = this.projectService.getProjectByID(actor, projectID);
        project.getMembers().add(new ProjectMembership(project, targetMember, MembershipRole.CONTRIBUTOR));
        this.projectService.updateProject(actor, project);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PatchMapping("/projects/{projectID}/setup/memberships/{membershipID}/{role}")
    protected ResponseEntity updateProjectMembers(@PathVariable Long projectID, @PathVariable Long membershipID,  @PathVariable MembershipRole role) throws Exception {
        final Account actor = this.userInfo.getCurrentAccount();
        final Project project = this.projectService.getProjectByIdWithAllMembers(actor, projectID);
        project.getMembers().stream()
                .filter(projectMembership -> projectMembership.getMembershipID() == membershipID)
                .forEach(projectMembership -> projectMembership.setRole(role));
        this.projectService.updateProject(actor, project);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @DeleteMapping("/projects/{projectID}/setup/memberships/{membershipID}")
    protected ResponseEntity deleteProjectMembers(@PathVariable Long projectID, @PathVariable Long membershipID) throws Exception {
        final Account actor = this.userInfo.getCurrentAccount();
        final Project project = this.projectService.getProjectByIdWithAllMembers(actor, projectID);
        project.getMembers().removeIf(projectMembership -> {
            return projectMembership.getMembershipID() == membershipID && projectMembership.getMember().getId() != actor.getId();
        });
        this.projectService.updateProject(actor, project);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PostMapping("/projects/{projectID}/setup/informations")
    protected String updateProjectConfiguration(@PathVariable long projectID, @ModelAttribute ProjectConfigForm projectConfigForm) throws Exception {

        final Account actor = this.userInfo.getCurrentAccount();

        Project project = this.projectService.getProjectByIdWithAllMembers(actor, projectID);
        project.setName(projectConfigForm.getName());
        project.setComments(projectConfigForm.getComments());
        project.setQuotation(projectConfigForm.getQuotation());

        this.projectService.updateProject(actor, project);

        /*
        //Extract project configuration
        project.getAttributes().clear();

        //new attributes
        String newAttrKey = request.getParameter("newAttrKey");
        String newAttrValue = request.getParameter("newAttrValue");
        Boolean newAttrEncrypted = false;
        if (request.getParameter("newAttrEncrypted") != null && request.getParameter("newAttrEncrypted").equals("on")) {
            newAttrEncrypted = true;
        }

        if (!newAttrKey.isEmpty()) {
            if (newAttrEncrypted) {
                newAttrValue = this.encryptionService.encryptAttribute(newAttrValue);
            }
            project.getAttributes().put(newAttrKey, new ProjectAttributValue(newAttrValue, newAttrEncrypted));
        }

        //Attribute update
        Enumeration<String> params1 = request.getParameterNames();
        while (params1.hasMoreElements()) {
            String param = params1.nextElement();
            if (param.startsWith("attr-")) {
                String key = param.substring(5, param.length());
                String value = request.getParameter(param);
                project.getAttributes().put(key, new ProjectAttributValue(value));
            }
            if (param.startsWith("attrenc-")) {
                String key = param.substring(8, param.length());
                String encrypted = request.getParameter(param);
                project.getAttributes().get(key).setEncrypted(true);
                // project.getAttributes().get(key).setEncrypted(Boolean.getBoolean(encrypted));
            }
        }

        //Extract memberships from request
        Map<Long, MembershipRole> memberships = new HashMap<>();
        Enumeration<String> params = request.getParameterNames();
        while (params.hasMoreElements()) {
            String param = params.nextElement();
            if (param.startsWith("members")) {
                String key = param.substring(param.indexOf('[') + 1, param.indexOf(']'));
                String value = request.getParameter(param);
                if (!value.isEmpty()) {
                    memberships.put(Long.parseLong(key), MembershipRole.valueOf(value));
                } else {
                    memberships.put(Long.parseLong(key), MembershipRole.CONTRIBUTOR);
                }
            }
        }

        this.projectService.updateProject(actor, project, memberships);

        Map<String, Object> map = new HashMap<>();
        prepareTemplateData(actor, project, map);

        viewModel.getViewDatas().putAll(map);
*/
        return "redirect:/projects/"+projectID+"/setup";
    }


    @GetMapping("/projects/{projectID}/delete")
    protected String handleGet(@PathVariable long projectID) throws ServletException, IOException, BusinessException {
        final Project project = this.projectService.getProjectByID(this.userInfo.getCurrentAccount(), projectID);
        this.projectService.archiveProjectByID(this.userInfo.getCurrentAccount(), project);
        return "redirect:/projects";
    }

    private void prepareTemplateData(final Account actor, final Project project, final Map<String, Object> map) throws BusinessException, JsonProcessingException {
        final ProjectConfigForm pcf = new ProjectConfigForm();
        pcf.setName(project.getName());
        pcf.setComments(project.getComments());
        pcf.setQuotation(project.getQuotation());

        final ProjectMembersForm pmf = new ProjectMembersForm();
        pmf.setMemberships(new ArrayList<>(project.getMembers()));

        map.put("project", project);
        map.put("projectConfigForm", pcf);
        map.put("projectMembersForm", pmf);
        map.put("roles", MembershipRole.values());

        /*
        map.put("project", project);
        map.put("members", project.getMembers());
        map.put("roles", MembershipRole.values());
        map.put("rolesForNewMember", OBJECT_MAPPER.writeValueAsString(MembershipRole.values()));
        map.put("exports", this.projectExportServices);
        map.put("imports", this.projectImportServices);
        map.put("tasks", this.projectService.listProjectTasks(actor, project));*/
    }

    public static class ProjectMembersForm{

        private  List<ProjectMembership> memberships = new ArrayList<>();

        public MembershipRole[] getRoles(){
            return MembershipRole.values();
        }

        public List<ProjectMembership> getMemberships() {
            return memberships;
        }

        public void setMemberships(List<ProjectMembership> memberships) {
            this.memberships = memberships;
        }
    }

    public static class ProjectConfigForm{

        private String name;
        private double quotation;
        private String comments;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public double getQuotation() {
            return quotation;
        }

        public void setQuotation(double quotation) {
            this.quotation = quotation;
        }

        public String getComments() {
            return comments;
        }

        public void setComments(String comments) {
            this.comments = comments;
        }


    }


}
