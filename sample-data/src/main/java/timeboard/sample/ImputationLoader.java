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
import timeboard.core.model.Account;
import timeboard.core.model.Imputation;
import timeboard.core.model.Task;

import java.text.SimpleDateFormat;
import java.util.*;


public class ImputationLoader {

    UserService userService;
    ProjectService projectService;

    ImputationLoader(ProjectService projectService, UserService userService){
        this.projectService = projectService;
        this.userService = userService;
    }

    public List<Imputation> load(List<Account> usersSaved, List<Task> tasksSaved, int nbProjectsByUsers, int nbTasksByProjects, int nbImputationsByTasks){
        List<Imputation> imputationsSaved = new ArrayList<>();
        Map<String, Double> mapDateSumImput = new HashMap<String, Double>();

        for (int i = 0; i < tasksSaved.size(); i++) {

            Account actor = usersSaved.get(i/(nbProjectsByUsers*nbTasksByProjects)); // car 1 user possède "nbProjectsByUsers" projects possédant chacun "nbTasksByProjects" tâches
            Task task = tasksSaved.get(i);

            if(actor != null) {
                // Création de "nbImputationsByTasks" imputations pour simuler un nombre de jours
                for (int j = 0; j < nbImputationsByTasks; j++) {
                    Date day = new Date(new Date().getTime() + j * (1000 * 60 * 60 * 24));
                    double value = this.getRandomValue(mapDateSumImput, day);//matTaskImput);
                    if(mapDateSumImput.containsKey(this.getSimpleDate(day))) {
                        mapDateSumImput.put(this.getSimpleDate(day), mapDateSumImput.get(this.getSimpleDate(day)) + value);
                    }else{
                        mapDateSumImput.put(this.getSimpleDate(day), value);
                    }

                    try {
                        this.projectService.updateTaskImputation(actor, task, day, value);
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

    private double getRandomValue(Map<String, Double> mapDateSumImput, Date day){//double[][] matTaskImput){
        double value = Math.floor(Math.random() * 6)/10; // Value between 0 and 0.5 with 1 décimal max

        // Vérification: Pour une journée la valeur du temps total passé sur les tâches ne doit pas être supérieur à 1
        double sum = 0.0;
        if(mapDateSumImput.containsKey(this.getSimpleDate(day))){
            sum = mapDateSumImput.get(this.getSimpleDate(day));
        }
        if(sum + value > 1.0 ){
            value = 1.0 - sum;
        }

        return value;
    }

    private String getSimpleDate(Date day){
        return new SimpleDateFormat("yyyy-MM-dd").format(day);
    }


}
