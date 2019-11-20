package timeboard.security;

/*-
 * #%L
 * security
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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;
import timeboard.core.api.UserService;
import timeboard.core.api.exceptions.BusinessException;
import timeboard.core.model.User;

import javax.servlet.Servlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;
import java.util.Map;

@Component(
        service = Servlet.class,
        scope = ServiceScope.PROTOTYPE,
        property = {
                "oauth.token.url=https://timeboard.auth.eu-west-1.amazoncognito.com/oauth2/token",
                "oauth.userinfo.url=https://timeboard.auth.eu-west-1.amazoncognito.com/oauth2/userInfo",
                "oauth.clientid=changeme",
                "oauth.secretid=changeme",
                "osgi.http.whiteboard.servlet.pattern=/signin",
                "osgi.http.whiteboard.context.select=(osgi.http.whiteboard.context.name=timeboard)"
        },
        configurationPid = {"timeboard.oauth"}
)
public class SigninServlet extends HttpServlet {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private String tokenURL;
    private String userInfoURL;
    private String clientID;
    private String secretID;

    @Reference
    private UserService userService;

    @Activate
    private void init(Map<String, String> params){
        this.tokenURL = params.get("oauth.token.url");
        this.userInfoURL = params.get("oauth.userinfo.url");
        this.clientID = params.get("oauth.clientid");
        this.secretID = params.get("oauth.secretid");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        final HttpClient client = HttpClient.newHttpClient();


        try {
            String oauthCode = req.getParameter("code");

            StringBuilder params = new StringBuilder();
            params.append("grant_type").append("=").append("authorization_code");
            params.append("&");
            params.append("client_id").append("=").append(clientID);
            params.append("&");
            params.append("code").append("=").append(oauthCode);
            params.append("&");
            params.append("redirect_uri").append("=").append("http://localhost:8181/signin");


            final HttpRequest oauthTokenRequest = HttpRequest
                    .newBuilder()
                    .POST(HttpRequest.BodyPublishers.ofString(params.toString()))
                    .header("Authorization", "Basic " + Base64.getEncoder().encodeToString((clientID + ":" + secretID).getBytes()))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .uri(URI.create(this.tokenURL))
                    .build();

            final String tokens = client.send(oauthTokenRequest, HttpResponse.BodyHandlers.ofString()).body();

            final Map tokensMap = MAPPER.readValue(tokens, Map.class);

            final HttpRequest oauthUserInfoRequest = HttpRequest
                    .newBuilder()
                    .GET()
                    .header("Authorization", "Bearer " + tokensMap.get("access_token").toString())
                    .uri(URI.create(this.userInfoURL))
                    .build();

            final String userInfo = client.send(oauthUserInfoRequest, HttpResponse.BodyHandlers.ofString()).body();

            final Map<String, String> userInfoMap = MAPPER.readValue(userInfo, Map.class);

            User user = this.userService.userProvisionning(userInfoMap.get("email"));
            HttpSession session = req.getSession(true);
            session.setAttribute("user", user);
            resp.sendRedirect("/");

        } catch (InterruptedException | BusinessException e) {
            e.printStackTrace();
        }


        resp.sendRedirect("/");

    }
}
