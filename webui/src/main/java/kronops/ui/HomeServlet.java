package kronops.ui;

/*-
 * #%L
 * webui
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

import kronops.core.api.KronopsServlet;
import kronops.core.api.NavigationExtPoint;
import kronops.core.api.ProjectExtPoint;
import kronops.core.api.UserServiceBP;
import org.osgi.service.component.annotations.*;

import javax.servlet.Servlet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Component(
        service = Servlet.class,
        scope = ServiceScope.PROTOTYPE,
        property = {
                "osgi.http.whiteboard.servlet.pattern=/",
                "osgi.http.whiteboard.context.select=(osgi.http.whiteboard.context.name=kronops)"
        }
)
public class HomeServlet extends KronopsServlet {

    @Reference
    public UserServiceBP userServiceBP;

    @Reference(
            cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC,
            policyOption = ReferencePolicyOption.GREEDY)
    public volatile List<NavigationExtPoint> navs;

    @Reference(
            cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC,
            policyOption = ReferencePolicyOption.GREEDY)
    public volatile List<ProjectExtPoint> projectPlugin;

    @Override
    protected ClassLoader getTemplateResolutionClassLoader() {
        return HomeServlet.class.getClassLoader();
    }

    @Override
    protected String getTemplate(String path) {
        return "home.html";
    }

    @Override
    protected Map<String, Object> getTemplateData() {
        Map<String, Object> data = new HashMap<>();
        data.put("name", this.userServiceBP.getCurrentUser().getName());
        if (this.projectPlugin != null) {
            data.put("plugins", this.projectPlugin);
        }

        return data;
    }

    @Override
    protected List<NavigationExtPoint> getNavs() {
        return this.navs;
    }
}
