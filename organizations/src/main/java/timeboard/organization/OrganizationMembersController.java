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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import timeboard.core.security.TimeboardAuthentication;
import timeboard.core.api.OrganizationService;
import timeboard.core.model.Account;
import timeboard.core.model.MembershipRole;
import timeboard.core.model.Organization;

import java.util.Optional;


/**
 * Display Organization members form.
 *
 * <p>Ex : /org/members
 */
@Controller
@RequestMapping("/org/members")
public class OrganizationMembersController {

    @Autowired
    public OrganizationService organizationService;

    @GetMapping
    protected String handleGet(TimeboardAuthentication authentication,  Model viewModel) {

        final Account actor = authentication.getDetails();

        final Optional<Organization> organization =
                this.organizationService.getOrganizationByID(actor, authentication.getCurrentOrganization());

        if(organization.isPresent()) {
            viewModel.addAttribute("members", organization.get().getMembers());
        }

        viewModel.addAttribute("roles", MembershipRole.values());
        viewModel.addAttribute("organization", organization.get());

        return "details_org_members";
    }
}
