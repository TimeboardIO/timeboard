package timeboard.reports;

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
        final ReportsRestAPI.TagWrapper tag = new ReportsRestAPI.TagWrapper("CUSTOMER","TIMEBOARD");
        final ExpressionParser parser = new SpelExpressionParser();
        final Expression exp = parser.parseExpression(filter);

        Assert.assertTrue(exp.getValue(tag, Boolean.class));
    }

    @Test
    public void testParseFalse(){

        final ReportsRestAPI.TagWrapper tag = new ReportsRestAPI.TagWrapper("CUSTOMER","TIMEBOARD");
        final ExpressionParser parser = new SpelExpressionParser();
        final Expression exp = parser.parseExpression("tagKey == 'CUSTOMER' && tagValue != 'TIMEBOARD'");

        Assert.assertFalse(exp.getValue(tag, Boolean.class));
    }

    @Test(expected = SpelParseException.class)
    public void testParseFail(){

        final ReportsRestAPI.TagWrapper tag = new ReportsRestAPI.TagWrapper("CUSTOMER","TIMEBOARD");
        final ExpressionParser parser = new SpelExpressionParser();
        final Expression exp = parser.parseExpression("tagKey == 'CUSTOMER' &@zfez& tagValue != 'TIMEBOARD'");

        Assert.assertFalse(exp.getValue(tag, Boolean.class));
    }

}
