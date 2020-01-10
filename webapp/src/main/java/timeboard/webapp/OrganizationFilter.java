package timeboard.webapp;

/*-
 * #%L
 * webapp
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
import org.springframework.core.annotation.Order;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import timeboard.core.api.OrganizationService;
import timeboard.core.api.ThreadLocalStorage;
import timeboard.core.model.Organization;
import timeboard.core.security.TimeboardAuthentication;
import timeboard.organization.OrganizationSelectController;

import javax.servlet.*;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@Order(1)
public class OrganizationFilter implements Filter {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrganizationFilter.class);

    @Autowired
    private OrganizationService organizationService;

    private static final List<String> whitelist = new ArrayList<>();

    static {
        whitelist.add(OrganizationSelectController.URI);
        whitelist.add(OnboardingController.URI);
        whitelist.add("/org/create");
        whitelist.add("/login/oauth2/code/cognito");
        whitelist.add(".*(.)(js|css|jpg|png|ttf|woff|woff2|svg)");
    }

    @Override
    public void doFilter(ServletRequest servletRequest,
                         ServletResponse servletResponse,
                         FilterChain filterChain) throws IOException, ServletException {

        if (isWhiteListed((HttpServletRequest) servletRequest)) {
            filterChain.doFilter(servletRequest, servletResponse);
        } else {
            if (processCookieExtraction(
                    (TimeboardAuthentication) SecurityContextHolder.getContext().getAuthentication(),
                    (HttpServletRequest) servletRequest,
                    (HttpServletResponse) servletResponse)) {
                return;
            }

            filterChain.doFilter(servletRequest, servletResponse);
        }
    }

    private boolean processCookieExtraction(TimeboardAuthentication authentication,
                                            HttpServletRequest servletRequest,
                                            HttpServletResponse servletResponse) throws IOException {

        Optional<Cookie> orgCookie = this.extractOrgCookie(servletRequest);
        if (orgCookie.isPresent()) {
            try {
                final Long organizationID = Long.parseLong(orgCookie.get().getValue());

                Optional<Organization> organization = this.organizationService
                        .getOrganizationByID(authentication.getDetails(), organizationID);

                if (organization.isPresent()) {
                    ThreadLocalStorage.setCurrentOrganizationID(organization.get().getId());
                } else {
                    servletResponse.sendRedirect(OrganizationSelectController.URI);
                    LOGGER.debug("Wrong or missing org cookie, redirect to login");
                    return true;
                }
            } catch (AccessDeniedException ex) {
                servletResponse.sendRedirect(OrganizationSelectController.URI);
                LOGGER.debug("Wrong or missing org cookie, redirect to login");
                return true;
            }

        } else {
            servletResponse.sendRedirect(OrganizationSelectController.URI);
            LOGGER.debug("Wrong or missing org cookie, redirect to login");
            return true;
        }
        return false;
    }

    private boolean isWhiteListed(HttpServletRequest servletRequest) {

        final Long nbRulesMatched = whitelist.stream()
                .filter(s -> servletRequest.getRequestURI().matches(s)).collect(Collectors.counting());

        return nbRulesMatched != null && nbRulesMatched > 0;
    }

    private Optional<Cookie> extractOrgCookie(HttpServletRequest servletRequest) {
        return Arrays.asList(servletRequest.getCookies())
                .stream()
                .filter(cookie -> cookie.getName().equals(OrganizationSelectController.COOKIE_NAME))
                .findFirst();
    }
}
