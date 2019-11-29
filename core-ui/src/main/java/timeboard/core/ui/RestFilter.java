package timeboard.core.ui;

/*-
 * #%L
 * reporting
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

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import timeboard.core.api.TimeboardSessionStore;
import timeboard.core.model.User;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Context;
import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

@Component(
        service = ContainerRequestFilter.class,
        property = {
                "osgi.jaxrs.extension=true"
        },
        immediate = true
)
public class RestFilter implements ContainerRequestFilter {

    @Context
    private HttpServletRequest req;

    @Reference
    private HttpSecurityContextService securityContextService;



    @Override
    public void filter(ContainerRequestContext containerRequestContext) throws IOException {

        User user = this.securityContextService.getCurrentUser(this.req);

        if(user == null){
            throw new SecurityException();
        }

        containerRequestContext.setProperty("actor", user);
        System.out.println("Filter executed :)");
    }
}
