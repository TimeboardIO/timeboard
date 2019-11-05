package timeboard.security.service;

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


import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;
import timeboard.security.api.EmailService;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Map;
import java.util.Properties;

@Component(
        service = EmailService.class,
        scope = ServiceScope.SINGLETON,
        property = {
                "timeboard.mail.fromEmail=paasport@tsc-nantes.com",
                "timeboard.mail.host=10.10.0.48",
                "timeboard.mail.port=25"

        },
        configurationPid = "timeboard.security.EmailService"
)
public class EmailServiceImpl implements EmailService {


    private String fromEmail;
    private String host;
    private String port;

    @Activate
    private void init(Map<String, String> conf){
        this.fromEmail = conf.get("timeboard.mail.fromEmail");
        this.host = conf.get("timeboard.mail.host");
        this.port = conf.get("timeboard.mail.port");
    }

    @Override
    public void sendMessage(String targetEmail, String subject, String message) throws MessagingException {
            Properties props = new Properties();
            props.setProperty("mail.host", host);
            props.setProperty("mail.smtp.port", port);

            Session session = Session.getInstance(props, null);

            MimeMessage msg = new MimeMessage(session);
            msg.setText(message);
            msg.setSubject(subject);
            msg.setFrom(new InternetAddress(fromEmail));
            msg.addRecipient(Message.RecipientType.TO, new InternetAddress(targetEmail));
            Transport.send(msg);
    }
}
