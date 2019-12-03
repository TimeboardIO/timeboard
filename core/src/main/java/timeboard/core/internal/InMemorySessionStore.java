package timeboard.core.internal;

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

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;
import org.osgi.service.log.LogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import timeboard.core.api.TimeboardSessionStore;
import timeboard.core.model.User;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
public class InMemorySessionStore implements TimeboardSessionStore {

    @Autowired
    private LogService logService;

    private static final Cache<UUID, TimeboardSession> SESSION_STORE = CacheBuilder.newBuilder()
            .maximumSize(5000)
            .expireAfterWrite(30, TimeUnit.MINUTES)
            .build();

    @PostConstruct
    private void init() {
        this.logService.log(LogService.LOG_INFO, String.format("Start session store : %s", this.toString()));
    }

    @Override
    public Optional<TimeboardSession> getSession(UUID sessionUUID) {
        TimeboardSession session = SESSION_STORE.getIfPresent(sessionUUID);
        return Optional.ofNullable(session);
    }

    @Override
    public TimeboardSession createSession(User user) {
        UUID sessionUUID = UUID.randomUUID();
        TimeboardSession session = new TimeboardSession(sessionUUID);
        session.getPayload().put("user", user);
        SESSION_STORE.put(sessionUUID, session);
        this.logService.log(LogService.LOG_INFO, String.format("Create session from user %s with sessionUUID %s in keystore %s", user.getScreenName(), sessionUUID.toString(), this));
        return session;
    }

    @Override
    public void invalidateSession(UUID uuid) {
        SESSION_STORE.invalidate(uuid);
        this.logService.log(LogService.LOG_INFO, String.format("Invalidate session %s", uuid));
    }

    @Override
    public List<TimeboardSession> listSessions() {
        return new ArrayList<>(SESSION_STORE.asMap().values());
    }
}
