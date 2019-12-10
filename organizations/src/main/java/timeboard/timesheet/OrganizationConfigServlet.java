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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import timeboard.core.api.EncryptionService;

import timeboard.core.api.OrganizationService;
import timeboard.core.api.exceptions.BusinessException;
import timeboard.core.model.Account;
import timeboard.core.model.MembershipRole;
import timeboard.core.model.*;
import timeboard.core.ui.TimeboardServlet;
import timeboard.core.ui.ViewModel;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Display Organization details form.
 *
 * <p>Ex : /orga/config?id=
 */
@WebServlet(name = "OrganizationConfigServlet", urlPatterns = "/orga/config")
public class OrganizationConfigServlet extends TimeboardServlet {

    @Autowired
    public OrganizationService OrganizationService;

    @Autowired
    public EncryptionService encryptionService;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    protected ClassLoader getTemplateResolutionClassLoader() {
        return OrganizationConfigServlet.class.getClassLoader();
    }




    @Override
    protected void handleGet(Account actor, HttpServletRequest request, HttpServletResponse response, ViewModel viewModel) throws ServletException, IOException, BusinessException  {


        viewModel.setTemplate("details_organization_config.html");
        long id = Long.parseLong(request.getParameter("organizationID"));

        viewModel.getViewDatas().put("", null);
    }

    @Override
    protected void handlePost(Account actor, HttpServletRequest request, HttpServletResponse response, ViewModel viewModel) throws Exception {

        viewModel.setTemplate("details_Organization_config.html");
/*
        //Extract organization
        long id = Long.parseLong(request.getParameter("organizationID"));
        Organization organization = this.OrganizationService.getOrganizationByIdWithAllMembers(actor, id);
        organization.setName(request.getParameter("OrganizationName"));
        organization.setComments(request.getParameter("OrganizationDescription"));
        organization.setQuotation(Double.parseDouble(request.getParameter("OrganizationQuotation")));

        //Extract organization configuration
        organization.getAttributes().clear();

        //new attributes
        String newAttrKey = request.getParameter("newAttrKey");
        String newAttrValue = request.getParameter("newAttrValue");
        Boolean newAttrEncrypted = false;
        if (request.getParameter("newAttrEncrypted") != null && request.getParameter("newAttrEncrypted").equals("on")) {
            newAttrEncrypted = true;
        }

        if (!newAttrKey.isEmpty()) {
            if (newAttrEncrypted) {
                newAttrValue = this.encryptionService.encryptAttribute(newAttrValue);
            }
            organization.getAttributes().put(newAttrKey, new OrganizationAttributValue(newAttrValue, newAttrEncrypted));
        }

        //Attribute update
        Enumeration<String> params1 = request.getParameterNames();
        while (params1.hasMoreElements()) {
            String param = params1.nextElement();
            if (param.startsWith("attr-")) {
                String key = param.substring(5, param.length());
                String value = request.getParameter(param);
                organization.getAttributes().put(key, new OrganizationAttributValue(value));
            }
            if (param.startsWith("attrenc-")) {
                String key = param.substring(8, param.length());
                String encrypted = request.getParameter(param);
                organization.getAttributes().get(key).setEncrypted(true);
                // organization.getAttributes().get(key).setEncrypted(Boolean.getBoolean(encrypted));
            }
        }

        //Extract memberships from request
        Map<Long, MembershipRole> memberships = new HashMap<>();
        Enumeration<String> params = request.getParameterNames();
        while (params.hasMoreElements()) {
            String param = params.nextElement();
            if (param.startsWith("members")) {
                String key = param.substring(param.indexOf('[') + 1, param.indexOf(']'));
                String value = request.getParameter(param);
                if (!value.isEmpty()) {
                    memberships.put(Long.parseLong(key), MembershipRole.valueOf(value));
                } else {
                    memberships.put(Long.parseLong(key), MembershipRole.CONTRIBUTOR);
                }
            }
        }

        this.OrganizationService.updateOrganization(actor, organization, memberships);

        Map<String, Object> map = new HashMap<>();
        prepareTemplateData(actor, organization, map);

        viewModel.getViewDatas().putAll(map);
    */}
}
