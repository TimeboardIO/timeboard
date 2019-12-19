package timeboard.reports;

/*-
 * #%L
 * reports
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

import org.junit.Assert;
import org.junit.Test;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.SpelParseException;
import org.springframework.expression.spel.standard.SpelExpressionParser;

public class TestExpressionParser {

    @Test
    public void testParseTrue(){
        final String filter = "tagKey == 'CUSTOMER' && tagValue == 'TIMEBOARD'";
        final ReportsService.TagWrapper tag = new ReportsService.TagWrapper("CUSTOMER","TIMEBOARD");
        final ExpressionParser parser = new SpelExpressionParser();
        final Expression exp = parser.parseExpression(filter);

        Assert.assertTrue(exp.getValue(tag, Boolean.class));
    }

    @Test
    public void testParseFalse(){

        final ReportsService.TagWrapper tag = new ReportsService.TagWrapper("CUSTOMER","TIMEBOARD");
        final ExpressionParser parser = new SpelExpressionParser();
        final Expression exp = parser.parseExpression("tagKey == 'CUSTOMER' && tagValue != 'TIMEBOARD'");

        Assert.assertFalse(exp.getValue(tag, Boolean.class));
    }

    @Test(expected = SpelParseException.class)
    public void testParseFail(){

        final ReportsService.TagWrapper tag = new ReportsService.TagWrapper("CUSTOMER","TIMEBOARD");
        final ExpressionParser parser = new SpelExpressionParser();
        final Expression exp = parser.parseExpression("tagKey == 'CUSTOMER' &@zfez& tagValue != 'TIMEBOARD'");

        Assert.assertFalse(exp.getValue(tag, Boolean.class));
    }

}
