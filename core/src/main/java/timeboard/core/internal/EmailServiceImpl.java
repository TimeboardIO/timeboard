package timeboard.core.internal;

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

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import timeboard.core.api.EmailService;
import timeboard.core.internal.observers.emails.EmailStructure;
import timeboard.core.internal.observers.emails.TemplateGenerator;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.stream.Collectors;


@Component
public class EmailServiceImpl implements EmailService {


    @Value("${timeboard.mail.fromEmail}")
    private String fromEmail;
    @Value("${timeboard.mail.host}")
    private String host;
    @Value("${timeboard.mail.port}")
    private String port;
    @Value("${timeboard.smtp.username}")
    private String username;
    @Value("${timeboard.smtp.password}")
    private String password;


    @Autowired
    private TemplateGenerator templateGenerator;

    @Override
    public void sendMessage(final EmailStructure emailStructure, Locale locale) throws MessagingException {
        final Properties props = new Properties();
        props.setProperty("mail.host", host);
        props.setProperty("mail.smtp.port", port);
        props.setProperty("mail.smtp.username", username);
        props.setProperty("mail.smtp.password", password);

        final Session session = Session.getInstance(props, null);

        final String message = templateGenerator.getTemplateString(emailStructure.getTemplate(), emailStructure.getModel(), locale);

        final MimeMessage msg = new MimeMessage(session);
        msg.setSubject("[Timeboard] " + emailStructure.getSubject());
        msg.setFrom(new InternetAddress(fromEmail));
        msg.setContent(message, "text/html; charset=utf-8");

        final List<String> listToEmailsWithoutDuplicate = emailStructure.getTargetUserList()
                .stream().distinct().collect(Collectors.toList());
        final String targetToEmails = StringUtils.join(listToEmailsWithoutDuplicate, ',');
        msg.addRecipients(Message.RecipientType.TO, InternetAddress.parse(targetToEmails));

        if (emailStructure.getTargetCCUserList() != null) {
            final List<String> listCCEmailsWithoutDuplicate = emailStructure.getTargetCCUserList()
                    .stream().distinct().collect(Collectors.toList());
            final String targetCCEmails = StringUtils.join(listCCEmailsWithoutDuplicate, ',');
            msg.addRecipients(Message.RecipientType.CC, InternetAddress.parse(targetCCEmails));
        }
        Transport.send(msg);
    }


    public void sendMessage(final EmailStructure emailStructure) throws MessagingException {
        this.sendMessage(emailStructure, Locale.FRANCE);
    }
}