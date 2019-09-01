package kronops.ui;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;

import javax.servlet.Servlet;


@Component(
        service = Servlet.class,
        scope = ServiceScope.PROTOTYPE,
        property = {
                "osgi.http.whiteboard.servlet.pattern=/",
                "osgi.http.whiteboard.context.select=(osgi.http.whiteboard.context.name=kronops)"
        }
)
public class HomeServlet extends TemplateServlet {


}
