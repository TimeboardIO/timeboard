package timeboard.core.api.exceptions;

import timeboard.core.internal.rules.Rule;

import java.util.HashSet;
import java.util.Set;

public class CommercialException  extends Exception {

    private final Set<Rule> triggeredRules = new HashSet<>();

    public CommercialException(Exception e) {
        super(e);
    }

    public CommercialException(String err) {
        super(err);
    }

    public CommercialException(Set<Rule> wrongRules) {
        this.triggeredRules.addAll(wrongRules);
    }

    @Override
    public String getMessage() {
        if (this.triggeredRules.isEmpty()) {
            return super.getMessage();
        } else {
            StringBuilder builder = new StringBuilder();
            this.triggeredRules.forEach(rule -> {
                builder.append(rule.ruleDescription());
                builder.append("\n");
            });
            return builder.toString();
        }
    }
}
