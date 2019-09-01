package kronops.ui;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;
import org.osgi.service.http.context.ServletContextHelper;

@Component(
        service = ServletContextHelper.class,
        scope = ServiceScope.BUNDLE,
        property = {
                "osgi.http.whiteboard.context.name=kronops",
                "osgi.http.whiteboard.context.path=/kronops"})
public class KronopsServletContext extends ServletContextHelper {


}
