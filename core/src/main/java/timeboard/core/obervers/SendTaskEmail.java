package timeboard.core.obervers;

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

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import timeboard.core.api.EmailService;
import timeboard.core.api.SendTaskEmailService;
import timeboard.core.internal.TimeboardSubjects;
import timeboard.core.model.*;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;


@Component(
        service = SendTaskEmailService.class,
        immediate = true
)
public class SendTaskEmail implements SendTaskEmailService {

    @Reference
    EmailService emailService;

    @Activate
    public void activate(){

        TimeboardSubjects.NEW_TASK
                .map(newTask -> sendEmailCreatingTask((Task) newTask) )
                .subscribe(emailStructure ->this.emailService.sendMessage(emailStructure));
    }

    @Override
    public EmailStructure sendEmailCreatingTask(Task newTaskDB) {
        List<String> to = new ArrayList<>();
        Project project = newTaskDB.getProject();
        User actor = newTaskDB.getLatestRevision().getRevisionActor();
        User assignedUser = newTaskDB.getLatestRevision().getAssigned();

        project.getMembers()
                .stream()
                .filter(member -> member.getRole() == ProjectRole.OWNER)
                .forEach(member -> to.add(member.getMember().getEmail()));

        List<String> cc = Arrays.asList(assignedUser.getEmail(), actor.getEmail());

        String subject = "Mail de création d'une tâche";
        String message = "Bonjour,\n"
                + actor.getFirstName() + " " + actor.getName() + " a ajouté une tâche au " + this.getDisplayFormatDate(new Date()) + ".\n"
                +"Nom de la tâche : " + newTaskDB.getName() + "." + "\n"
                +"Date de début : " + this.getDisplayFormatDate(newTaskDB.getStartDate()) + "." + "\n"
                +"Date de fin : " + this.getDisplayFormatDate(newTaskDB.getEndDate()) + "." + "\n"
                +"Estimation initiale : " + newTaskDB.getEstimateWork() + "." + "\n"
                +"Projet : " + project.getName() + "." + "\n";

        return new EmailStructure(to, cc, subject, message);
    }

    private String getDisplayFormatDate(Date date){
        return  new SimpleDateFormat("dd/MM/yyyy").format(date);
    }

}

