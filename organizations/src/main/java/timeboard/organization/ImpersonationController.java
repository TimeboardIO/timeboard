package timeboard.organization;

/*-
 * #%L
 * organizations
 * %%
 * Copyright (C) 2019 - 2020 Timeboard
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import timeboard.core.api.AbacEntries;
import timeboard.core.api.AccountService;
import timeboard.core.api.exceptions.BusinessException;
import timeboard.core.model.Account;
import timeboard.core.security.TimeboardAuthentication;


@Controller
@RequestMapping("/org/impersonate")
public class ImpersonationController {

    @Autowired
    public AccountService accountService;

    @PostMapping(value = "/{account}")
    @PreAuthorize("hasPermission(#user, '" + AbacEntries.ORG_IMPERSONATE + "')")
    protected String handleImpersonation(final TimeboardAuthentication authentication,
                                         final Model model, @PathVariable final Account account) throws BusinessException {

        authentication.setOverriddenAccount(account);

        return "redirect:/home";

    }

    @GetMapping(value = "/cancel")
    protected String cancelImpersonation(final TimeboardAuthentication authentication) throws BusinessException {

        authentication.setOverriddenAccount(null);

        return "redirect:/home";

    }
}
