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

import org.osgi.framework.BundleException;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import timeboard.core.api.ProjectService;
import timeboard.core.api.UserService;
import timeboard.core.api.exceptions.BusinessException;
import timeboard.core.model.Account;
import timeboard.core.model.Imputation;
import timeboard.core.model.Project;
import timeboard.core.model.Task;

import java.util.List;

@Component(
        service = LauncherLoader.class,
        immediate = true
)
public class LauncherLoader {

    private final int nbUsers = 5;
    private final int nbProjectsByUsers = 3;
    private final int nbTasksByProjects = 5;
    private final int nbImputationsByTasks = 10;
    @Reference
    ProjectService projectService;
    @Reference
    UserService userService;

    @Activate
    public void load() throws BundleException, BusinessException {

        // Launch the creation of sample datas
        try {
            List<Account> usersSaved = new UserLoader(this.userService).load(nbUsers);

            List<Project> projectsSaved = new ProjectLoader(this.projectService, this.userService)
                    .load(usersSaved, nbProjectsByUsers);

            List<Task> tasksSaved = new TaskLoader(this.projectService, this.userService)
                    .load(usersSaved, projectsSaved, nbProjectsByUsers, nbTasksByProjects);

            List<Imputation> imputationsSaved = new ImputationLoader(this.projectService, this.userService)
                    .load(usersSaved, tasksSaved, nbProjectsByUsers, nbTasksByProjects, nbImputationsByTasks);

        } catch (BusinessException e) {
            e.printStackTrace();
        }

        // Stop the sample-data bundle
        try {
            FrameworkUtil.getBundle(LauncherLoader.class).getBundleContext().getBundle().stop();
        } catch (BundleException e) {
            e.printStackTrace();
        }

    }


}
