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
import timeboard.core.model.Project;
import timeboard.core.model.Task;
import timeboard.core.model.TaskStatus;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class TaskLoader {

    UserService userService;
    ProjectService projectService;

    TaskLoader(ProjectService projectService, UserService userService) {
        this.projectService = projectService;
        this.userService = userService;
    }

    public List<Task> load(List<Account> usersSaved, List<Project> projectsSaved, int nbProjectsByUsers, int nbTasksByProjects) {
        List<Task> tasksSaved = new ArrayList<>();
        for (int i = 0; i < projectsSaved.size(); i++) {

            Account owner = usersSaved.get(i / nbProjectsByUsers); // car 1 user possède "nbProjectsByUsers" projects
            Project project = projectsSaved.get(i);

            if (owner != null && project != null) {
                // On créé "nbTasksByProjects" tâches par projet
                for (int j = 0; j < nbTasksByProjects; j++) {
                    try {
                        tasksSaved.add(this.projectService.createTask(owner,
                                project, "task-project" + i + "-task" + j,
                                "comment task" + j,
                                new Date(),
                                new Date(new Date().getTime() + 10 * (1000 * 60 * 60 * 24)),
                                8,
                                null,
                                owner,
                                ProjectService.ORIGIN_TIMEBOARD,
                                null,
                                null,
                                TaskStatus.PENDING,
                                null));


                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

        }

        return tasksSaved;

    }


}
