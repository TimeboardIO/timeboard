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
import timeboard.core.model.User;
import timeboard.core.model.Project;
import org.osgi.service.component.annotations.Reference;

import java.util.Date;


public class TaskLoader {

    @Reference
    UserService userService;

    @Reference
    ProjectService projectService;

    public void load() {
        for (int i = 0; i < 50; i++) {

            User owner = this.userService.findUserByLogin("timeboard" + i);
            Long projectId = Long.valueOf(1000 + (2*i) + 1);
            Project project = this.projectService.getProjectByID(owner, projectId);

            if(owner != null && project != null) {
                for (int j = 0; j < 20; j++) {
                    try {
                        this.projectService.createTask(owner, project, "task-project" + i + "-task" + j, "comment task" + j, new Date(), new Date(new Date().getTime() + 10 * (1000 * 60 * 60 * 24)), 8, null, owner);
                        System.out.println("Save task: " + "task" + j);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

        }

    }


}
