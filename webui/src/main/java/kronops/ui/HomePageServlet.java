package kronops.ui;

import org.osgi.service.component.annotations.*;
import org.osgi.service.http.whiteboard.HttpWhiteboardConstants;
import org.osgi.service.log.LogService;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

@Component(service = Servlet.class,
        name = "HomePageServlet",
        configurationPid = "kronops.ui.HomePageServlet",
        configurationPolicy = ConfigurationPolicy.REQUIRE,
        property = {
                HttpWhiteboardConstants.HTTP_WHITEBOARD_SERVLET_NAME + "=MainUIServlet",
                HttpWhiteboardConstants.HTTP_WHITEBOARD_SERVLET_PATTERN + "=/ui"
        },
        scope = ServiceScope.PROTOTYPE)
public class HomePageServlet extends HttpServlet {

    private String username;

    @Reference
    private org.osgi.service.log.LogService logService;


    @Activate
    private void activate(Map<String, String> props){
        this.username = props.get("username");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {

        ExpressionParser parser = new SpelExpressionParser();
        Expression exp = parser.parseExpression("'Hello '");
        String message = (String) exp.getValue();
        this.logService.log(LogService.LOG_INFO,message + this.username);
        resp.getWriter().println(message + this.username);
    }
}
