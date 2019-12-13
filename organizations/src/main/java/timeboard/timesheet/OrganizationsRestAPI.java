package timeboard.timesheet;

/*-
 * #%L
 * reporting
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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import timeboard.core.api.OrganizationService;
import timeboard.core.api.UserService;
import timeboard.core.model.Account;
import timeboard.core.model.AccountHierarchy;
import timeboard.core.model.MembershipRole;
import timeboard.core.ui.UserInfo;

import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
@RestController
@RequestMapping(value = "/api/org", produces = MediaType.APPLICATION_JSON_VALUE)
public class OrganizationsRestAPI {

    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Autowired
    private OrganizationService organizationService;

    @Autowired
    private UserService userService;

    @Autowired
    private UserInfo userInfo;


    @GetMapping("/members")
    public ResponseEntity getMembers(HttpServletRequest request) throws JsonProcessingException {
        Account actor = this.userInfo.getCurrentAccount();

        final String strOrgID = request.getParameter("orgID");
        Long orgID = null;
        if (strOrgID != null) {
            orgID = Long.parseLong(strOrgID);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Incorrect org id argument");
        }

        final Account organization  = this.organizationService.getOrganizationByID(actor, orgID);

        if (organization == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Project does not exists or you don't have enough permissions to access it.");
        }

        //final List<Account> members = this.organizationService.getMembers(actor, organization);

        final Set<AccountHierarchy> members = organization.getMembers();
        final List<MemberWrapper> result = new ArrayList<>();

        for (AccountHierarchy member : members) {

            result.add(new MemberWrapper(
                    member.getMember().getId(),
                    member.getMember().getScreenName(),
                    member.getMember().getIsOrganization(),
                    (member.getRole() != null ? member.getRole().name() : "")
                    ));
        }

        return ResponseEntity.status(HttpStatus.OK).body(MAPPER.writeValueAsString(result.toArray()));

    }

    @GetMapping("/members/add")
    public ResponseEntity addMember(HttpServletRequest request) throws JsonProcessingException {
        Account actor = this.userInfo.getCurrentAccount();

        final String strOrgID = request.getParameter("orgID");
        Long orgID = null;
        if (strOrgID != null) {
            orgID = Long.parseLong(strOrgID);
        }else{
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Incorrect org id argument");
        }
        final Account organization  = this.organizationService.getOrganizationByID(actor, orgID);

        final String strMemberID = request.getParameter("memberID");
        Long memberID = null;
        if (strMemberID != null) {
            memberID = Long.parseLong(strMemberID);
        }else{
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Incorrect org member argument");
        }

        final Account member = this.organizationService.getOrganizationByID(actor,  memberID);

        try {
           AccountHierarchy ah = organizationService.addMember(actor, organization, member);
            return ResponseEntity.status(HttpStatus.OK).body(MAPPER.writeValueAsString(new MemberWrapper(ah)));
        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }


    }



    @GetMapping("/members/updateRole")
    public ResponseEntity updateMemberRole(HttpServletRequest request) throws JsonProcessingException {
        Account actor = this.userInfo.getCurrentAccount();

        final String strOrgID = request.getParameter("orgID");
        Long orgID = null;
        if (strOrgID != null) {
            orgID = Long.parseLong(strOrgID);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Incorrect org id argument");
        }
        final Account organization  = this.organizationService.getOrganizationByID(actor, orgID);

        final String strMemberID = request.getParameter("memberID");
        Long memberID = null;
        if (strMemberID != null) {
            memberID = Long.parseLong(strMemberID);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Incorrect org member argument");
        }
        final Account member = this.organizationService.getOrganizationByID(actor,  memberID);


        final String strRole = request.getParameter("role");
        MembershipRole role = null;
        if (strRole != null) {
            role = MembershipRole.valueOf(strRole);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Incorrect org member argument");
        }

        try {
            AccountHierarchy ah = organizationService.updateMemberRole(actor, organization, member, role);
            return ResponseEntity.status(HttpStatus.OK).body(MAPPER.writeValueAsString(new MemberWrapper(ah)));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }

    }


    @GetMapping("/members/remove")
    public ResponseEntity removeMember(HttpServletRequest request){
        Account actor = this.userInfo.getCurrentAccount();

        final String strOrgID = request.getParameter("orgID");
        Long orgID = null;
        if (strOrgID != null) {
            orgID = Long.parseLong(strOrgID);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Incorrect org id argument");
        }
        final Account organization  = this.organizationService.getOrganizationByID(actor, orgID);

        final String strMemberID = request.getParameter("memberID");
        Long memberID = null;
        if (strOrgID != null) {
            memberID = Long.parseLong(strMemberID);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Incorrect org member argument");
        }

        final Account member = this.userService.findUserByID(memberID);

        try {
            organizationService.removeMember(actor, organization, member);
        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    public static class MemberWrapper implements Serializable {

        public Long orgID;
        public String screenName;
        public boolean isOrganization;
        public String role;

        public MemberWrapper(){}
        public MemberWrapper(AccountHierarchy h){
            this.orgID = h.getMember().getId();
            this.screenName = h.getMember().getScreenName();
            this.isOrganization = h.getMember().getIsOrganization();
            this.role = h.getRole().name();
        }

        public MemberWrapper(Long orgID, String screenName, boolean isOrganization, String role) {
            this.orgID = orgID;
            this.screenName = screenName;
            this.isOrganization = isOrganization;
            this.role = role;
        }

    }
}
