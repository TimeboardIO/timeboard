package timeboard.core.ui;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import timeboard.core.api.UserService;
import timeboard.core.api.exceptions.BusinessException;
import timeboard.core.model.User;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;
import java.util.Map;


@WebServlet(name = "SigninServlet", urlPatterns = "/signin")
public class SigninServlet extends HttpServlet {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    @Value("${timeboard.oauth.token.url}")
    private String tokenURL;

    @Value("${timeboard.oauth.userinfo.url}")
    private String userInfoURL;

    @Value("${timeboard.oauth.clientId}")
    private String clientID;

    @Value("${timeboard.oauth.secretId}")
    private String secretID;

    @Value("${timeboard.oauth.redirect.uri}")
    private String redirectURI;

    @Autowired
    private UserService userService;


    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        final HttpClient client = HttpClient.newHttpClient();

        try {
            StringBuilder params = new StringBuilder();
            params.append("grant_type").append("=").append("authorization_code");
            params.append("&");
            params.append("client_id").append("=").append(clientID);
            params.append("&");
            String oauthCode = req.getParameter("code");
            params.append("code").append("=").append(oauthCode);
            params.append("&");
            params.append("redirect_uri").append("=").append(this.redirectURI);


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

            final User user = this.userService.userProvisionning(userInfoMap.get("sub"), userInfoMap.get("email"));


            req.getSession(true).setAttribute("user", user);

            resp.sendRedirect("/home");

        } catch (InterruptedException | BusinessException e) {
            e.printStackTrace();
        }


    }
}
