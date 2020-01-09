package timeboard.organization;

/*-
 * #%L
 * webapp
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import timeboard.core.security.TimeboardAuthentication;
import timeboard.core.api.OrganizationService;
import timeboard.core.model.Organization;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping(OrganizationSelectController.URI)
public class OrganizationSelectController {

    public static final String URI = "/select";
    public static final String COOKIE_NAME = "org";

    @Autowired
    private OrganizationService organizationService;

    @Value("${timeboard.organizations.default}")
    private String defaultOrganisationName;

    @GetMapping
    public String selectOrganisation(TimeboardAuthentication authentication,
                                     HttpServletRequest req, TimeboardAuthentication p, Model model){


        final List<Organization> orgs = authentication.getDetails().getOrganizations()
                .stream().map(organizationMembership -> organizationMembership.getOrganization()).collect(Collectors.toList());

        orgs.add(this.organizationService.getOrganizationByName(this.defaultOrganisationName).get());

        model.addAttribute("organizations", orgs);

        return "org_select";
    }

    @PostMapping
    public String selectOrganisation(TimeboardAuthentication authentication,
                                     @ModelAttribute("organization") Long selectedOrgID, HttpServletResponse res){

        final Optional<Organization> selectedOrg =
                this.organizationService.getOrganizationByID(authentication.getDetails(), selectedOrgID);

        if(selectedOrg.isPresent()) {
            final Cookie orgCookie = new Cookie(COOKIE_NAME, String.valueOf(selectedOrg.get().getId()));
            res.addCookie(orgCookie);
        }
        return "redirect:/home";
    }
}
