package timeboard.webapp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.ui.ModelMap;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.context.request.WebRequestInterceptor;
import timeboard.core.api.DataTableService;
import timeboard.core.api.OrganizationService;
import timeboard.core.api.ThreadLocalStorage;
import timeboard.core.model.Account;
import timeboard.core.ui.CssService;
import timeboard.core.ui.JavascriptService;
import timeboard.core.ui.NavigationEntryRegistryService;
import timeboard.core.ui.UserInfo;

import java.util.List;

@Component
public class WebRessourcesInterceptor implements WebRequestInterceptor {

    @Value("${timeboard.appName}")
    private String appName;

    @Autowired
    private JavascriptService javascriptService;

    @Autowired
    private CssService cssService;

    @Autowired
    private UserInfo userInfo;

    @Autowired
    private NavigationEntryRegistryService navRegistry;

    @Autowired
    private DataTableService dataTableService;

    @Autowired
    private OrganizationService organizationService;

    @Override
    public void preHandle(WebRequest webRequest) throws Exception {

    }

    @Override
    public void postHandle(WebRequest webRequest, ModelMap modelMap) throws Exception {
        if(modelMap != null && webRequest.getUserPrincipal() != null) {
            modelMap.put("account", userInfo.getCurrentAccount());
            modelMap.put("navs", navRegistry.getEntries());
            modelMap.put("javascripts", javascriptService.listJavascriptUrls());
            modelMap.put("CSSs", cssService.listCSSUrls());
            modelMap.put("dataTableService", dataTableService);
            modelMap.put("orgID", ThreadLocalStorage.getCurrentOrganizationID());
            modelMap.put("currentOrg", organizationService.getOrganizationByID(userInfo.getCurrentAccount(), ThreadLocalStorage.getCurrentOrganizationID()));

            final List<Account> organisations = organizationService.getParents(userInfo.getCurrentAccount(), userInfo.getCurrentAccount());
            organisations.add(userInfo.getCurrentAccount());
            modelMap.put("orgList", organisations);
        }

        if(modelMap != null){
            modelMap.put("appName", appName);
        }
    }

    @Override
    public void afterCompletion(WebRequest webRequest, Exception e) throws Exception {

    }
}
