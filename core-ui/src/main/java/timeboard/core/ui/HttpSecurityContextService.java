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

import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import org.osgi.service.component.annotations.ServiceScope;
import org.osgi.service.log.LogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import timeboard.core.api.TimeboardSessionStore;
import timeboard.core.model.User;



@Component
public class HttpSecurityContextService {


    @Autowired
    private LogService logService;

    @Autowired
    private TimeboardSessionStore sessionStore;

    public  UUID  getCurrentSessionID(HttpServletRequest req) {
        UUID u = null;

        if (req.getCookies() != null) {
            Optional<Cookie> sessionCookie = Arrays.asList((req).getCookies()).stream().filter(c -> c.getName().equals("timeboard")).findFirst();

            if (sessionCookie.isPresent()) {
                u = UUID.fromString(sessionCookie.get().getValue());
            }
        }
        return u;
    }

    public  User getCurrentUser(HttpServletRequest req) {
        User u = null;

        if (req.getCookies() != null) {
            Optional<Cookie> sessionCookie = Arrays.asList((req).getCookies()).stream().filter(c -> c.getName().equals("timeboard")).findFirst();

            if (sessionCookie.isPresent()) {
                UUID uuid = UUID.fromString(sessionCookie.get().getValue());
                Optional<TimeboardSessionStore.TimeboardSession> userSession = this.sessionStore.getSession(uuid);

                if (userSession.isPresent()) {
                    u = (User) userSession.get().getPayload().get("user");
                } else {
                    this.logService.log(LogService.LOG_INFO, String.format("No session for cookie %s in keystore %s", uuid.toString(), this.sessionStore));
                }
            }
        }
        return u;
    }


}
