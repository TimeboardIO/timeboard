package timeboard.webapp;

/*-
 * #%L
 * integ-tests
 * %%
 * Copyright (C) 2019 - 2020 Timeboard
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

import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import edu.emory.mathcs.backport.java.util.Collections;
import org.junit.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import timeboard.core.api.exceptions.BusinessException;
import timeboard.core.model.Project;
import timeboard.core.model.Task;
import timeboard.core.model.TaskStatus;
import timeboard.core.model.TaskType;
import timeboard.projects.ProjectsController;

import java.util.Calendar;
import java.util.List;
import java.util.Random;

public class TaskFeatureATest extends TimeboardTest {

    @Autowired
    private TimeboardWorld world;

    @Autowired
    protected ProjectsController projectsController;

    private Project project;

    @When("^the user create a task$")
    public void theUserCreateATask() {

        Calendar start = Calendar.getInstance();
        Calendar end = Calendar.getInstance();
        end.add(Calendar.DAY_OF_YEAR, 5);

        projectService.createTask(world.organization, world.account, world.project,"","",
                start.getTime(), end.getTime(), new Random().nextInt(),
                null,  null,null, null, null,
                TaskStatus.PENDING, Collections.emptyList());

    }

    @Then("^the user has (\\d+) task on project$")
    public void theUserHasTaskOnProject(int arg0) throws BusinessException {
        List<Task> tasks = projectService.listProjectTasks(world.account, world.project);
        Assert.assertEquals(tasks.size(), 1);

    }
}
