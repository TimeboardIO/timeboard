package timeboard.core.ui;

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
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public abstract class TimeboardServlet extends HttpServlet {

    private OSGITemplateResolver resolver = new OSGITemplateResolver();
    private NavigationEntryRegistryService navRegistry;

    protected abstract ClassLoader getTemplateResolutionClassLoader();

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);

        ServiceReference<NavigationEntryRegistryService> sr = FrameworkUtil.getBundle(TimeboardServlet.class).getBundleContext().getServiceReference(NavigationEntryRegistryService.class);
        this.navRegistry = FrameworkUtil.getBundle(TimeboardServlet.class).getBundleContext().getService(sr);
        resolver.setTemplateMode(TemplateMode.HTML);
        resolver.setPrefix("/templates/");
        resolver.setCacheable(true);
        resolver.setCacheTTLMs(50000L);
        resolver.setCharacterEncoding("utf-8");
    }


    private String getAppName() {
        String appName = "Missing Theme Plugin";
        ServiceReference<BrandingService> brandingServiceRef = FrameworkUtil.getBundle(TimeboardServlet.class).getBundleContext().getServiceReference(BrandingService.class);
        if (brandingServiceRef != null) {
            BrandingService brandingService = FrameworkUtil.getBundle(TimeboardServlet.class).getBundleContext().getService(brandingServiceRef);
            appName = brandingService.appName();
        }
        return appName;
    }

    private List<String> getCssLinkURLs() {
        List<String> cssURLs = new ArrayList<>();
        ServiceReference<CssService> cssServiceRef = FrameworkUtil.getBundle(TimeboardServlet.class).getBundleContext().getServiceReference(CssService.class);
        if (cssServiceRef != null) {
            CssService cssService = FrameworkUtil.getBundle(TimeboardServlet.class).getBundleContext().getService(cssServiceRef);
            cssURLs.addAll(cssService.listCSSUrls());
        }
        return cssURLs;
    }

    private List<String> getJavascriptURLs() {
        List<String> jsURLs = new ArrayList<>();
        ServiceReference<JavascriptService> jsServiceRef = FrameworkUtil.getBundle(TimeboardServlet.class).getBundleContext().getServiceReference(JavascriptService.class);
        if (jsServiceRef != null) {
            JavascriptService jsService = FrameworkUtil.getBundle(TimeboardServlet.class).getBundleContext().getService(jsServiceRef);
            jsURLs.addAll(jsService.listJavascriptUrls());
        }
        return jsURLs;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        final ViewModel viewModel = new ViewModel();
        try {
            this.handleGet(request, response, viewModel);
        } catch (Exception e) {
            viewModel.getErrors().add(e);
            System.out.println(e.getMessage());
            e.printStackTrace(System.err);
        }
        doService(request, response, viewModel);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        final ViewModel viewModel = new ViewModel();
        try {
            this.handlePost(request, response, viewModel);
        } catch (Exception e) {
            viewModel.getErrors().add(e);
            System.out.println(e.getMessage());
            e.printStackTrace(System.err);
        }
        doService(request, response, viewModel);
    }

    protected void handlePost(HttpServletRequest request, HttpServletResponse response, final ViewModel viewModel) throws Exception {
    }

    protected void handleGet(HttpServletRequest request, HttpServletResponse response, final ViewModel viewModel) throws Exception {
    }


    protected List<NavigationExtPoint> getNavs() {
        return this.navRegistry.getEntries();
    }

    protected void forward(HttpServletRequest request, HttpServletResponse response, String url, ViewModel viewModel) throws ServletException, IOException {
        RequestDispatcher requestDispatcher = request.getRequestDispatcher(url);
        request.setAttribute("errors", viewModel.getErrors());
        requestDispatcher.forward(request, response);
    }


    protected void doService(HttpServletRequest request, HttpServletResponse response, final ViewModel viewModel) throws ServletException, IOException {
        response.setCharacterEncoding(resolver.getCharacterEncoding());
        viewModel.getViewDatas().put("user", request.getSession().getAttribute("user"));

        TemplateEngine engine = new TemplateEngine();
        engine.setTemplateResolver(resolver);
        engine.addDialect(new LayoutDialect());

        final WebContext ctx = new WebContext(request, response, request.getServletContext());
        viewModel.getViewDatas().forEach((s, o) -> {
            ctx.setVariable(s, o);
        });

        //Used to forward errors between servlets
        if (request.getAttribute("errors") != null) {
            viewModel.getErrors().addAll((Collection<? extends Exception>) request.getAttribute("errors"));
        }
        ctx.setVariable("errors", viewModel.getErrors());


        ArrayList<NavigationExtPoint> navigationEntries = new ArrayList<>();
        if (this.getNavs() != null) {
            navigationEntries.addAll(this.getNavs());
        }
        ctx.setVariable("navs", navigationEntries);

        ctx.setVariable("appName", this.getAppName());

        ctx.setVariable("javascripts", this.getJavascriptURLs());

        ctx.setVariable("CSSs", this.getCssLinkURLs());

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

    protected Optional<String> getParameter(final HttpServletRequest req, final String paramName){
        return Optional.of(req.getParameter(paramName));
    }


}
