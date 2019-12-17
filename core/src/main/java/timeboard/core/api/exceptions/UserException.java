package timeboard.core.api.exceptions;

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

import java.util.HashSet;
import java.util.Set;

public class UserException extends BusinessException {

    private final Set<Rule> triggeredRules = new HashSet<>();

    public UserException(Exception e) {
        super(e);
    }

    public UserException(String err) {
        super(err);
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
