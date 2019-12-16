package timeboard.webapp;

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
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import timeboard.core.api.OrganizationService;
import timeboard.core.model.Account;
import timeboard.core.ui.UserInfo;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping(OrganizationSelectController.URI)
public class OrganizationSelectController {

    public static final String URI = "/select";
    public static final String COOKIE_NAME = "org";

    @Autowired
    private OrganizationService organizationService;

    @Autowired
    private UserInfo userInfo;

    @GetMapping
    public String selectOrganisation(Model model){

        final List<Account> orgs = this.organizationService.getParents(userInfo.getCurrentAccount(), userInfo.getCurrentAccount());
        orgs.add(this.userInfo.getCurrentAccount());
        model.addAttribute("organizations", orgs);

        return "org_select";
    }

    @PostMapping
    public String selectOrganisation(@ModelAttribute("organization") Long selectedOrgID, HttpServletResponse res){

        final Optional<Account> selectedOrg = this.organizationService.getOrganizationByID(this.userInfo.getCurrentAccount(), selectedOrgID);

        if(selectedOrg.isPresent()) {
            final Cookie orgCookie = new Cookie(COOKIE_NAME, String.valueOf(selectedOrg.get().getId()));
            res.addCookie(orgCookie);
        }
        return "redirect:/home";
    }
}
