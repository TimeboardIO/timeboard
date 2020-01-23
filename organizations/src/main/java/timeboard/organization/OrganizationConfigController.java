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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import timeboard.core.api.EncryptionService;
import timeboard.core.api.OrganizationService;
import timeboard.core.api.exceptions.BusinessException;
import timeboard.core.model.Account;
import timeboard.core.model.DefaultTask;
import timeboard.core.model.Organization;
import timeboard.core.model.TaskType;
import timeboard.core.security.TimeboardAuthentication;

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

    @Autowired
    public OrganizationService organizationService;
    @Autowired
    public EncryptionService encryptionService;
    @Value("${timeboard.tasks.default.vacation}")
    private String defaultVacationTaskName;

    @GetMapping
    protected String handleGet(final TimeboardAuthentication authentication,
                               final Model model) throws BusinessException {

        final Account actor = authentication.getDetails();

        final Optional<Organization> organization = this.organizationService.getOrganizationByID(actor, authentication.getCurrentOrganization());

        final List<DefaultTask> defaultTasks = this.organizationService.listDefaultTasks(authentication.getCurrentOrganization(),
                new Date(), new Date());
        final List<TaskType> taskTypes = this.organizationService.listTaskType(authentication.getCurrentOrganization());

        model.addAttribute("taskTypes", taskTypes);
        model.addAttribute("defaultTasks", defaultTasks);
        if (organization.isPresent()) {
            model.addAttribute("organization", organization.get());
        }
        return "org_config.html";

    }

    @GetMapping(value = "/default-task/list", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<TaskWrapper>> listDefaultTasks(final TimeboardAuthentication authentication) throws BusinessException {
        final List<DefaultTask> defaultTasks = this.organizationService.listDefaultTasks(authentication.getCurrentOrganization(),
                new Date(), new Date());
        return ResponseEntity.ok(defaultTasks.stream().filter(
                t -> !t.getName().matches(defaultVacationTaskName)).map(TaskWrapper::new).collect(Collectors.toList()));
    }

    @GetMapping(value = "/task-type/list", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<TypeWrapper>> listTaskTypes(final TimeboardAuthentication authentication) throws BusinessException {
        final List<TaskType> taskTypes = this.organizationService.listTaskType(authentication.getCurrentOrganization());
        return ResponseEntity.ok(taskTypes.stream().map(TypeWrapper::new).collect(Collectors.toList()));
    }

    @PostMapping(value = "/default-task", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity addDefaultTask(final TimeboardAuthentication authentication,
                                         @ModelAttribute final TaskWrapper taskWrapper) throws JsonProcessingException, BusinessException {
        final Account actor = authentication.getDetails();
        final Long orID = authentication.getCurrentOrganization();

        this.organizationService.createDefaultTask(actor, orID, taskWrapper.getName());

        return this.listDefaultTasks(authentication);
    }

    @PatchMapping(value = "/default-task/{taskID}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity updateDefaultTask(final TimeboardAuthentication authentication, @PathVariable final Long taskID,
                                            @ModelAttribute final TaskWrapper taskWrapper) throws JsonProcessingException, BusinessException {

        final Account actor = authentication.getDetails();

        final DefaultTask task = (DefaultTask) this.organizationService.getDefaultTaskByID(actor, taskWrapper.getId());

        task.setName(taskWrapper.getName());
        this.organizationService.updateDefaultTask(actor, task);

        return this.listDefaultTasks(authentication);
    }

    @DeleteMapping(value = "/default-task/{taskID}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity deleteDefaultTask(final TimeboardAuthentication authentication, @PathVariable final Long taskID) throws BusinessException {

        final Account actor = authentication.getDetails();
        final Long orgID = authentication.getCurrentOrganization();

        this.organizationService.disableDefaultTaskByID(actor, orgID, taskID);

        return this.listDefaultTasks(authentication);
    }


    @PostMapping(value = "/task-type", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity addTaskType(final TimeboardAuthentication authentication,
                                      @ModelAttribute final TypeWrapper typeWrapper) throws JsonProcessingException, BusinessException {
        final Account actor = authentication.getDetails();
        final Long orID = authentication.getCurrentOrganization();

        this.organizationService.createTaskType(actor, orID, typeWrapper.getName());

        return this.listTaskTypes(authentication);
    }

    @PatchMapping(value = "/task-type/{typeID}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity updateTaskType(final TimeboardAuthentication authentication, @PathVariable final Long typeID,
                                         @ModelAttribute final TypeWrapper typeWrapper) throws JsonProcessingException, BusinessException {

        final Account actor = authentication.getDetails();

        final TaskType type = this.organizationService.findTaskTypeByID(typeWrapper.getId());

        type.setTypeName(typeWrapper.getName());
        this.organizationService.updateTaskType(actor, type);

        return this.listTaskTypes(authentication);
    }

    @DeleteMapping(value = "/task-type/{typeID}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity deleteTaskType(final TimeboardAuthentication authentication, @PathVariable final Long typeID) throws BusinessException {

        final Account actor = authentication.getDetails();
        final TaskType type = organizationService.findTaskTypeByID(typeID);
        this.organizationService.disableTaskType(actor, type);

        return this.listTaskTypes(authentication);
    }


    @PostMapping
    protected String handlePost(
            final TimeboardAuthentication authentication,
            final RedirectAttributes redirectAttributes,
            final @ModelAttribute Organization model) throws Exception {

        final Account actor = authentication.getDetails();
        final Optional<Organization> updatedOrg = this.organizationService.updateOrganization(actor, model);
        if (updatedOrg.isPresent()) {
            redirectAttributes.addFlashAttribute("success", "Successfully updated..");
        }
        return "redirect:/org/setup";
    }


    public static class TaskWrapper implements Serializable {
        public String name;
        public long id;

        public TaskWrapper() {
        }

        public TaskWrapper(final DefaultTask task) {
            this.name = task.getName();
            this.id = task.getId();
        }

        public String getName() {
            return name;
        }

        public void setName(final String name) {
            this.name = name;
        }

        public long getId() {
            return this.id;
        }

        public void setId(final long id) {
            this.id = id;
        }


    }

    public static class TypeWrapper {
        public String name;
        public long id;

        public TypeWrapper() {
        }


        public TypeWrapper(final TaskType type) {
            this.name = type.getTypeName();
            this.id = type.getId();
        }

        public String getName() {
            return name;
        }

        public void setName(final String name) {
            this.name = name;
        }

        public long getId() {
            return this.id;
        }

        public void setId(final long id) {
            this.id = id;
        }
    }
}
