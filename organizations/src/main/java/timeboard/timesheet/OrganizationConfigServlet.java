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
import timeboard.core.api.EncryptionService;

import timeboard.core.api.OrganizationService;
import timeboard.core.api.exceptions.BusinessException;
import timeboard.core.model.Account;
import timeboard.core.ui.TimeboardServlet;
import timeboard.core.ui.ViewModel;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;


/**
 * Display Organization details form.
 *
 * <p>Ex : /org/config?id=
 */
@WebServlet(name = "OrganizationConfigServlet", urlPatterns = "/org/config")
public class OrganizationConfigServlet extends TimeboardServlet {

    @Autowired
    public OrganizationService organizationService;

    @Autowired
    public EncryptionService encryptionService;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    protected ClassLoader getTemplateResolutionClassLoader() {
        return OrganizationConfigServlet.class.getClassLoader();
    }

    @Override
    protected void handleGet(Account actor, HttpServletRequest request, HttpServletResponse response, ViewModel viewModel) throws ServletException, IOException, BusinessException  {


        viewModel.setTemplate("details_org_config.html");
        long id = Long.parseLong(request.getParameter("orgID"));

        Account organization = this.organizationService.getOrganizationByID(actor, id);

        List<Account> parents = this.organizationService.getParents(actor, organization);
        List<Account> members = this.organizationService.getMembers(actor, organization);

        viewModel.getViewDatas().put("parents", parents);
        viewModel.getViewDatas().put("members", members);
        viewModel.getViewDatas().put("organization", organization);

    }

    @Override
    protected void handlePost(Account actor, HttpServletRequest request, HttpServletResponse response, ViewModel viewModel) throws Exception {

        viewModel.setTemplate("details_org_config.html");

        //Extract organization
        long id = Long.parseLong(request.getParameter("organizationID"));
        Account organization = this.organizationService.getOrganizationByID(actor, id);

        organization.setName(request.getParameter("OrganizationName"));


        this.organizationService.updateOrganization(actor, organization);

    }
}
