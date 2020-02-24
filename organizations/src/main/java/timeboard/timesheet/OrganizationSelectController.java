package timeboard.timesheet;

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
import timeboard.core.api.OrganizationService;
import timeboard.core.model.Organization;
import timeboard.core.security.TimeboardAuthentication;

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

    @Value("${app.domain}")
    private String appDomain;

    @GetMapping
    public String selectOrganisation(final TimeboardAuthentication authentication,
                                     final HttpServletRequest req, final TimeboardAuthentication p, final Model model) {


        final List<Organization> orgs = authentication.getDetails().getOrganizationMemberships()
                .stream().map(organizationMembership -> organizationMembership.getOrganization()).collect(Collectors.toList());

        model.addAttribute("organizations", orgs);

        return "org_select";
    }

    @PostMapping
    public String selectOrganisation(final TimeboardAuthentication authentication,
                                     @ModelAttribute("organization") final Long selectedOrgID, final HttpServletResponse res) {

        final Optional<Organization> selectedOrg =
                this.organizationService.getOrganizationByID(authentication.getDetails(), selectedOrgID);

        if (selectedOrg.isPresent()) {
            final Cookie orgCookie = new Cookie(COOKIE_NAME, String.valueOf(selectedOrg.get().getId()));
            orgCookie.setMaxAge(60 * 60 * 24 * 365 * 10);
            orgCookie.setDomain(this.appDomain);
            res.addCookie(orgCookie);
        }
        return "redirect:/home";
    }
}
