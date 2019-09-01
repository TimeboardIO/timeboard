package kronops.ui;

import org.osgi.service.component.annotations.Component;

import javax.servlet.Servlet;
import javax.servlet.http.HttpServlet;


@Component(
        service = Servlet.class,
        property = {
                "osgi.http.whiteboard.resource.pattern=/static/*",
                "osgi.http.whiteboard.resource.prefix=/static"}
)
public class StaticRessourcesServlet extends HttpServlet {


}
