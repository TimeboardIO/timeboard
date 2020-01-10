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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import timeboard.core.security.TimeboardAuthentication;
import timeboard.core.api.EncryptionService;
import timeboard.core.api.OrganizationService;
import timeboard.core.api.ProjectService;
import timeboard.core.api.exceptions.BusinessException;
import timeboard.core.model.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.Serializable;
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
        final List<TaskType> taskTypes = this.projectService.listTaskType(authentication.getCurrentOrganization());

        model.addAttribute("taskTypes", taskTypes);
        model.addAttribute("defaultTasks", defaultTasks);
        if (organization.isPresent()) {
            model.addAttribute("organization", organization.get());
        }
        return "org_config.html";

    }


    @GetMapping(value = "/default-task/list", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<TaskWrapper>> listDefaultTasks(TimeboardAuthentication authentication) throws BusinessException {
        final List<DefaultTask> defaultTasks = this.projectService.listDefaultTasks(authentication.getCurrentOrganization(), new Date(), new Date());
        return ResponseEntity.ok(defaultTasks.stream().map(TaskWrapper::new).collect(Collectors.toList()));
    }

    @GetMapping(value = "/task-type/list", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<TypeWrapper>> listTaskTypes(TimeboardAuthentication authentication) throws BusinessException {
        final List<TaskType> taskTypes = this.projectService.listTaskType(authentication.getCurrentOrganization());
        return ResponseEntity.ok(taskTypes.stream().map(TypeWrapper::new).collect(Collectors.toList()));
    }

    @PostMapping(value = "/default-task", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity addDefaultTask(TimeboardAuthentication authentication,
                                            @ModelAttribute TaskWrapper taskWrapper) throws JsonProcessingException, BusinessException {
        Account actor = authentication.getDetails();
        Long orID = authentication.getCurrentOrganization();

        this.projectService.createDefaultTask(actor, orID, taskWrapper.getName());

        return this.listDefaultTasks(authentication);
    }

    @PatchMapping(value = "/default-task/{taskID}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity updateDefaultTask(TimeboardAuthentication authentication, @PathVariable Long taskID,
                                    @ModelAttribute TaskWrapper taskWrapper) throws JsonProcessingException, BusinessException {

        Account actor = authentication.getDetails();

        DefaultTask task = (DefaultTask) this.projectService.getTaskByID(actor, taskWrapper.getId());

        task.setName(taskWrapper.getName());
        this.projectService.updateDefaultTask(actor, task);

        return this.listDefaultTasks(authentication);
    }

    @DeleteMapping(value = "/default-task/{taskID}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity deleteDefaultTask(TimeboardAuthentication authentication, @PathVariable Long taskID) throws BusinessException {

        Account actor = authentication.getDetails();
        Long orgID = authentication.getCurrentOrganization();

        this.projectService.disableDefaultTaskByID(actor, orgID, taskID);

        return this.listDefaultTasks(authentication);
    }



    @PostMapping(value = "/task-type", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity addTaskType(TimeboardAuthentication authentication,
                                         @ModelAttribute TypeWrapper typeWrapper) throws JsonProcessingException, BusinessException {
        Account actor = authentication.getDetails();
        Long orID = authentication.getCurrentOrganization();

        this.projectService.createTaskType(actor, orID, typeWrapper.getName());

        return this.listTaskTypes(authentication);
    }

    @PatchMapping(value = "/task-type/{typeID}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity updateTaskType(TimeboardAuthentication authentication, @PathVariable Long typeID,
                                            @ModelAttribute TypeWrapper typeWrapper) throws JsonProcessingException, BusinessException {

        Account actor = authentication.getDetails();

        TaskType type =  this.projectService.findTaskTypeByID(typeWrapper.getId());

        type.setTypeName(typeWrapper.getName());
        this.projectService.updateTaskType(actor, type);

        return this.listTaskTypes(authentication);
    }

    @DeleteMapping(value = "/task-type/{typeID}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity deleteTaskType(TimeboardAuthentication authentication, @PathVariable Long typeID) throws BusinessException {

        Account actor = authentication.getDetails();
        TaskType type = projectService.findTaskTypeByID(typeID);
        this.projectService.disableTaskType(actor, type);

        return this.listTaskTypes(authentication);
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
        }

        //Extract organization
        return this.handleGet(authentication, request, model);
    }



    public static class TaskWrapper implements Serializable {
        public String name;
        public long id;

        public TaskWrapper(){}

        public TaskWrapper(DefaultTask task) {
            this.name = task.getName();
            this.id = task.getId();
        }

        public String getName(){
            return name;
        }

        public long getId(){
            return this.id;
        }

        public void setName(String name){
            this.name = name;
        }

        public void  setId(long id){
            this.id = id;
        }


    }
    public static class TypeWrapper {
        public String name;
        public long id;

        public TypeWrapper(){}


        public TypeWrapper(TaskType type) {
            this.name = type.getTypeName();
            this.id = type.getId();
        }

        public String getName(){
            return name;
        }

        public long getId(){
            return this.id;
        }

        public void setName(String name){
            this.name = name;
        }

        public void  setId(long id){
            this.id = id;
        }
    }
}
