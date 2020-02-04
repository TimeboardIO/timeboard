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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import timeboard.core.api.OrganizationService;
import timeboard.core.api.ProjectService;
import timeboard.core.api.UserService;
import timeboard.core.api.exceptions.BusinessException;
import timeboard.core.model.Account;
import timeboard.core.model.Organization;
import timeboard.core.model.Project;
import timeboard.core.security.TimeboardAuthentication;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.Serializable;
import java.util.*;


/**
 * search user  : /api/search?q={search query}.
 * search user in project : /api/search?projectID={project id}&q={search query}
 */

@RestController
@RequestMapping("/api/search")
public class UsersSearchRestController {

    @Autowired
    private UserService userService;

    @Autowired
    private OrganizationService organizationService;

    @Autowired
    private ProjectService projectService;

    @GetMapping
    protected ResponseEntity<SearchResults> doGet(
            final TimeboardAuthentication authentication,
            final HttpServletRequest req,
            final HttpServletResponse resp) throws BusinessException {

        final String query = req.getParameter("q");
        final Account actor = authentication.getDetails();

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

        final Set<Account> accounts = new HashSet<>();

        if (projectID != null) {
            final Project project = projectService.getProjectByIdWithAllMembers(actor, projectID);
            accounts.addAll(this.userService.searchUserByEmail(actor, query, project));
        } else if (orgID != null) {
            final Optional<Organization> org = organizationService.getOrganizationByID(actor, orgID);
            accounts.addAll(this.userService.searchUserByEmail(actor, query, org.get()));
        } else {
            accounts.addAll(this.userService.searchUserByEmail(actor, query));
        }

        final SearchResults searchResults = new SearchResults(accounts.size(), accounts);

        return ResponseEntity.ok(searchResults);
    }

    @GetMapping("/byRole")
    protected ResponseEntity<SearchResults> doGetMembersProjects(
            final TimeboardAuthentication authentication,
            final HttpServletRequest req, final HttpServletResponse resp) throws BusinessException {

        final Account actor = authentication.getDetails();

        final String query = req.getParameter("q");
        if (query.isBlank() || query.isEmpty()) {
            throw new BusinessException("Query is empty");
        }

        Long orgID = null;
        if (req.getParameter("orgID") != null) {
            orgID = authentication.getCurrentOrganization();
        }

        final List<Account> accounts = new ArrayList<>();
        if (orgID != null) {
            final List<Account> ownersOfAnyUserProject = this.projectService.findOwnersOfAnyUserProject(actor);
            accounts.addAll(ownersOfAnyUserProject);

        } else {
            throw new BusinessException("OrganizationID is null");
        }

        final SearchResults searchResults = new SearchResults(accounts.size(), accounts);

        return ResponseEntity.ok(searchResults);
    }

    public static class SearchResult implements Serializable {
        private Long id;
        private String screenName;

        public SearchResult(final Account a) {
            this.id = a.getId();
            this.screenName = a.getScreenName();
        }

        public Long getId() {
            return id;
        }

        public void setId(final Long id) {
            this.id = id;
        }

        public String getScreenName() {
            return screenName;
        }

        public void setScreenName(final String screenName) {
            this.screenName = screenName;
        }

    }

    public static class SearchResults {

        private Integer count;
        private Collection<SearchResult> items;

        public SearchResults(final Integer count, final Collection<Account> items) {
            this.count = count;
            this.items = new ArrayList<>();

            for (final Account a : items) {
                this.items.add(new SearchResult(a));
            }
        }

        public Integer getCount() {
            return count;
        }

        public void setCount(final Integer count) {

            this.count = count;
        }

        public Collection<SearchResult> getItems() {
            return items;
        }

        public void setItems(final List<SearchResult> items) {
            this.items = items;
        }
    }
}
