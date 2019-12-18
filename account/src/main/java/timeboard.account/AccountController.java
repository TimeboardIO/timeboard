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
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import timeboard.core.api.sync.ProjectSyncPlugin;
import timeboard.core.api.ProjectService;
import timeboard.core.api.UserService;
import timeboard.core.model.Account;
import timeboard.core.model.Project;
import timeboard.core.ui.UserInfo;

import javax.servlet.http.HttpServletRequest;
import java.text.DateFormatSymbols;
import java.util.*;


@Controller
@RequestMapping("/account")
public class AccountController {

    @Autowired
    private UserService userService;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private UserInfo userInfo;

    @Autowired(
            required = false
    )
    private List<ProjectSyncPlugin> projectImportServices;


    @PostMapping
    protected String handlePost(HttpServletRequest request, Model model) {

        final String submitButton = request.getParameter("formType");
        final Account actor = this.userInfo.getCurrentAccount();

        switch (submitButton) {

            case "account":
                //Account modification
                final String fistName = request.getParameter("firstName");
                final String name = request.getParameter("name");
                final String email = request.getParameter("email");

                actor.setFirstName(fistName);
                actor.setName(name);
                actor.setEmail(email);

                try {
                    final Account u = userService.updateUser(actor);
                    model.addAttribute("message", "User account changed successfully !");
                } catch (Exception e) {
                    model.addAttribute("error", "Error while updating user information.");
                }
                break;

            case "external":
                final Enumeration<String> params1 = request.getParameterNames();
                while (params1.hasMoreElements()) {
                    final String param = params1.nextElement();
                    if (param.startsWith("attr-")) {
                        final String key = param.substring(5, param.length());
                        final String value = request.getParameter(param);
                        actor.getExternalIDs().put(key, value);
                    }
                }
                try {
                    final Account u = userService.updateUser(actor);
                    model.addAttribute("message", "External tools updated successfully !");
                } catch (Exception e) {
                    model.addAttribute("error", "Error while external tools");
                }
                break;


            default:
        }
        loadPage(model, actor);
        return "account.html";

    }

    @GetMapping
    protected String handleGet(Model model) {
        final Account actor = this.userInfo.getCurrentAccount();
        loadPage(model, actor);
        return "account.html";
    }

    private void loadPage(Model model, Account actor) {
        model.addAttribute("account", actor);

        final List<Project> projects = projectService.listProjects(actor);

        final List<String> fieldNames = new ArrayList<>();
        //import external ID field name from import plugins list
        if (projectImportServices != null) {
            projectImportServices.forEach(service -> {
                fieldNames.add(service.getServiceName());
            });
        }

        final Set<Integer> yearsSinceHiring = new LinkedHashSet<>();
        final Map<Integer, String> monthsSinceHiring = new LinkedHashMap<>();
        final Calendar end = Calendar.getInstance();
        end.setTime(actor.getBeginWorkDate());
        final Calendar start = Calendar.getInstance();
        start.setTime(new Date());
        final DateFormatSymbols dfs = new DateFormatSymbols(Locale.ENGLISH);
        dfs.getLocalPatternChars();
        final String[] months = dfs.getMonths();
        for (int i = start.get(Calendar.MONTH);
             start.after(end);
             start.add(Calendar.MONTH, -1), i = start.get(Calendar.DAY_OF_MONTH)) {

            yearsSinceHiring.add(start.get(Calendar.YEAR));
            if (monthsSinceHiring.size() < 12) {
                monthsSinceHiring.put(start.get(Calendar.MONTH), months[start.get(Calendar.MONTH)]);
            }
        }

        model.addAttribute("externalTools", fieldNames);
        model.addAttribute("projects", projects);
        model.addAttribute("yearsSinceHiring", yearsSinceHiring);
        model.addAttribute("monthsSinceHiring", monthsSinceHiring);

    }

}
