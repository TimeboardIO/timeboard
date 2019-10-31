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

import org.osgi.service.component.annotations.*;
import timeboard.core.api.ProjectImportService;
import timeboard.core.api.ProjectService;
import timeboard.core.api.UserService;
import timeboard.core.api.exceptions.UserException;
import timeboard.core.api.exceptions.WrongPasswordException;
import timeboard.core.model.ProjectAttributValue;
import timeboard.core.model.User;
import timeboard.core.ui.TimeboardServlet;
import timeboard.core.ui.ViewModel;
import timeboard.security.SecurityContext;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;


@Component(
        service = Servlet.class,
        scope = ServiceScope.PROTOTYPE,
        property = {
                "osgi.http.whiteboard.servlet.pattern=/account",
                "osgi.http.whiteboard.context.select=(osgi.http.whiteboard.context.name=timeboard)"
        }
)
public class AccountServlet extends TimeboardServlet {

    @Reference
    private UserService userService;

    @Reference(
            policyOption = ReferencePolicyOption.GREEDY,
            cardinality = ReferenceCardinality.MULTIPLE,
            collectionType = CollectionType.SERVICE
    )
    private List<ProjectImportService> projectImportServlets;

    @Override
    protected ClassLoader getTemplateResolutionClassLoader() {
        return AccountServlet.class.getClassLoader();
    }

    @Override
    protected void handlePost(HttpServletRequest request, HttpServletResponse response, ViewModel viewModel) throws ServletException, IOException {
        User user = SecurityContext.getCurrentUser(request);

        String submitButton = request.getParameter("formType");
        if(submitButton.matches("password")){
            //Password modification

            String newPassword = request.getParameter("password1");
            String oldPassword = request.getParameter("oldPassword");

                user.setPassword(newPassword);
                try {
                    userService.updateUserPassword(user.getId(), oldPassword, newPassword);
                    viewModel.getViewDatas().put("message", "Password changed successfully !");
               /* } catch (WrongPasswordException e) { //TODO custom exception make OSGI controller crash
                    viewModel.getViewDatas().put("error", "Old password is incorrect.");
                } catch (UserException e) {
                    e.printStackTrace();
                    viewModel.getViewDatas().put("error", "Error while updating user password.");
                }*/

                }catch(Exception e){ //TODO replace by multiple catch 
                    viewModel.getViewDatas().put("error", "Old password is incorrect.");
                }


        }else if(submitButton.matches("account")){
            //Account modification
           String fistName = request.getParameter("firstName");
           String name = request.getParameter("name");
           String login = request.getParameter("login");
           String email = request.getParameter("email");

           user.setFirstName(fistName);
           user.setName(name);
           user.setLogin(login);
           user.setEmail(email);

           try{
              User u = userService.updateUser(user);
               viewModel.getViewDatas().put("message", "User account changed successfully !");
           }catch  (Exception e){
               viewModel.getViewDatas().put("error", "Error while updating user information.");
           }
        }else if(submitButton.matches("external")){
            Enumeration<String> params1 = request.getParameterNames();
            while (params1.hasMoreElements()) {
                String param = params1.nextElement();
                if (param.startsWith("attr-")) {
                    String key = param.substring(5, param.length());
                    String value = request.getParameter(param);
                    user.getExternalIDs().put(key, value);
                }
            }
            try{
                User u = userService.updateUser(user);
                viewModel.getViewDatas().put("message", "External tools updated successfully !");
            }catch  (Exception e){
                viewModel.getViewDatas().put("error", "Error while external tools");
            }
        }


        viewModel.getViewDatas().put("user", user);
        List<String> fieldNames = new ArrayList<>();
        projectImportServlets.forEach(service -> {
            fieldNames.addAll(service.getRequiredUserFields());
        });


        viewModel.getViewDatas().put("externalTools", fieldNames);
        viewModel.setTemplate("account:account.html");
    }

    @Override
    protected void handleGet(HttpServletRequest request, HttpServletResponse response, ViewModel viewModel) throws ServletException, IOException {
        User user = SecurityContext.getCurrentUser(request);

        viewModel.getViewDatas().put("user", user);

        List<String> fieldNames = new ArrayList<>();
        projectImportServlets.forEach(service -> {
            fieldNames.addAll(service.getRequiredUserFields());
        });

        viewModel.getViewDatas().put("externalTools", fieldNames);

        viewModel.setTemplate("account:account.html");
    }


}
