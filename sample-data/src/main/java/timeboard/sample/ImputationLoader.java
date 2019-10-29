package timeboard.sample;

/*-
 * #%L
 * sample-data
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


import timeboard.core.api.ProjectService;
import timeboard.core.api.UserService;
import timeboard.core.model.Task;
import timeboard.core.model.User;
import timeboard.core.model.Imputation;

import java.util.Date;
import java.util.ArrayList;
import java.util.List;


public class ImputationLoader {

    UserService userService;
    ProjectService projectService;

    ImputationLoader(ProjectService projectService, UserService userService){
        this.projectService = projectService;
        this.userService = userService;
    }

    public List<Imputation> load(List<User> usersSaved, List<Task> tasksSaved, int nbProjectsByUsers, int nbTasksByProjects, int nbImputationsByTasks){
        List<Imputation> imputationsSaved = new ArrayList<>();

        for (int i = 0; i < tasksSaved.size(); i++) {

            User actor = usersSaved.get(i/(nbProjectsByUsers*nbTasksByProjects)); // car 1 user possède "nbProjectsByUsers" projects possédant chacun "nbTasksByProjects" tâches
            Task task = tasksSaved.get(i);

            if(actor != null) {
                // Création de "nbImputationsByTasks" imputations pour simuler un nombre de jours
                for (int j = 0; j < nbImputationsByTasks; j++) {
                    Date day = new Date(new Date().getTime() + j * (1000 * 60 * 60 * 24));
                    Double value = Math.floor(Math.random() * 11)/10; // Value between 0 and 1 with 1 décimal max
                    try {
                        this.projectService.updateTaskImputation(actor, task.getId(), day, value);
                        Imputation imputation = new Imputation();
                        imputation.setDay(day);
                        imputation.setTask(task);
                        imputation.setValue(value);
                        imputationsSaved.add(imputation);
                        System.out.println("Save imputations: task" + task.getId() + " imputation" + j);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

        }

        System.out.println("Imputations saved ! ");
        return imputationsSaved;

    }


}
