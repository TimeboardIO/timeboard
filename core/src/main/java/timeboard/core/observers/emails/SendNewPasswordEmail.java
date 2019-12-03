package timeboard.core.observers.emails;

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

import io.reactivex.schedulers.Schedulers;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.springframework.beans.factory.annotation.Autowired;
import timeboard.core.api.EmailService;
import timeboard.core.api.TimeboardSubjects;
import timeboard.core.model.User;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Executors;



@Component(
        service = SendNewPasswordEmail.class,
        immediate = true
)
//TODO keep this useless class ?
public class SendNewPasswordEmail {

    @Autowired
    EmailService emailService;

    @Activate
    public void activate() {

        TimeboardSubjects.GENERATE_PASSWORD
                .map(userPasswordMap -> sendEmailNewPassword(userPasswordMap))
                .observeOn(Schedulers.from(Executors.newFixedThreadPool(10)))
                .subscribe(emailStructure -> this.emailService.sendMessage(emailStructure));
    }

    public EmailStructure sendEmailNewPassword(Map<User, String> userPasswordMap) {
        User user = (User) userPasswordMap.keySet().toArray()[0];
        String password = userPasswordMap.get(user);

        List<String> to = Arrays.asList(user.getEmail());
        List<String> cc = new ArrayList<>();
        String subject = "Réinitialisation du mot de passe";
        String message = "Voici pour l'identifiant suivant, le nouveau mot de passe:\n\n "
                + "Login: " + user.getEmail() + "\nMot de passe: " + password;

        return new EmailStructure(to, cc, subject, message);
    }

    private String getDisplayFormatDate(Date date) {
        return  new SimpleDateFormat("dd/MM/yyyy").format(date);
    }

}

