package timeboard.core.internal.rules.task;

/*-
 * #%L
 * core
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

import timeboard.core.internal.rules.Rule;
import timeboard.core.model.Account;
import timeboard.core.model.ProjectMembership;
import timeboard.core.model.Task;

import java.util.Optional;


public class ActorIsProjectMemberbyTask implements Rule<Task> {

    @Override
    public String ruleDescription() {
        return "User is not project member";
    }

    @Override
    public boolean isSatisfied(final Account u, final Task thing) {
        final Optional<ProjectMembership> userOptional = thing.getProject().getMembers().stream()
                .filter(projectMembership ->
                        projectMembership.getMember().getId() == u.getId()
                )
                .findFirst();
        return userOptional.isPresent();
    }
}
