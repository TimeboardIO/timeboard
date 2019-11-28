package timeboard.core.ui;

/*-
 * #%L
 * security
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

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import timeboard.core.model.User;
import timeboard.core.api.TimeboardSessionStore;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

public class HttpSecurityContext {


    public static User getCurrentUser(HttpServletRequest req) {
        User u = null;
        Bundle bnd = FrameworkUtil.getBundle(HttpSecurityContext.class);
        BundleContext ctx = bnd.getBundleContext();
        ServiceReference<TimeboardSessionStore> sr = ctx.getServiceReference(TimeboardSessionStore.class);
        TimeboardSessionStore sessionStore = ctx.getService(sr);

        Optional<Cookie> sessionCookie = Arrays.asList(req.getCookies()).stream().filter(c -> c.getName().equals("timeboard")).findFirst();

        if (sessionCookie.isPresent()) {
            Optional<TimeboardSessionStore.TimeboardSession> userSession = sessionStore.getSession(UUID.fromString(sessionCookie.get().getValue()));

            if (userSession.isPresent()) {
                u = (User) userSession.get().getPayload().get("user");
            }
        }
        return u;
    }
}
