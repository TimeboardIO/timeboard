package timeboard.organization;

/*-
 * #%L
 * reports
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
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import timeboard.core.security.TimeboardAuthentication;
import timeboard.core.api.OrganizationService;
import timeboard.core.api.UserService;
import timeboard.core.model.Account;
import timeboard.core.model.MembershipRole;
import timeboard.core.model.Organization;
import timeboard.core.model.OrganizationMembership;

import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

@Component
@RestController
@RequestMapping(value = "/org", produces = MediaType.APPLICATION_JSON_VALUE)
public class OrganizationsRestAPI {

    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Autowired
    private OrganizationService organizationService;

    @Autowired
    private UserService userService;

    @GetMapping("/members/list")
    public ResponseEntity getMembers(TimeboardAuthentication authentication,
                                     HttpServletRequest request) throws JsonProcessingException {

        Account actor = authentication.getDetails();

        final String strOrgID = request.getParameter("orgID");
        Long orgID = null;
        if (strOrgID != null) {
            orgID = Long.parseLong(strOrgID);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Incorrect org id argument");
        }

        final Optional<Organization> organization = this.organizationService.getOrganizationByID(actor, orgID);

        if (!organization.isPresent()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Project does not exists or you don't have enough permissions to access it.");
        }


        final Set<OrganizationMembership> members = organization.get().getMembers();
        final List<MemberWrapper> result = new ArrayList<>();

        for (OrganizationMembership member : members) {

            result.add(new MemberWrapper(
                    member.getId(),
                    member.getMember().getScreenName(),
                    (member.getRole() != null ? member.getRole().name() : ""),
                    member.getCreationDate()
            ));
        }

        return ResponseEntity.status(HttpStatus.OK).body(MAPPER.writeValueAsString(result.toArray()));

    }

    @GetMapping("/members/add")
    public ResponseEntity addMember(TimeboardAuthentication authentication,
                                    HttpServletRequest request) throws JsonProcessingException {

        Account actor = authentication.getDetails();

        // Get current organization
        final String strOrgID = request.getParameter("orgID");
        Long orgID = null;
        if (strOrgID != null) {
            orgID = Long.parseLong(strOrgID);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Incorrect org id argument");
        }
        final Optional<Organization> organization = this.organizationService.getOrganizationByID(actor, orgID);

        // Get added member
        final String strMemberID = request.getParameter("memberID");
        Long memberID = null;
        if (strMemberID != null) {
            memberID = Long.parseLong(strMemberID);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Incorrect org member argument");
        }
        final Account member = this.userService.findUserByID(memberID);

        // Add member in current organization
        try {
            Optional<Organization> newOrganization = organizationService.addMember(actor, organization.get(), member, MembershipRole.CONTRIBUTOR);
            MemberWrapper memberWrapper = new MemberWrapper(memberID, member.getScreenName(), MembershipRole.CONTRIBUTOR.toString(), new Date());
            return ResponseEntity.status(HttpStatus.OK).body(memberWrapper);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }


    }





    @GetMapping("/members/remove")
    public ResponseEntity removeMember(TimeboardAuthentication authentication,
                                       HttpServletRequest request) {

        Account actor = authentication.getDetails();

        final String strOrgID = request.getParameter("orgID");
        Long orgID = null;
        if (strOrgID != null) {
            orgID = Long.parseLong(strOrgID);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Incorrect org id argument");
        }
        final Optional<Organization> organization = this.organizationService.getOrganizationByID(actor, orgID);

        final String strMemberID = request.getParameter("memberID");
        Long memberID = null;
        if (strOrgID != null) {
            memberID = Long.parseLong(strMemberID);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Incorrect org member argument");
        }

        final Account member = this.userService.findUserByID(memberID);

        try {
            Optional<Organization> newOrganization = organizationService.removeMember(actor, organization.get(), member);

            return ResponseEntity.status(HttpStatus.OK).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    public static class MemberWrapper implements Serializable {

        public Long id;
        public String screenName;
        public String role;

        @JsonFormat(pattern="yyyy-MM-dd")
        public java.util.Date creationDate;

        public MemberWrapper() {
        }

        public MemberWrapper(OrganizationMembership h) {
            this.id = h.getMember().getId();
            this.screenName = h.getMember().getScreenName();
            this.role = h.getRole().name();
            this.creationDate = h.getCreationDate();
        }

        public MemberWrapper(Long memberID, String screenName, String role, java.util.Date date) {
            this.id = memberID;
            this.screenName = screenName;
            this.role = role;
            this.creationDate = date;
        }

    }
}
