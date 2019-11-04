package timeboard.security;

/*-
 * #%L
 * webui
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

import org.osgi.service.component.annotations.*;
import timeboard.core.api.UserService;
import timeboard.core.model.User;
import timeboard.core.ui.TimeboardServlet;
import timeboard.core.ui.ViewModel;
import timeboard.security.api.LoginService;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;


@Component(
        service = Servlet.class,
        scope = ServiceScope.PROTOTYPE,
        property = {
                "osgi.http.whiteboard.servlet.pattern=/newPassword",
                "osgi.http.whiteboard.context.select=(osgi.http.whiteboard.context.name=timeboard)"
        }
)
public class NewPasswordServlet extends TimeboardServlet {


    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policyOption = ReferencePolicyOption.GREEDY)
    List<LoginService> loginServices;

    @Reference
    UserService userService;


    @Override
    protected ClassLoader getTemplateResolutionClassLoader() {
        return NewPasswordServlet.class.getClassLoader();
    }


    @Override
    protected void handlePost(HttpServletRequest request, HttpServletResponse response, ViewModel viewModel) throws ServletException, IOException {
        viewModel.setTemplate("security:newPassword.html");
        String username = request.getParameter("username");
        String origin = request.getParameter("origin");

        User user = this.userService.findUserByLogin(username);

        if (user != null) {

            /* send email */


            String from = "paasport@tsc-nantes.com"; // mail PAASPORT
            String to = "clarene.szymalka@mythalesgroup.io";
            String subject = "subject";
            String message = "message";
            String login = "";
            String password = "";

            try {
                Properties props = new Properties();
                props.setProperty("mail.host", "10.10.0.48"); // host PAASPORT
                props.setProperty("mail.smtp.port", "25"); // port PAASPORT
                // props.setProperty("mail.smtp.auth", "true");
                // props.setProperty("mail.smtp.starttls.enable", "true");

                // Authenticator auth = new SMTPAuthenticator(login, password);

                // Session session = Session.getInstance(props, auth);
                Session session = Session.getInstance(props, null);

                MimeMessage msg = new MimeMessage(session);
                msg.setText(message);
                msg.setSubject(subject);
                msg.setFrom(new InternetAddress(from));
                msg.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
                Transport.send(msg);

            } catch (Exception ex) {
                System.out.println(ex);
            }


            /* send email */





            response.sendRedirect(origin);
        } else {
            response.setStatus(403);
            viewModel.getViewDatas().put("message", "Username incorrect.");
            viewModel.setTemplate("security:newPassword.html");
        }

    }


    private class SMTPAuthenticator extends Authenticator {

        private PasswordAuthentication authentication;

        public SMTPAuthenticator(String login, String password) {
            authentication = new PasswordAuthentication(login, password);
        }

        @Override
        protected PasswordAuthentication getPasswordAuthentication() {
            return authentication;
        }
    }



    @Override
    protected void handleGet(HttpServletRequest request, HttpServletResponse response, ViewModel viewModel) throws ServletException, IOException {
        viewModel.setTemplate("security:newPassword.html");
        Map<String, Object> templateDatas = new HashMap<>();
        String origin = "/";
        if (request.getParameter("origin") != null) {
            origin = request.getParameter("origin");
        }
        templateDatas.put("origin", origin);
        viewModel.getViewDatas().putAll(templateDatas);
    }


}
