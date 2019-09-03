package kronops.ui;

import kronops.core.api.ProjectServiceBP;
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
                "osgi.http.whiteboard.servlet.pattern=/projects",
                "osgi.http.whiteboard.context.select=(osgi.http.whiteboard.context.name=kronops)"
        }
)
public class ProjectsServlet extends TemplateServlet {


    @Reference
    ProjectServiceBP projectServiceBP;


    @Override
    protected Map<String, Object> getTemplateData() {
        Map<String, Object> data = new HashMap<>();
        data.put("projects", this.projectServiceBP.getProjects());
        return data;
    }
}
