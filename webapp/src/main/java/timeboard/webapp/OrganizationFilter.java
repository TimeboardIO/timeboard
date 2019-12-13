package timeboard.webapp;

import com.atlassian.jira.rest.client.api.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.ui.ModelMap;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.context.request.WebRequestInterceptor;
import org.springframework.web.servlet.handler.DispatcherServletWebRequest;
import timeboard.core.api.DataTableService;
import timeboard.core.api.ThreadLocalStorage;
import timeboard.core.ui.CssService;
import timeboard.core.ui.JavascriptService;
import timeboard.core.ui.NavigationEntryRegistryService;
import timeboard.core.ui.UserInfo;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@Component
@Order(1)
public class OrganizationFilter implements Filter {

    @Autowired
    private UserInfo userInfo;



    private String getURL(HttpServletRequest webRequest) {
        return webRequest.getRequestURI();
    }

    private Long extractOrgID(String url) {
        try {
            return Long.parseLong(url.split("/")[2]);
        }catch (Exception e){
            return null;
        }
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        final String url = getURL((HttpServletRequest) servletRequest);
        final Long orgID = extractOrgID(url);
        if(orgID != null) {
            ThreadLocalStorage.setCurrentOrganizationID(orgID);
        }else{
            if(this.userInfo.getCurrentAccount() != null) {
                ThreadLocalStorage.setCurrentOrganizationID(this.userInfo.getCurrentAccount().getId());
            }
        }

        filterChain.doFilter(servletRequest, servletResponse);
    }
}
