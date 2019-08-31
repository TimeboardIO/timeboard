package kronops.ui;

import org.osgi.service.component.annotations.*;

import javax.servlet.Servlet;
import javax.servlet.http.HttpServlet;


@Component(
        service= Servlet.class,
        property = {
                "osgi.http.whiteboard.resource.pattern=/ui/*",
                "osgi.http.whiteboard.resource.prefix=/www"}
                )
public class ResourcesServlet extends HttpServlet {
}
