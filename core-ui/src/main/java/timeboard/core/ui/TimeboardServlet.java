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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import timeboard.core.api.UserService;
import timeboard.core.model.User;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;


public abstract class TimeboardServlet extends HttpServlet {

    private ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();

    @Autowired
    private NavigationEntryRegistryService navRegistry;

    @Autowired
    private BrandingService brandingService;

    @Autowired
    private JavascriptService javascriptService;

    @Autowired
    private UserService userService;

    @Autowired
    private CssService cssService;

    protected abstract ClassLoader getTemplateResolutionClassLoader();


    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);

        resolver.setTemplateMode(TemplateMode.HTML);
        resolver.setPrefix("/templates/");
        resolver.setCacheable(true);
        resolver.setCacheTTLMs(50000L);
        resolver.setCharacterEncoding("utf-8");
    }


    private String getAppName() {
        return this.brandingService.appName();
    }

    private List<String> getCssLinkURLs() {
        return this.cssService.listCSSUrls();
    }

    private List<String> getJavascriptURLs() {
       return this.javascriptService.listJavascriptUrls();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        final ViewModel viewModel = new ViewModel();
        try {
            this.handleGet(getActorFromRequestAttributes(request), request, response, viewModel);
        } catch (Exception e) {
            if (request.getSession().getAttribute("errors") != null) {
                ((List) request.getSession().getAttribute("errors")).add(e);
            } else {
                request.getSession().setAttribute("errors", new LinkedList<>(Arrays.asList(e)));
            }
            e.printStackTrace(System.err);
        }
        if (viewModel.getTemplate() != null) {
            doService(request, response, viewModel);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        final ViewModel viewModel = new ViewModel();
        try {
            this.handlePost(getActorFromRequestAttributes(request), request, response, viewModel);
        } catch (Exception e) {
            viewModel.getErrors().add(e);
            System.out.println(e.getMessage());
            e.printStackTrace(System.err);
        }
        doService(request, response, viewModel);
    }

    protected User getActorFromRequestAttributes(HttpServletRequest request) {
        OAuth2AuthenticationToken authentication = (OAuth2AuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        return this.userService.findUserBySubject((String) authentication.getPrincipal().getAttributes().get("sub"));
    }

    protected void handlePost(User actor, HttpServletRequest request, HttpServletResponse response, final ViewModel viewModel) throws Exception {
    }

    protected void handleGet(User actor, HttpServletRequest request, HttpServletResponse response, final ViewModel viewModel) throws Exception {
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

        response.setContentType(MediaType.TEXT_HTML_VALUE);

        response.setCharacterEncoding(resolver.getCharacterEncoding());
        viewModel.getViewDatas().put("user", getActorFromRequestAttributes(request));

        TemplateEngine engine = new TemplateEngine();
        engine.setTemplateResolver(resolver);
        engine.addDialect(new LayoutDialect());

        final WebContext ctx = new WebContext(request, response, request.getServletContext());
        viewModel.getViewDatas().forEach(ctx::setVariable);

        //Used to forward errors between servlets
        if (request.getAttribute("errors") != null) {
            viewModel.getErrors().addAll((Collection<? extends Exception>) request.getAttribute("errors"));
        }
        //Used to forward errors out of servlets
        if (request.getSession().getAttribute("errors") != null) {
            viewModel.getErrors().addAll((Collection<? extends Exception>) request.getSession().getAttribute("errors"));
            ((Collection<? extends Exception>) request.getSession().getAttribute("errors")).clear();
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

    protected Optional<String> getParameter(final HttpServletRequest req, final String paramName) {
        return Optional.of(req.getParameter(paramName));
    }


}
