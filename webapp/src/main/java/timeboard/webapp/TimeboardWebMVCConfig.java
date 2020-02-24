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
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.format.FormatterRegistry;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import timeboard.core.converters.LongToAccountConverter;
import timeboard.projects.converters.LongToProjectConverter;
import timeboard.reports.converters.ReportConverter;
import timeboard.vacations.converters.LongToVacationRequestConverter;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

@Configuration
public class TimeboardWebMVCConfig implements WebMvcConfigurer {

    @Autowired
    private WebRessourcesInterceptor webRessourcesInterceptor;

    @Autowired
    private LongToProjectConverter longToProjectConverter;

    @Autowired
    private LongToVacationRequestConverter longToVacationRequestConverter;

    @Autowired
    private LongToAccountConverter longToAccountConverter;

    @Autowired
    private ReportConverter reportConverter;

    @Override
    public void addCorsMappings(final CorsRegistry registry) {
        registry.addMapping("/**").allowedOrigins("*");
    }

    @Bean
    public MessageSource messageSource() {
        final ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasename("i18n/messages");
        messageSource.setDefaultEncoding("UTF-8");
        messageSource.setUseCodeAsDefaultMessage(true);
        return messageSource;
    }

    @Bean
    public LocaleResolver localeResolver() {
        final CookieLocaleResolver clr = new CookieLocaleResolver();
        clr.setDefaultLocale(Locale.ENGLISH);
        return clr;
    }

    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor() {
        final LocaleChangeInterceptor lci = new LocaleChangeInterceptor();
        lci.setParamName("lang");
        return lci;
    }


    @Override
    public void addResourceHandlers(final ResourceHandlerRegistry registry) {
        registry
                .addResourceHandler("/webjars/**")
                .setCacheControl(CacheControl.maxAge(365, TimeUnit.DAYS))
                .addResourceLocations("classpath:/META-INF/resources/webjars/");
    }

    @Override
    public void addInterceptors(final InterceptorRegistry registry) {
        registry.addWebRequestInterceptor(this.webRessourcesInterceptor);
        registry.addInterceptor(localeChangeInterceptor());
    }

    @Override
    public void addFormatters(final FormatterRegistry registry) {
        registry.addConverter(this.longToProjectConverter);
        registry.addConverter(this.longToVacationRequestConverter);
        registry.addConverter(this.reportConverter);
        registry.addConverter(this.longToAccountConverter);
    }
}
