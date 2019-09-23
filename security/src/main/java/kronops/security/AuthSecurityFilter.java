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

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.log.LogService;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

@Component(
        service = Filter.class,
        property = {
                "osgi.http.whiteboard.filter.regex=/*",
                "osgi.http.whiteboard.context.select=(osgi.http.whiteboard.context.name=kronops)",
                "kronops.security.login-url=/login",
                "kronops.security.logout-url=/logout"
        }
)
public class AuthSecurityFilter implements Filter {

    private String loginURL;
    private String logoutURL;


    @Reference
    private LogService logService;

    @Activate
    private void activate(Map<String, String> properties) {
        this.logService.log(LogService.LOG_INFO, "Security Filter is activated");
        this.loginURL = properties.get("kronops.security.login-url");
        this.logoutURL = properties.get("kronops.security.logout-url");
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        boolean isLogin = req.getServletPath().equals(this.loginURL);
        boolean isStatic =
                req.getServletPath().startsWith("/static")
                        || req.getServletPath().equals("/favicon.ico");

        boolean isLogged = req.getSession(false) != null;
        if (isLogged) {
            isLogged = req.getSession(false).getAttribute("user") != null;
        }


        if (!isLogged && !isStatic && !isLogin) {
            res.sendRedirect(this.loginURL + "?origin=" + ((HttpServletRequest) request).getRequestURI() + "?" + ((HttpServletRequest) request).getQueryString());
        } else {
            chain.doFilter(request, response);
        }
    }

    @Override
    public void destroy() {

    }
}
