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
import timeboard.core.api.AccountService;
import timeboard.core.api.OrganizationService;
import timeboard.core.api.ProjectService;
import timeboard.core.api.exceptions.BusinessException;
import timeboard.core.api.sync.ProjectSyncPlugin;
import timeboard.core.model.Account;
import timeboard.core.model.Organization;
import timeboard.core.model.Project;
import timeboard.core.security.TimeboardAuthentication;

import javax.servlet.http.HttpServletRequest;
import java.text.DateFormatSymbols;
import java.util.*;


@Controller
@RequestMapping("/account")
public class AccountController {

    @Autowired
    private ProjectService projectService;

    @Autowired
    private OrganizationService organizationService;

    @Autowired
    private AccountService accountService;


    @Autowired(
            required = false
    )
    private List<ProjectSyncPlugin> projectImportServices;


    @PostMapping
    protected String handlePost(
            final TimeboardAuthentication authentication,
            final HttpServletRequest request,
            final Model model) throws BusinessException {

        final String submitButton = request.getParameter("formType");
        final Account actor = authentication.getDetails();


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
                    this.accountService.updateUser(actor);
                    model.addAttribute("message", "User account changed successfully !");
                } catch (final Exception e) {
                    model.addAttribute("error", "Error while updating user information.");
                }
                break;

            case "external":
                final Enumeration<String> params1 = request.getParameterNames();
                while (params1.hasMoreElements()) {
                    final String param = params1.nextElement();
                    if (param.startsWith("attr-")) {
                        final String key = param.substring(5);
                        final String value = request.getParameter(param);
                        actor.getExternalIDs().put(key, value);
                    }
                }
                try {
                    this.accountService.updateUser(actor);
                    model.addAttribute("message", "External tools updated successfully !");
                } catch (final Exception e) {
                    model.addAttribute("error", "Error while external tools");
                }
                break;


            default:
        }
        loadPage(model, actor, authentication.getCurrentOrganization());
        return "account.html";

    }

    @GetMapping
    protected String handleGet(final TimeboardAuthentication authentication, final Model model) throws BusinessException {
        final Account actor = authentication.getDetails();
        loadPage(model, actor, authentication.getCurrentOrganization());
        return "account.html";
    }

    private void loadPage(final Model model, final Account actor, final Organization org) throws BusinessException {
        model.addAttribute("account", actor);

        final List<Project> projects = projectService.listProjects(actor, org);

        final Map<String, String> fieldNames = new HashMap<>();
        //import external ID field name from import plugins list
        if (projectImportServices != null) {
            projectImportServices.forEach(service -> {
                fieldNames.put(service.getServiceID(), service.getServiceName());
            });
        }

        final Set<Integer> yearsSinceHiring = new LinkedHashSet<>();
        final Map<Integer, String> monthsSinceHiring = new LinkedHashMap<>();
        final Calendar end = this.organizationService.findOrganizationMembership(actor, org).get().getCreationDate();
        final Calendar start = Calendar.getInstance();
        start.setTime(new Date());
        final DateFormatSymbols dfs = new DateFormatSymbols(Locale.ENGLISH);
        dfs.getLocalPatternChars();
        final String[] months = dfs.getMonths();
        for (start.get(Calendar.MONTH);
             start.after(end);
             start.add(Calendar.MONTH, -1), start.get(Calendar.DAY_OF_MONTH)) {

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
