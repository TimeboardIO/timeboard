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
import timeboard.core.api.DataTableService;
import timeboard.core.api.ProjectImportService;
import timeboard.core.api.UserService;
import timeboard.core.model.Account;
import timeboard.core.model.DataTableConfig;
import timeboard.core.model.User;
import timeboard.core.ui.TimeboardServlet;
import timeboard.core.ui.ViewModel;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;



@WebServlet(name = "AccountServlet", urlPatterns = "/account")
public class AccountServlet extends TimeboardServlet {

    @Autowired
    private UserService userService;

    @Autowired(
            required = false
    )
    private List<ProjectImportService> projectImportServlets;

    @Autowired
    private DataTableService dataTableService;

    @Override
    protected ClassLoader getTemplateResolutionClassLoader() {
        return AccountServlet.class.getClassLoader();
    }

    @Override
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
                  User u = userService.updateUser(actor);
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
                    User u = userService.updateUser(actor);
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

    @Override
    protected void handleGet(Account actor, HttpServletRequest request, HttpServletResponse response, ViewModel viewModel) throws ServletException, IOException {
        loadPage(viewModel, actor);
    }
    
    private void loadPage(ViewModel viewModel, Account account) {
        viewModel.getViewDatas().put("user", account);

        List<String> fieldNames = new ArrayList<>();
        //import external ID field name from import plugins list
        if(projectImportServlets != null) {
            projectImportServlets.forEach(service -> {
                fieldNames.add(service.getServiceName());
            });
        }

        viewModel.getViewDatas().put("externalTools", fieldNames);

        DataTableConfig tableConfig = this.dataTableService.findTableConfigByUserAndTable(this.dataTableService.TABLE_TASK_ID, user);
        List<String> userTaskColumns = tableConfig != null ? tableConfig.getColumns() : new ArrayList<>();
        viewModel.getViewDatas().put("userTaskColumns", userTaskColumns);
        viewModel.getViewDatas().put("allTaskColumns", this.dataTableService.ALL_COLUMNS_TABLE_TASK);

        viewModel.setTemplate("account.html");
    }

}
