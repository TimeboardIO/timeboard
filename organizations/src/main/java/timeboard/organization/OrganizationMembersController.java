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

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import timeboard.core.api.AbacEntries;
import timeboard.core.api.AccountService;
import timeboard.core.api.OrganizationService;
import timeboard.core.api.exceptions.BusinessException;
import timeboard.core.model.Account;
import timeboard.core.model.MembershipRole;
import timeboard.core.model.Organization;
import timeboard.core.model.OrganizationMembership;
import timeboard.core.security.TimeboardAuthentication;

import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;
import java.util.*;


/**
 * Display Organization members form.
 *
 * <p>Ex : /org/members
 */
@Controller
@RequestMapping(value = "/org/members", produces = MediaType.APPLICATION_JSON_VALUE)
public class OrganizationMembersController {

    @Autowired
    public OrganizationService organizationService;

    @Autowired
    private AccountService accountService;

    @GetMapping
    protected String handleGet(final TimeboardAuthentication authentication, final Model viewModel) {

        viewModel.addAttribute("members", authentication.getCurrentOrganization().getMembers());
        viewModel.addAttribute("roles", MembershipRole.values());
        viewModel.addAttribute("organization", authentication.getCurrentOrganization());

        return "org_members";
    }

    @GetMapping("/list")
    @PreAuthorize("hasPermission(null, '" + AbacEntries.ORG_SETUP + "')")
    public ResponseEntity getMembers(final TimeboardAuthentication authentication) {

        final Set<OrganizationMembership> members = authentication.getCurrentOrganization().getMembers();
        final List<MemberWrapper> result = new ArrayList<>();

        for (OrganizationMembership member : members) {

            result.add(new MemberWrapper(
                    member.getMember().getId(),
                    member.getMember().getScreenName(),
                    member.getRole() != null ? member.getRole().name() : "",
                    member.getCreationDate()
            ));
        }

        return ResponseEntity.status(HttpStatus.OK).body(result.toArray());

    }

    @PatchMapping(value = "/{member}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity updateMemberRole(
            final TimeboardAuthentication authentication,
            final @PathVariable Account member,
            final @RequestBody MemberWrapper membershipWrapper) throws BusinessException {


        final Optional<OrganizationMembership> membershipOpt = this.organizationService
                .findOrganizationMembership(member, authentication.getCurrentOrganization());

        if (membershipOpt.isPresent()) {

            final OrganizationMembership membership = membershipOpt.get();
            membership.setRole(MembershipRole.valueOf(membershipWrapper.role));
            membership.setCreationDate(membershipWrapper.creationDate);

            final Optional<Organization> updatedOrgMembership = organizationService
                    .updateMembership(authentication.getDetails(), membership);

            return ResponseEntity.status(HttpStatus.OK).body(updatedOrgMembership.get());
        }

        return ResponseEntity.badRequest().build();

    }


    @GetMapping("/add")
    public ResponseEntity addMember(TimeboardAuthentication authentication,
                                    HttpServletRequest request) throws JsonProcessingException {

        final Account actor = authentication.getDetails();

        // Get current organization
        final Organization org = authentication.getCurrentOrganization();


        // Get added member
        final String strMemberID = request.getParameter("memberID");
        Long memberID = null;
        if (strMemberID != null) {
            memberID = Long.parseLong(strMemberID);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Incorrect org member argument");
        }
        final Account member = this.accountService.findUserByID(memberID);

        // Add member in current organization
        try {
            organizationService.addMembership(actor, org, member, MembershipRole.CONTRIBUTOR);

            final MemberWrapper memberWrapper = new MemberWrapper(
                    memberID,
                    member.getScreenName(),
                    MembershipRole.CONTRIBUTOR.toString(),
                    Calendar.getInstance());

            return ResponseEntity.status(HttpStatus.OK).body(memberWrapper);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }


    }

    @GetMapping("/remove/{member}")
    public ResponseEntity removeMember(final TimeboardAuthentication authentication,
                                       @PathVariable final Account member) throws BusinessException {

        final Account actor = authentication.getDetails();


        if (member == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Incorrect org member argument");
        }

        try {
            organizationService.removeMembership(actor, authentication.getCurrentOrganization(), member);

            return ResponseEntity.status(HttpStatus.OK).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }


    public static class MemberWrapper implements Serializable {

        public Long id;
        public String screenName;
        public String role;

        @JsonFormat(pattern = "yyyy-MM-dd")
        public java.util.Calendar creationDate;

        public MemberWrapper() {
        }

        public MemberWrapper(OrganizationMembership h) {
            this.id = h.getMember().getId();
            this.screenName = h.getMember().getScreenName();
            this.role = h.getRole().name();
            this.creationDate = h.getCreationDate();
        }

        public MemberWrapper(Long memberID, String screenName, String role, java.util.Calendar date) {
            this.id = memberID;
            this.screenName = screenName;
            this.role = role;
            this.creationDate = date;
        }

    }

}
