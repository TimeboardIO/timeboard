package timeboard.core.ui;

/*-
 * #%L
 * webui
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

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.log.LogService;
import timeboard.core.ui.HttpSecurityContext;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Component(
        service = Filter.class,
        property = {
                "osgi.http.whiteboard.filter.regex=/*",
                "osgi.http.whiteboard.context.select=(osgi.http.whiteboard.context.name=timeboard)",
                "oauth.login.url=https://timeboard.auth.eu-west-1.amazoncognito.com/login",
                "oauth.clientid=changeme",
                "oauth.redirect.uri=http://localhost:8181/signin",
                "timeboard.security.newPassword-url=/newPassword"
        },
        configurationPid = {"timeboard.oauth"}
)
public class OAuthSecurityFilter implements Filter {


    private String loginURL;


    @Reference
    private LogService logService;

    @Activate
    private void activate(Map<String, String> properties) {
        this.logService.log(LogService.LOG_INFO, "Security Filter is activated");
        this.loginURL = String.format(
                properties.get("oauth.login.url") + "?response_type=code&client_id=%s&redirect_uri=%s",
                properties.get("oauth.clientid"),
                properties.get("oauth.redirect.uri")
                );
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        boolean isLogin = req.getServletPath().equals(this.loginURL) || req.getServletPath().equals("/signin");
        boolean isStatic =
                req.getServletPath().startsWith("/static")
                        || req.getServletPath().equals("/favicon.ico");

        boolean isLogged = HttpSecurityContext.getCurrentUser(req) != null;

        if (!isLogged && !isStatic && !isLogin) {
            String origin = ((HttpServletRequest) request).getRequestURI() + "?" + ((HttpServletRequest) request).getQueryString();
            origin = URLEncoder.encode(origin, StandardCharsets.UTF_8.toString());
            res.sendRedirect(this.loginURL);
        } else {
            chain.doFilter(request, response);
        }
    }

    @Override
    public void destroy() {

    }
}
