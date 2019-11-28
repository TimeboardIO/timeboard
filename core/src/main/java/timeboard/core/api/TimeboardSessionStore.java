package timeboard.core.api;

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

import timeboard.core.model.User;

import java.io.Serializable;
import java.util.*;

public interface TimeboardSessionStore {

    /**
     *
     * @param sessionUUID sessionUUID
     * @return session if exist or null
     */
    Optional<TimeboardSession> getSession(UUID sessionUUID);

    TimeboardSession createSession(User user);

    void invalidateSession(UUID uuid);

    List<TimeboardSession> listSessions();

    class TimeboardSession implements Serializable {
        private final UUID sessionUUID;
        private final Date createDate;
        private final Map<String, Object> payload = new HashMap<>();

        public TimeboardSession(UUID sessionUUID) {
            this.sessionUUID = sessionUUID;
            this.createDate = new Date();
        }

        public Date getCreateDate() {
            return createDate;
        }

        public UUID getSessionUUID() {
            return sessionUUID;
        }

        public Map<String, Object> getPayload() {
            return payload;
        }

    }

}
