package timeboard.core.security;

/*-
 * #%L
 * core
 * %%
 * Copyright (C) 2019 - 2020 Timeboard
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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import timeboard.core.api.BusinessPolicyEvaluator;
import timeboard.core.api.ProjectService;
import timeboard.core.api.exceptions.CommercialException;
import timeboard.core.model.Account;
import timeboard.core.model.Project;

import java.util.Date;

@Component
public class PolicyEnvironment {

    @Autowired
    BusinessPolicyEvaluator businessPolicyEvaluator;

    @Autowired
    ApplicationContext ctx;

    public PolicyEnvironment() {
    }

    public Date getDate() {
        return new Date();
    }

    public boolean checkEnabledProjectLimit(final TimeboardAuthentication authentication) throws CommercialException {
        return businessPolicyEvaluator.checkProjectEnabledLimit(authentication.getDetails());
    }

    public boolean checkProjectByUserLimit(final TimeboardAuthentication authentication) throws CommercialException {
        return businessPolicyEvaluator.checkProjectByUserLimit(authentication.getDetails());
    }

    public boolean checkTaskByProjectLimit(final TimeboardAuthentication authentication, final Project project) throws CommercialException {
        return businessPolicyEvaluator.checkTaskByProjectLimit(authentication.getDetails(), project);
    }

    public boolean isOwnerOfAnyUserProject(final TimeboardAuthentication authentication, final Account user) {
        return ctx.getBean(ProjectService.class).isOwnerOfAnyUserProject(authentication.getDetails(), user);
    }


}
