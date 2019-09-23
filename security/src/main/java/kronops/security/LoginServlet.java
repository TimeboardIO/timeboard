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

import kronops.core.api.UserServiceBP;
import kronops.core.api.exceptions.BusinessException;
import kronops.core.model.User;
import kronops.core.ui.KronopsServlet;
import kronops.core.ui.ViewModel;
import kronops.security.api.LoginService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;
import org.osgi.service.log.LogService;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


@Component(
        service = Servlet.class,
        scope = ServiceScope.PROTOTYPE,
        property = {
                "osgi.http.whiteboard.servlet.pattern=/login",
                "osgi.http.whiteboard.context.select=(osgi.http.whiteboard.context.name=kronops)"
        }
)
public class LoginServlet extends KronopsServlet {

    @Reference
    LogService logService;

    @Reference
    LoginService loginService;

    @Reference
    UserServiceBP userServiceBP;


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

        try {

            this.loginService.logUser(username, password);
            User user = this.userServiceBP.findUserByLogin(username);
            HttpSession session = request.getSession(true);
            session.setAttribute("user", user);

        } catch (BusinessException e) {
            logService.log(LogService.LOG_WARNING, e.getLocalizedMessage());
            response.setStatus(403);
        }


        response.sendRedirect(origin);
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
