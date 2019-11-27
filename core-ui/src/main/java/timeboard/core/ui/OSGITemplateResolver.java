package timeboard.core.ui;

/*-
 * #%L
 * core-ui
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

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.thymeleaf.IEngineConfiguration;
import org.thymeleaf.templateresolver.AbstractConfigurableTemplateResolver;
import org.thymeleaf.templateresource.ITemplateResource;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.Map;

public class OSGITemplateResolver extends AbstractConfigurableTemplateResolver {



    @Override
    protected ITemplateResource computeTemplateResource(IEngineConfiguration iEngineConfiguration, String s, String s1, String s2, String s3, Map<String, Object> map) {
        BundleContext ctx = FrameworkUtil.getBundle(OSGITemplateResolver.class).getBundleContext();
        String bundleName = "timeboard."+s1.split(":")[0];
        String templateName = this.getPrefix()+s1.split(":")[1];
        Bundle bundle = null;
        for(Bundle b : ctx.getBundles()){
            if(b.getSymbolicName().equals(bundleName)){
                bundle = b;
            }
        }

        final URL template = bundle.getEntry(templateName);


        return new ITemplateResource() {
            @Override
            public String getDescription() {
                return "";
            }

            @Override
            public String getBaseName() {
                return templateName;
            }

            @Override
            public boolean exists() {
                return true;
            }

            @Override
            public Reader reader() throws IOException {
                return new InputStreamReader(template.openStream());
            }

            @Override
            public ITemplateResource relative(String s) {
                return null;
            }
        };
    }
}
