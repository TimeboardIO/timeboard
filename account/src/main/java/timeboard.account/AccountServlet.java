package timeboard.account;

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
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import timeboard.core.api.DataTableService;
import timeboard.core.api.ProjectImportService;
import timeboard.core.api.ProjectService;
import timeboard.core.api.UserService;
import timeboard.core.model.Account;
import timeboard.core.model.Project;
import timeboard.core.model.DataTableConfig;
import timeboard.core.ui.ViewModel;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import java.text.DateFormatSymbols;


@Controller
@RequestMapping("/org/{orgID}/account")
public class AccountServlet {

    @Autowired
    private UserService userService;

    @Autowired
    private ProjectService projectService;

    @Autowired(
            required = false
    )
    private List<ProjectImportService> projectImportServlets;

    @Autowired
    private DataTableService dataTableService;

    @PostMapping
    protected void handlePost(Account actor, HttpServletRequest request, HttpServletResponse response, ViewModel viewModel) throws ServletException, IOException {

        String submitButton = request.getParameter("formType");

        switch (submitButton) {

            case "account":
               //Account modification
               String fistName = request.getParameter("firstName");
               String name = request.getParameter("name");
               String email = request.getParameter("email");

                actor.setFirstName(fistName);
                actor.setName(name);
                actor.setEmail(email);

               try {
                   Account u = userService.updateUser(actor);
                   viewModel.getViewDatas().put("message", "User account changed successfully !");
               } catch (Exception e) {
                   viewModel.getViewDatas().put("error", "Error while updating user information.");
               }
               break;

            case "external":
                Enumeration<String> params1 = request.getParameterNames();
                while (params1.hasMoreElements()) {
                    String param = params1.nextElement();
                    if (param.startsWith("attr-")) {
                        String key = param.substring(5, param.length());
                        String value = request.getParameter(param);
                        actor.getExternalIDs().put(key, value);
                    }
                }
                try {
                    Account u = userService.updateUser(actor);
                    viewModel.getViewDatas().put("message", "External tools updated successfully !");
                } catch (Exception e) {
                    viewModel.getViewDatas().put("error", "Error while external tools");
                }
                break;

            case "columnTask":
                List<String> selectedColumnsString = request.getParameterValues("columnSelected") != null ? Arrays.asList(request.getParameterValues("columnSelected")) : new ArrayList<>();
                try {
                    this.dataTableService.addOrUpdateTableConfig(this.dataTableService.TABLE_TASK_ID, actor, selectedColumnsString);
                    viewModel.getViewDatas().put("message", "Task columns preferences updated successfully !");
                } catch (Exception e) {
                    viewModel.getViewDatas().put("error", "Error while updating Task columns preferences");
                }
                break;

            default:
        }
        loadPage(viewModel, actor);

    }

    @GetMapping
    protected void handleGet(Account actor, HttpServletRequest request, HttpServletResponse response, ViewModel viewModel) throws ServletException, IOException {
        loadPage(viewModel, actor);
    }
    
    private void loadPage(ViewModel viewModel, Account actor) {
        viewModel.getViewDatas().put("account", actor);

        List<Project> projects = projectService.listProjects(actor);

        List<String> fieldNames = new ArrayList<>();
        //import external ID field name from import plugins list
        if(projectImportServlets != null) {
            projectImportServlets.forEach(service -> {
                fieldNames.add(service.getServiceName());
            });
        }

        Set<Integer> yearsSinceHiring = new LinkedHashSet<>();
        Map<Integer, String> monthsSinceHiring = new LinkedHashMap<>();
        Calendar end = Calendar.getInstance();
        end.setTime(actor.getBeginWorkDate());
        Calendar start = Calendar.getInstance();
        start.setTime(new Date());
        DateFormatSymbols dfs = new DateFormatSymbols(Locale.ENGLISH);
        dfs.getLocalPatternChars();
        String[] months = dfs.getMonths();
        for (int i = start.get(Calendar.MONTH); start.after(end); start.add(Calendar.MONTH, -1), i = start.get(Calendar.DAY_OF_MONTH)) {
            yearsSinceHiring.add(start.get(Calendar.YEAR));
            if(monthsSinceHiring.size() < 12) monthsSinceHiring.put(start.get(Calendar.MONTH), months[start.get(Calendar.MONTH)]);
        }

        viewModel.getViewDatas().put("externalTools", fieldNames);
        viewModel.getViewDatas().put("projects", projects);
        viewModel.getViewDatas().put("yearsSinceHiring", yearsSinceHiring);
        viewModel.getViewDatas().put("monthsSinceHiring", monthsSinceHiring);
        DataTableConfig tableConfig = this.dataTableService.findTableConfigByUserAndTable(this.dataTableService.TABLE_TASK_ID, actor);
        List<String> userTaskColumns = tableConfig != null ? tableConfig.getColumns() : new ArrayList<>();
        viewModel.getViewDatas().put("userTaskColumns", userTaskColumns);
        viewModel.getViewDatas().put("allTaskColumns", this.dataTableService.ALL_COLUMNS_TABLE_TASK);
        viewModel.setTemplate("account.html");
    }

}
