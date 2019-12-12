package timeboard.timesheet;

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
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import timeboard.core.api.EncryptionService;
import timeboard.core.api.OrganizationService;
import timeboard.core.api.ProjectService;
import timeboard.core.api.exceptions.BusinessException;
import timeboard.core.model.Account;
import timeboard.core.model.DefaultTask;
import timeboard.core.model.TaskType;
import timeboard.core.ui.UserInfo;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Date;
import java.util.List;


/**
 * Display Organization details form.
 *
 * <p>Ex : /org/config?id=
 */
@Controller
@RequestMapping("/org/{orgID}/config")
public class OrganizationConfigServlet  {

    @Autowired
    private UserInfo userInfo;

    @Autowired
    public OrganizationService organizationService;

    @Autowired
    public ProjectService projectService;

    @Autowired
    public EncryptionService encryptionService;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @GetMapping
    protected String handleGet(HttpServletRequest request, Model viewModel) throws ServletException, IOException, BusinessException  {

        final Account actor = this.userInfo.getCurrentAccount();

        long id = Long.parseLong(request.getParameter("orgID"));

        final Account organization = this.organizationService.getOrganizationByID(actor, id);

        final List<DefaultTask> defaultTasks = this.projectService.listDefaultTasks(new Date(), new Date());
        final List<TaskType> taskTypes = this.projectService.listTaskType();

        viewModel.addAttribute("taskTypes", taskTypes);
        viewModel.addAttribute("defaultTasks", defaultTasks);
        viewModel.addAttribute("organization", organization);

        return "details_org_config";

    }

    @PostMapping
    protected String handlePost( HttpServletRequest request, Model viewModel) throws Exception {
        final Account actor = this.userInfo.getCurrentAccount();

        String action = request.getParameter("action");
        long id = Long.parseLong(request.getParameter("orgID"));
        Account organization = this.organizationService.getOrganizationByID(actor, id);

        switch (action) {
            case "CONFIG":
                organization.setName(request.getParameter("organizationName"));
                this.organizationService.updateOrganization(actor, organization);
                break;
            case "NEW_TASK":
                this.projectService.createDefaultTask(actor, request.getParameter("newDefaultTask"));
                break;
            case "NEW_TYPE":
                this.projectService.createTaskType(actor, request.getParameter("newTaskType"));
                break;
            case "DELETE_TYPE":
                long typeID = Long.parseLong(request.getParameter("typeID"));
                TaskType first = this.projectService.listTaskType().stream().filter(taskType -> taskType.getId() == typeID).findFirst().get();
                this.projectService.disableTaskType(actor, first);
                break;
            case "DELETE_TASK":
                long taskID = Long.parseLong(request.getParameter("taskID"));
                this.projectService.disableDefaultTaskByID(actor,taskID);
                break;
        }

        //Extract organization
        return this.handleGet(request, viewModel);
    }
}
