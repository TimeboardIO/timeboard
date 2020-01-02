package timeboard.webapp.security;

/*-
 * #%L
 * webapp
 * %%
 * Copyright (C) 2019 - 2020 Timeboard
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
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.web.filter.GenericFilterBean;
import timeboard.core.TimeboardAuthentication;
import timeboard.core.api.UserService;
import timeboard.core.model.Account;
import timeboard.home.HomeController;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;

@Configuration
@EnableWebSecurity
public class TimeboardSecurityConfig extends WebSecurityConfigurerAdapter {

    @Value("${cognito.logout}")
    private String logoutEndpoint;


    @Value("${app.url}")
    private String appLogout;


    @Value("${oauth.clientid}")
    private String clientid;

    @Autowired
    private UserService userService;

    @Autowired
    private DatabaseAuthenticationProvider databaseAuthenticationProvider;


    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers("/",
                "/onboarding/**",
                "/public/**");

    }


    @Override
    protected void configure(HttpSecurity http) throws Exception {
        final String logoutURL = String.format("%s?client_id=%s&logout_uri=%s",
                this.logoutEndpoint,
                this.clientid,
                this.appLogout);

        http.authorizeRequests()

                .anyRequest()
                .authenticated()

                .and()
                .formLogin()
                .defaultSuccessUrl(HomeController.URI).permitAll()

                .and()
                    .oauth2Login()

                .and()
                .logout()
                .logoutUrl("/logout")
                .logoutSuccessUrl(logoutURL);

        http.addFilterAfter(new CustomFilter(), BasicAuthenticationFilter.class);

     }


    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        final PasswordEncoder encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();

        auth
            .userDetailsService(this.userService)
            .and()
            .authenticationProvider(this.databaseAuthenticationProvider);
    }


    public class CustomFilter extends GenericFilterBean {

        @Override
        public void doFilter(
                ServletRequest request,
                ServletResponse response,
                FilterChain chain) throws IOException, ServletException {

            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            Account account = null;

            if (auth instanceof UsernamePasswordAuthenticationToken) {
                account = userService.findUserBySubject(((UsernamePasswordAuthenticationToken) auth).getName());
            }

            if (auth instanceof OAuth2AuthenticationToken) {
                account = userService.findUserBySubject(
                        (String) ((OAuth2AuthenticationToken) auth).getPrincipal().getAttributes().get("sub"));
            }

            if (account != null) {
                SecurityContextHolder.getContext().setAuthentication(new TimeboardAuthentication(account));
            }else{
                SecurityContextHolder.clearContext();
            }

            chain.doFilter(request, response);
        }
    }

}
