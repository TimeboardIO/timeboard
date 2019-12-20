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

import java.util.Optional;

@Component
public class WebRessourcesInterceptor implements WebRequestInterceptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebRessourcesInterceptor.class);

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
            Long orgaID = ThreadLocalStorage.getCurrentOrganizationID();
            if(orgaID != null) {
                fillModelWithOrganization(modelMap, orgaID);
            }else{
                LOGGER.warn("User : {} try to access to null organisation", userInfo.getCurrentAccount());
            }

        }

        if(modelMap != null){
            modelMap.put("appName", appName);
        }
    }

    private void fillModelWithOrganization(ModelMap modelMap, Long orgaID) {
        modelMap.put("orgID", orgaID);
        Optional<Account> organisation = organizationService.getOrganizationByID(userInfo.getCurrentAccount(), orgaID);
        if(organisation.isPresent()){
            modelMap.put("currentOrg", organisation.get());
        }else{
            LOGGER.warn("User : {} try to access missing org : {}", userInfo.getCurrentAccount(), orgaID);
        }
    }

    @Override
    public void afterCompletion(WebRequest webRequest, Exception e) throws Exception {

    }
}
