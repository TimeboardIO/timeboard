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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import timeboard.core.model.User;

import javax.annotation.PostConstruct;
import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;



@WebFilter(urlPatterns = {"/*"})
public class OAuthSecurityFilter implements Filter {

    private static final Logger LOGGER = LoggerFactory.getLogger(OAuthSecurityFilter.class);


    @Value("${timeboard.oauth.loginURL}")
    private String loginURLPrefix;

    @Value("${timeboard.oauth.clientId}")
    private String clientId;

    @Value("${timeboard.oauth.redirect.uri}")
    private String redirectURI;


    @Autowired
    private HttpSecurityContextService securityContextService;


    private String loginURL;

    @PostConstruct
    private void activate() {
        LOGGER.info("Security Filter is activated");
        this.loginURL = String.format(
                this.loginURLPrefix + "?response_type=code&client_id=%s&redirect_uri=%s",
                this.clientId,
                this.redirectURI
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
    ||
        req.getServletPath().startsWith("/webjars")
                        || req.getServletPath().equals("/favicon.ico");

        User user = this.securityContextService.getCurrentUser(req);
        boolean isLogged = user != null;

        if (!isLogged && !isStatic && !isLogin) {
            String origin = ((HttpServletRequest) request).getRequestURI() + "?" + ((HttpServletRequest) request).getQueryString();
            origin = URLEncoder.encode(origin, StandardCharsets.UTF_8.toString());
            res.sendRedirect(this.loginURL);
        } else {
            request.setAttribute("actor", user);
            chain.doFilter(request, response);
        }
    }

    @Override
    public void destroy() {

    }
}
