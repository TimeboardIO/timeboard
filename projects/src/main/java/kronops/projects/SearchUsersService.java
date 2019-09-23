package kronops.projects;

/*-
 * #%L
 * core
 * %%
 * Copyright (C) 2019 Kronops
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
import kronops.core.api.UserServiceBP;
import kronops.core.model.User;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Component(
        service = Servlet.class,
        scope = ServiceScope.PROTOTYPE,
        property = {
                "osgi.http.whiteboard.servlet.pattern=/search",
                "osgi.http.whiteboard.context.select=(osgi.http.whiteboard.context.name=kronops)"
        }
)
public class SearchUsersService extends HttpServlet {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    @Reference
    private UserServiceBP userServiceBP;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String query = req.getParameter("q");

        Long projectID = null;
        if (req.getParameter("projectID") != null) {
            projectID = Long.parseLong(req.getParameter("projectID"));
        }

        List<User> users = new ArrayList<>();

        if (projectID != null) {
            users.addAll(this.userServiceBP.searchUserByName(query, projectID));
        } else {
            users.addAll(this.userServiceBP.searchUserByName(query));
        }
        SearchResult searchResult = new SearchResult(users.size(), users);

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
