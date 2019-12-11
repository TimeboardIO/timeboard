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

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


/**
 * search user  : /search?q={search query}.
 * search user in project : /search?projectID={project id}&q={search query}
 */

@RestController
@RequestMapping("/search")
public class UsersSearchRestController  {

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

        List<Account> accounts = new ArrayList<>();

        if (projectID != null) {
            accounts.addAll(this.userService.searchUserByEmail(query, projectID));
        } else {
            accounts.addAll(this.userService.searchUserByEmail(query));
        }
        SearchResult searchResult = new SearchResult(accounts.size(), accounts);

        MAPPER.writeValue(resp.getWriter(), searchResult);
    }

    public static class SearchResult {

        private Integer count;
        private List<? extends Serializable> items;

        public SearchResult(Integer count, List<? extends Serializable> items) {
            this.count = count;
            this.items = items;
        }

        public Integer getCount() {
            return count;
        }

        public void setCount(Integer count) {
            this.count = count;
        }

        public List<? extends Serializable> getItems() {
            return items;
        }

        public void setItems(List<? extends Serializable> items) {
            this.items = items;
        }
    }
}
