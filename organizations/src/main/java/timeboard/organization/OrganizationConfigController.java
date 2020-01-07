package timeboard.organization;

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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import timeboard.core.TimeboardAuthentication;
import timeboard.core.api.EncryptionService;
import timeboard.core.api.OrganizationService;
import timeboard.core.api.ProjectService;
import timeboard.core.api.exceptions.BusinessException;
import timeboard.core.model.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


/**
 * Display Organization details form.
 *
 * <p>Ex : /org/config?id=
 */
@Controller
@RequestMapping("/org/setup")
public class OrganizationConfigController {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    @Autowired
    public OrganizationService organizationService;

    @Autowired
    public ProjectService projectService;

    @Autowired
    public EncryptionService encryptionService;

    @GetMapping
    protected String handleGet(TimeboardAuthentication authentication,
                               HttpServletRequest request, Model model) throws ServletException, IOException, BusinessException {

        final Account actor = authentication.getDetails();

        final Optional<Organization> organization = this.organizationService.getOrganizationByID(actor, authentication.getCurrentOrganization());

        final List<DefaultTask> defaultTasks = this.projectService.listDefaultTasks(authentication.getCurrentOrganization(), new Date(), new Date());
        final List<TaskType> taskTypes = this.projectService.listTaskType();

        model.addAttribute("taskTypes", taskTypes);
        model.addAttribute("defaultTasks", defaultTasks);
        if (organization.isPresent()) {
            model.addAttribute("organization", organization.get());
        }
        return "org_config.html";

    }


    @GetMapping(value = "/default-task/list", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<TaskWrapper>> listDefaultTasks(TimeboardAuthentication authentication,
                                                            @PathVariable Long projectID) throws BusinessException {
        final List<DefaultTask> defaultTasks = this.projectService.listDefaultTasks(authentication.getCurrentOrganization(), new Date(0L), new Date(Long.MAX_VALUE));
        return ResponseEntity.ok(defaultTasks.stream().map(TaskWrapper::new).collect(Collectors.toList()));
    }



    @PostMapping
    protected String handlePost(TimeboardAuthentication authentication,
                                HttpServletRequest request, Model model) throws Exception {

        final Account actor = authentication.getDetails();

        String action = request.getParameter("action");
        Optional<Organization> organization = this.organizationService.getOrganizationByID(actor, authentication.getCurrentOrganization());

        switch (action) {
            case "CONFIG":
                organization.get().setName(request.getParameter("organizationName"));
                this.organizationService.updateOrganization(actor, organization.get());
                break;
            case "NEW_TASK":
                this.projectService.createDefaultTask(actor, authentication.getCurrentOrganization(), request.getParameter("newDefaultTask"));
                break;
            case "NEW_TYPE":
                this.projectService.createTaskType(actor, request.getParameter("newTaskType"));
                break;
            case "DELETE_TYPE":
                long typeID = Long.parseLong(request.getParameter("typeID"));
                TaskType first = this.projectService.listTaskType()
                        .stream().filter(taskType -> taskType.getId() == typeID).findFirst().get();
                this.projectService.disableTaskType(actor, first);
                break;
            case "DELETE_TASK":
                long taskID = Long.parseLong(request.getParameter("taskID"));
                this.projectService.disableDefaultTaskByID(actor, authentication.getCurrentOrganization(), taskID);
                break;
        }

        //Extract organization
        return this.handleGet(authentication, request, model);
    }



    public static class TaskWrapper {
        private DefaultTask task;

        public TaskWrapper(DefaultTask task) {
            this.task = task;
        }

        public String getName(){
            return task.getName();
        }

    }
}
