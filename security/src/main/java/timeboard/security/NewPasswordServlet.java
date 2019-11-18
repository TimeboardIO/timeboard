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
import timeboard.core.api.exceptions.UserException;
import timeboard.core.api.TimeboardSubjects;
import timeboard.core.model.User;
import timeboard.core.ui.TimeboardServlet;
import timeboard.core.ui.ViewModel;
import timeboard.security.api.LoginService;
import timeboard.core.api.EmailService;

import javax.mail.MessagingException;
import javax.servlet.Servlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.security.SecureRandom;


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
    EmailService emailService;

    @Reference
    UserService userService;

    private static SecureRandom random = new SecureRandom();

    private static final int DEFAULT_SIZE_PASSWORD = 8;
    private static final String ALPHA_CAPS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String ALPHA = "abcdefghijklmnopqrstuvwxyz";
    private static final String NUMERIC = "0123456789";
    private static final String SPECIAL_CHARS = "#$^+=!()*@~%&/";


    @Override
    protected ClassLoader getTemplateResolutionClassLoader() {
        return NewPasswordServlet.class.getClassLoader();
    }


    @Override
    protected void handlePost(HttpServletRequest request, HttpServletResponse response, ViewModel viewModel) throws MessagingException, UserException {
        String username = request.getParameter("username");

        User user = null;

        try {
            user = this.userService.findUserByLogin(username);
        }catch (Exception e){
            System.out.println(e);
        }

        if (user != null) {

            final String newPassword = generatePassword();

            this.userService.updateUserGeneratedPassword(user.getId(), newPassword);

            // Send email
            Map<User, String> userPasswordMap = new HashMap<>();
            userPasswordMap.put(user, newPassword);
            TimeboardSubjects.NEW_PASSWORD.onNext(userPasswordMap);

            viewModel.setTemplate("security:newPassword.html");
            viewModel.getViewDatas().put("isSuccess", true);

        } else {
            response.setStatus(403);
            viewModel.setTemplate("security:newPassword.html");
            viewModel.getViewDatas().put("message", "Username incorrect.");
            viewModel.getViewDatas().put("isSuccess", false);
        }

    }

    private static String generatePassword() {
        String allPossibilities = ALPHA_CAPS + ALPHA + NUMERIC + SPECIAL_CHARS;
        int passwordLength = DEFAULT_SIZE_PASSWORD;

        String result = "";
        for (int i = 0; i < passwordLength; i++) {
            int index = random.nextInt(allPossibilities.length());
            result += allPossibilities.charAt(index);
        }
        return result;
    }



    @Override
    protected void handleGet(HttpServletRequest request, HttpServletResponse response, ViewModel viewModel) {
        viewModel.setTemplate("security:newPassword.html");
        Map<String, Object> templateDatas = new HashMap<>();
        String origin = "/";
        if (request.getParameter("origin") != null) {
            origin = request.getParameter("origin");
        }
        templateDatas.put("origin", origin);
        viewModel.getViewDatas().put("isSuccess", false);
        viewModel.getViewDatas().putAll(templateDatas);
    }


}
