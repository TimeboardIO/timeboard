package kronops.ui;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;


@Component(
        service = Servlet.class,
        scope = ServiceScope.PROTOTYPE,
        property = {
                "osgi.http.whiteboard.servlet.pattern=/*",
                "osgi.http.whiteboard.context.select=(osgi.http.whiteboard.context.name=kronops)"
        }
)
public class HomeServlet extends HttpServlet {


    private ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver(HomeServlet.class.getClassLoader());

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);


        resolver.setTemplateMode(TemplateMode.HTML);
        resolver.setPrefix("/templates/");
        resolver.setCacheable(true);
        resolver.setCacheTTLMs(60000L);
        resolver.setCharacterEncoding("utf-8");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doService(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doService(request, response);
    }

    protected void doService(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setCharacterEncoding(resolver.getCharacterEncoding());

        TemplateEngine engine = new TemplateEngine();
        engine.setTemplateResolver(resolver);

        WebContext ctx = new WebContext(request, response, request.getServletContext());
        String templateName = getTemplateName(request);
        String result = engine.process(templateName, ctx);

        PrintWriter out = null;
        try {
            out = response.getWriter();
            out.println(result);
        } finally {
            out.close();
        }
    }

    protected String getTemplateName(HttpServletRequest request) {
        String requestPath = request.getRequestURI();
        String contextPath = request.getContextPath();
        if (contextPath == null) {
            contextPath = "";
        }

        return requestPath.substring(contextPath.length());
    }
}
