package timeboard.projects;

/*-
 * #%L
 * core
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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import timeboard.core.api.OrganizationService;
import timeboard.core.api.ProjectService;
import timeboard.core.api.UserService;
import timeboard.core.api.exceptions.BusinessException;
import timeboard.core.model.*;
import timeboard.core.security.TimeboardAuthentication;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;


/**
 * search user  : /api/search?q={search query}.
 * search user in project : /api/search?projectID={project id}&q={search query}
 */

@RestController
@RequestMapping("/api/search")
public class UsersSearchRestController {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    @Autowired
    private UserService userService;

    @Autowired
    private OrganizationService organizationService;

    @Autowired
    private ProjectService projectService;

    @GetMapping
    protected ResponseEntity<SearchResults> doGet(TimeboardAuthentication authentication,
                                                  HttpServletRequest req, HttpServletResponse resp) throws IOException, BusinessException {

        String query = req.getParameter("q");
        Account actor = authentication.getDetails();

        if (query.isBlank() || query.isEmpty()) {
            throw new BusinessException("Query is empty");
        }

        Long projectID = null;
        if (req.getParameter("projectID") != null) {
            projectID = Long.parseLong(req.getParameter("projectID"));
        }

        Long orgID = null;
        if (req.getParameter("orgID") != null) {
            orgID = authentication.getCurrentOrganization();
        }

        Set<Account> accounts = new HashSet<>();

        if (projectID != null) {
            Project project = projectService.getProjectByIdWithAllMembers(actor, projectID);
            accounts.addAll(this.userService.searchUserByEmail(actor, query, project));
        } else if (orgID != null) {
            Optional<Organization> org = organizationService.getOrganizationByID(actor, orgID);
            accounts.addAll(this.userService.searchUserByEmail(actor, query, org.get()));
        } else {
            accounts.addAll(this.userService.searchUserByEmail(actor, query));
        }

        SearchResults searchResults = new SearchResults(accounts.size(), accounts);

        return ResponseEntity.ok(searchResults);
    }

    @GetMapping("/byRole")
    protected ResponseEntity<SearchResults> doGetMembersProjects(
            final TimeboardAuthentication authentication,
            final HttpServletRequest req, HttpServletResponse resp) throws BusinessException {

        Account actor = authentication.getDetails();

        String query = req.getParameter("q");
        if (query.isBlank() || query.isEmpty()) {
            throw new BusinessException("Query is empty");
        }

        Long orgID = null;
        if (req.getParameter("orgID") != null) {
            orgID = authentication.getCurrentOrganization();
        }

        MembershipRole role = null;
        if (req.getParameter("role") != null) {
            role = req.getParameter("role").equals("OWNER") ? MembershipRole.OWNER : MembershipRole.CONTRIBUTOR;
        }

        List<Account> accounts = new ArrayList<>();
        if (orgID != null) {
            List<Project> myProjects = projectService.listProjects(actor, orgID)
                    .stream()
                    .filter(project -> project.isMember(actor))
                    .collect(Collectors.toList());

            List<Account> myManagers = new ArrayList<>();
            MembershipRole finalRole = role;
            myProjects
                    .stream()
                    .map(project -> project.getMemberShipsByRole(finalRole))
                    .forEach(projectMembershipOwners -> {
                        for (ProjectMembership projectMembershipOwner : projectMembershipOwners) {
                            if (projectMembershipOwner.getMember().getEmail().startsWith(query)
                                    && !myManagers.contains(projectMembershipOwner.getMember())) {
                                myManagers.add(projectMembershipOwner.getMember());
                            }
                        }
                    });

            accounts.addAll(myManagers);

        } else {
            throw new BusinessException("OrganizationID is null");
        }

        SearchResults searchResults = new SearchResults(accounts.size(), accounts);

        return ResponseEntity.ok(searchResults);
    }

    public static class SearchResult implements Serializable {
        private Long id;
        private String screenName;

        public SearchResult(Account a) {
            this.id = a.getId();
            this.screenName = a.getScreenName();
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getScreenName() {
            return screenName;
        }

        public void setScreenName(String screenName) {
            this.screenName = screenName;
        }

    }

    public static class SearchResults {

        private Integer count;
        private Collection<SearchResult> items;

        public SearchResults(Integer count, Collection<Account> items) {
            this.count = count;
            this.items = new ArrayList<>();

            for (Account a : items) {
                this.items.add(new SearchResult(a));
            }
        }

        public Integer getCount() {
            return count;
        }

        public void setCount(Integer count) {

            this.count = count;
        }

        public Collection<SearchResult> getItems() {
            return items;
        }

        public void setItems(List<SearchResult> items) {
            this.items = items;
        }
    }
}
