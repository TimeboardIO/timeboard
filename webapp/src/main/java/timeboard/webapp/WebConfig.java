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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.ui.ModelMap;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.context.request.WebRequestInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.handler.DispatcherServletWebRequest;
import timeboard.core.api.DataTableService;
import timeboard.core.api.ThreadLocalStorage;
import timeboard.core.api.UserService;
import timeboard.core.model.Account;
import timeboard.core.ui.CssService;
import timeboard.core.ui.JavascriptService;
import timeboard.core.ui.NavigationEntryRegistryService;
import timeboard.core.ui.UserInfo;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private NavigationEntryRegistryService navRegistry;

    @Value("${timeboard.appName}")
    private String appName;

    @Autowired
    private JavascriptService javascriptService;

    @Autowired
    private UserService userService;

    @Autowired
    private CssService cssService;

    @Autowired
    private DataTableService dataTableService;

    @Autowired
    private UserInfo userInfo;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/");
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addWebRequestInterceptor(new WebRequestInterceptor() {

            @Override
            public void preHandle(WebRequest webRequest) throws Exception {
                final String url = getURL((DispatcherServletWebRequest) webRequest);
                final Long orgID = extractOrgID(url);
                if(orgID != null) {
                    ThreadLocalStorage.setCurrentOrganizationID(orgID);
                }
            }

            @Override
            public void postHandle(WebRequest webRequest, ModelMap modelMap) throws Exception {
                if(modelMap != null && webRequest.getUserPrincipal() != null)  {

                    String url = getURL((DispatcherServletWebRequest) webRequest);
                    if(url.contains("/org/")) {
                        modelMap.put("orgID", ThreadLocalStorage.getCurrentOrganizationID());
                    }
                    modelMap.put("account", userInfo.getCurrentAccount());
                    modelMap.put("navs", navRegistry.getEntries());
                    modelMap.put("javascripts", javascriptService.listJavascriptUrls());
                    modelMap.put("CSSs", cssService.listCSSUrls());
                    // Use instance of DataTablaService
                    modelMap.put("dataTableService", dataTableService);
                }

                if(modelMap != null){
                    modelMap.put("appName", appName);
                }

            }

            private String getURL(DispatcherServletWebRequest webRequest) {
                return webRequest.getRequest().getRequestURI();
            }

            private Long extractOrgID(String url) {
                try {
                    return Long.parseLong(url.split("/")[2]);
                }catch (Exception e){
                    return null;
                }
            }

            @Override
            public void afterCompletion(WebRequest webRequest, Exception e) throws Exception {

            }

        });
    }
}
