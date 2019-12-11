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
import org.springframework.web.bind.annotation.*;
import timeboard.core.api.OrganizationService;
import timeboard.core.api.ProjectService;
import timeboard.core.api.UserService;
import timeboard.core.api.exceptions.BusinessException;
import timeboard.core.model.*;
import timeboard.core.ui.UserInfo;

import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

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
        }else{
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Incorrect org id argument");
        }

        final Account organization  = this.organizationService.getOrganizationByID(actor, orgID);

        if (organization == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Project does not exists or you don't have enough permissions to access it.");
        }

        final List<Account> members = this.organizationService.getMembers(actor, organization);

        final List<MemberWrapper> result = new ArrayList<>();

        for (Account member : members) {

            result.add(new MemberWrapper(
                    member.getId(),
                    member.getScreenName(),
                    member.getIsOrganization(),
                    ""
                    ));

        }

        return ResponseEntity.status(HttpStatus.OK).body(MAPPER.writeValueAsString(result.toArray()));

    }

    @GetMapping("/members/remove")
    public ResponseEntity remove(HttpServletRequest request){
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
        if (strOrgID != null) {
            memberID = Long.parseLong(strMemberID);
        }else{
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

            public MemberWrapper(Long orgID, String screenName, boolean isOrganization, String role) {
                this.orgID = orgID;
                this.screenName = screenName;
                this.isOrganization = isOrganization;
                this.role = role;
            }

    }
}
