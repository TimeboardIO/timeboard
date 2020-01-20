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
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import timeboard.account.AccountConfiguration;
import timeboard.core.CoreConfiguration;
import timeboard.home.HomeConfiguration;
import timeboard.organization.OrganizationsConfiguration;
import timeboard.organization.TimesheetConfiguration;
import timeboard.plugin.project.imp.jira.JiraPluginConfiguration;
import timeboard.plugin.project.sync.github.GithubPluginConfiguration;
import timeboard.projects.ProjectsConfiguration;
import timeboard.reports.ReportsConfiguration;
import timeboard.theme.ThemeConfiguration;
import timeboard.vacations.VacationsConfiguration;

@Configuration
@Import({
        ThemeConfiguration.class,
        CoreConfiguration.class,
        HomeConfiguration.class,
        AccountConfiguration.class,
        ReportsConfiguration.class,
        TimesheetConfiguration.class,
        ProjectsConfiguration.class,
        JiraPluginConfiguration.class,
        GithubPluginConfiguration.class,
        OrganizationsConfiguration.class,
        VacationsConfiguration.class
})
public class ModulesConfiguration {

    @Autowired
    private OrganizationFilter organizationFilter;

    @Autowired
    private RequestDurationFilter requestDurationFilter;

    @Bean
    public FilterRegistrationBean<OrganizationFilter> loggingFilter(){
        FilterRegistrationBean<OrganizationFilter> registrationBean = new FilterRegistrationBean<>();

        registrationBean.setFilter(this.organizationFilter);
        registrationBean.addUrlPatterns("/*");

        return registrationBean;
    }

    @Bean
    public FilterRegistrationBean<RequestDurationFilter> loggingRequestDurationFilter(){
        FilterRegistrationBean<RequestDurationFilter> registrationBean = new FilterRegistrationBean<>();

        registrationBean.setFilter(this.requestDurationFilter);
        registrationBean.addUrlPatterns("/*");

        return registrationBean;
    }
}
