package kronops.ui;

import kronops.core.api.UserServiceBP;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;

import javax.servlet.Servlet;
import java.util.HashMap;
import java.util.Map;


@Component(
        service = Servlet.class,
        scope = ServiceScope.PROTOTYPE,
        property = {
                "osgi.http.whiteboard.servlet.pattern=/",
                "osgi.http.whiteboard.context.select=(osgi.http.whiteboard.context.name=kronops)"
        }
)
public class HomeServlet extends TemplateServlet {

    @Reference
    public UserServiceBP userServiceBP;

    @Override
    protected Map<String, Object> getTemplateData() {
        Map<String, Object> data = new HashMap<>();
        data.put("name", this.userServiceBP.getCurrentUser().getName());
        return data;
    }
}
