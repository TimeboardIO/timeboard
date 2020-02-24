package timeboard.organization;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.*;
import timeboard.core.api.AccountService;
import timeboard.core.api.exceptions.BusinessException;

import timeboard.core.model.Account;
import timeboard.core.api.AbacEntries;
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
