package kronops.core.ui;

/*-
 * #%L
 * core
 * %%
 * Copyright (C) 2019 Kronops
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

import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.swing.text.View;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

public abstract class KronopsServlet extends HttpServlet {

    private final Set<NavigationExtPoint> navigationItems = new HashSet<>();
    private ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver(new JoinClassLoader(KronopsServlet.class.getClassLoader(), this.getTemplateResolutionClassLoader()));
    private NavigationEntryRegistryService navRegistry;

    protected abstract ClassLoader getTemplateResolutionClassLoader();

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);

        ServiceReference<NavigationEntryRegistryService> sr = FrameworkUtil.getBundle(KronopsServlet.class).getBundleContext().getServiceReference(NavigationEntryRegistryService.class);
        this.navRegistry = FrameworkUtil.getBundle(KronopsServlet.class).getBundleContext().getService(sr);
        resolver.setTemplateMode(TemplateMode.HTML);
        resolver.setPrefix("/templates/");
        resolver.setCacheable(true);
        resolver.setCacheTTLMs(50000L);
        resolver.setCharacterEncoding("utf-8");
    }


    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        final ViewModel viewModel = new ViewModel();
        this.handleGet(request, response, viewModel);
        doService(request, response, viewModel);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        final ViewModel viewModel = new ViewModel();
        this.handlePost(request, response, viewModel);
        doService(request, response, viewModel);
    }

    protected  void handlePost(HttpServletRequest request, HttpServletResponse response, final ViewModel viewModel) throws ServletException, IOException{};

    protected  void handleGet(HttpServletRequest request, HttpServletResponse response, final ViewModel viewModel) throws ServletException, IOException{};


    protected List<NavigationExtPoint> getNavs() {
        return this.navRegistry.getEntries();
    }


    protected void doService(HttpServletRequest request, HttpServletResponse response, final ViewModel viewModel) throws ServletException, IOException {
        response.setCharacterEncoding(resolver.getCharacterEncoding());
        viewModel.getViewDatas().put("user", request.getSession().getAttribute("user"));

        TemplateEngine engine = new TemplateEngine();
        engine.setTemplateResolver(resolver);

        final WebContext ctx = new WebContext(request, response, request.getServletContext());
        viewModel.getViewDatas().forEach((s, o) -> {
            ctx.setVariable(s, o);
        });

        ArrayList<NavigationExtPoint> navigationEntries = new ArrayList<>();
        if (this.getNavs() != null) {
            navigationEntries.addAll(this.getNavs());
        }
        ctx.setVariable("navs", navigationEntries);
        String templateName = viewModel.getTemplate();
        String result = engine.process(templateName, ctx);


        PrintWriter out = null;
        try {
            out = response.getWriter();
            out.println(result);
        } finally {
            out.close();
        }
    }



}
