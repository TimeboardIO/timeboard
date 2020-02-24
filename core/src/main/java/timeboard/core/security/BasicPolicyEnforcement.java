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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.expression.EvaluationException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class BasicPolicyEnforcement implements PolicyEnforcement {

    private static final Logger LOGGER = LoggerFactory.getLogger(BasicPolicyEnforcement.class);

    @Autowired
    private PolicyDefinition policyDefinition;

    @Override
    public boolean check(final Object subject, final Object resource, final Object action, final Object environment) {
        //Get all policy rules
        final List<PolicyRuleSet> allRules = policyDefinition.getAllPolicyRules()
                .stream().filter(prs -> prs.getActions().contains(new String(action + "").replaceAll("\'", "")))
                .collect(Collectors.toList());

        //Wrap the context
        final SecurityAccessContext cxt = new SecurityAccessContext(subject, resource, action, environment);

        final boolean res = checkRules(allRules, cxt);

        if (!res) {
            LOGGER.debug("Account :" + subject + " has no policy for action : " + action + " on resource : " + resource);
        }

        return res;
    }


    private boolean checkRules(final List<PolicyRuleSet> matchedRules, final SecurityAccessContext cxt) {
        if (matchedRules.isEmpty()) {
            return false;
        }
        for (final PolicyRuleSet rule : matchedRules) {
            try {
                if (!rule.getConditions().stream()
                        .allMatch(expression -> expression.getValue(cxt, Boolean.class))) {
                    return false;
                }
            } catch (final EvaluationException ex) {
                LOGGER.error("An error occurred while evaluating PolicyRule.", ex);
                return false;
            }
        }
        return true;
    }
}
