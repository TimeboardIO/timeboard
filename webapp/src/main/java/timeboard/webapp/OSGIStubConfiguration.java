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

import org.osgi.service.log.LogService;
import org.osgi.service.log.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import timeboard.core.CoreConfiguration;
import timeboard.core.ui.CoreUIConfiguration;
import timeboard.home.HomeConfiguration;

@Configuration
public class OSGIStubConfiguration {

    @Bean
    public LogService createLogService(){
        return new LogService() {
            @Override
            public void log(int i, String s) {

            }

            @Override
            public void log(int i, String s, Throwable throwable) {

            }

            @Override
            public void log(org.osgi.framework.ServiceReference<?> serviceReference, int i, String s) {

            }

            @Override
            public void log(org.osgi.framework.ServiceReference<?> serviceReference, int i, String s, Throwable throwable) {

            }

            @Override
            public Logger getLogger(String s) {
                return null;
            }

            @Override
            public Logger getLogger(Class<?> aClass) {
                return null;
            }

            @Override
            public <L extends Logger> L getLogger(String s, Class<L> aClass) {
                return null;
            }

            @Override
            public <L extends Logger> L getLogger(Class<?> aClass, Class<L> aClass1) {
                return null;
            }

            @Override
            public <L extends Logger> L getLogger(org.osgi.framework.Bundle bundle, String s, Class<L> aClass) {
                return null;
            }
        };
    }
}
