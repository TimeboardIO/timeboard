package timeboard.core.internal.observers.emails;

/*-
 * #%L
 * core
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

import nz.net.ultraq.thymeleaf.LayoutDialect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import java.util.Locale;
import java.util.Map;

@Component
public class TemplateGenerator {

    private ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();

    private boolean init = false;

    @Autowired
    private MessageSource messageSource;

    public void init() {

        resolver.setTemplateMode(TemplateMode.HTML);
        resolver.setPrefix("/templates/");
        resolver.setCacheable(true);
        resolver.setCacheTTLMs(50000L);
        resolver.setCharacterEncoding("utf-8");

        init = true;
    }


    public String getTemplateString(final String templateName, final Map<String, Object> templateData, Locale locale) {
        if (!init) {
            init();
        }

        final SpringTemplateEngine engine = new SpringTemplateEngine();
        engine.setTemplateResolver(resolver);
        engine.addDialect(new LayoutDialect());
        engine.setTemplateEngineMessageSource(messageSource);

        final Context ctx = new Context(locale);

        templateData.forEach(ctx::setVariable);
        return engine.process(templateName, ctx);

    }

}
