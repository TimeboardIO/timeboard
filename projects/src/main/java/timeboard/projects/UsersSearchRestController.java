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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import timeboard.core.api.UserService;
import timeboard.core.model.Account;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Serializable;
import java.util.*;


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

    @GetMapping
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String query = req.getParameter("q");

        Long projectID = null;
        if (req.getParameter("projectID") != null) {
            projectID = Long.parseLong(req.getParameter("projectID"));
        }

        Set<Account> accounts = new HashSet<>();

        if (projectID != null) {
            accounts.addAll(this.userService.searchUserByEmail(query, projectID));
        } else {
            accounts.addAll(this.userService.searchUserByEmail(query));
            accounts.addAll(this.userService.searchUserByName(query));
        }
        SearchResults searchResults = new SearchResults(accounts.size(), accounts);

        MAPPER.writeValue(resp.getWriter(), searchResults);
    }

    public static class SearchResult implements Serializable{
        private Long id;

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

        private String screenName;

    }
    public static class SearchResults {

        private Integer count;
        private Collection<SearchResult> items;

        public SearchResults(Integer count, Collection<Account> items) {
            this.count = count;
            this.items = new ArrayList<>();

            for (Account a : items){
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
