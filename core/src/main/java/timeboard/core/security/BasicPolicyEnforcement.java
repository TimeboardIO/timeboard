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

import java.util.ArrayList;
import java.util.List;

@Component
public class BasicPolicyEnforcement implements PolicyEnforcement {

    private static final Logger LOGGER = LoggerFactory.getLogger(BasicPolicyEnforcement.class);

    @Autowired
    private PolicyDefinition policyDefinition;

    @Override
    public boolean check(Object subject, Object resource, Object action, Object environment) {
        //Get all policy rules
        List<PolicyRule> allRules = policyDefinition.getAllPolicyRules();
        //Wrap the context
        SecurityAccessContext cxt = new SecurityAccessContext(subject, resource, action, environment);
        //Filter the rules according to context.
        List<PolicyRule> matchedRules = filterRules(allRules, cxt);
        //finally, check if any of the rules are satisfied, otherwise return false.
        return checkRules(matchedRules, cxt);
    }

    private List<PolicyRule> filterRules(List<PolicyRule> allRules, SecurityAccessContext cxt) {
        List<PolicyRule> matchedRules = new ArrayList<>();
        for (PolicyRule rule : allRules) {
            try {
                if (rule.getTarget().getValue(cxt, Boolean.class)) {
                    matchedRules.add(rule);
                }
            } catch (EvaluationException ex) {
                LOGGER.error("An error occurred while evaluating PolicyRule.", ex);
            }
        }
        return matchedRules;
    }

    private boolean checkRules(List<PolicyRule> matchedRules, SecurityAccessContext cxt) {
        for (PolicyRule rule : matchedRules) {
            try {
                if (rule.getCondition().getValue(cxt, Boolean.class)) {
                    return true;
                }
            } catch (EvaluationException ex) {
                LOGGER.error("An error occurred while evaluating PolicyRule.", ex);
            }
        }
        return false;
    }
}
