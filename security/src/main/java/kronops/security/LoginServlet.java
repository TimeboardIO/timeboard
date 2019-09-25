package kronops.security;

/*-
 * #%L
 * webui
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

import kronops.core.api.UserService;
import kronops.core.model.User;
import kronops.core.ui.KronopsServlet;
import kronops.core.ui.ViewModel;
import kronops.security.api.LoginService;
import kronops.security.api.UsernamePasswordCredential;
import org.osgi.service.component.annotations.*;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Component(
        service = Servlet.class,
        scope = ServiceScope.PROTOTYPE,
        property = {
                "osgi.http.whiteboard.servlet.pattern=/login",
                "osgi.http.whiteboard.context.select=(osgi.http.whiteboard.context.name=kronops)"
        }
)
public class LoginServlet extends KronopsServlet {


    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policyOption = ReferencePolicyOption.GREEDY)
    List<LoginService> loginServices;

    @Reference
    UserService userService;


    @Override
    protected ClassLoader getTemplateResolutionClassLoader() {
        return LoginServlet.class.getClassLoader();
    }


    @Override
    protected void handlePost(HttpServletRequest request, HttpServletResponse response, ViewModel viewModel) throws ServletException, IOException {
        viewModel.setTemplate("login.html");
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String origin = request.getParameter("origin");

        final UsernamePasswordCredential usernamePasswordCredential = new UsernamePasswordCredential(username, password);


        boolean logged = this.loginServices.stream()
                .filter(loginService -> loginService.isServiceValidFor(usernamePasswordCredential))
                .map(loginService -> loginService.validateCredential(usernamePasswordCredential))
                .collect(Collectors.toList())
                .contains(true);

        if (logged) {
            User user = this.userService.findUserByLogin(username);
            HttpSession session = request.getSession(true);
            session.setAttribute("user", user);

            origin = URLDecoder.decode(origin, StandardCharsets.UTF_8.toString());
            response.sendRedirect(origin);
        } else {
            response.setStatus(403);
            viewModel.setTemplate("login.html");
        }

    }

    @Override
    protected void handleGet(HttpServletRequest request, HttpServletResponse response, ViewModel viewModel) throws ServletException, IOException {
        viewModel.setTemplate("login.html");
        Map<String, Object> templateDatas = new HashMap<>();
        String origin = "/";
        if (request.getParameter("origin") != null) {
            origin = request.getParameter("origin");
        }
        templateDatas.put("origin", origin);
        viewModel.getViewDatas().putAll(templateDatas);
    }


}
