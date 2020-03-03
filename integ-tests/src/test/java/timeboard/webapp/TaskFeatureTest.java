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
import timeboard.projects.ProjectTasksController;

import java.util.Calendar;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

public class TaskFeatureTest extends TimeboardTest {

    @Autowired
    protected ProjectTasksController projectTasksController;
    @Autowired
    private TimeboardWorld world;
    private Project project;

    private String remoteID = new Random().nextInt() + "";

    @When("^the user create a task$")
    public void theUserCreateATask() {

        Calendar start = Calendar.getInstance();
        Calendar end = Calendar.getInstance();
        end.add(Calendar.DAY_OF_YEAR, 5);

        projectService.createTask(world.organization, world.account, world.lastProject, "", "",
                start.getTime(), end.getTime(), Math.random(),
                null, null, null, null, remoteID,
                TaskStatus.PENDING, Collections.emptyList());

    }

    @When("^the user update a task$")
    public void theUserUpdateATask() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(2020, Calendar.JANUARY, 1);
        Optional<Task> task = projectService.getTaskByRemoteID(world.account, remoteID);
        Assert.assertTrue(task.isPresent());
        Task t = task.get();
        t.setTaskStatus(TaskStatus.IN_PROGRESS);
        t.setStartDate(calendar.getTime());
        t.setAssigned(world.account);
        t.setOriginalEstimate(2.25);
        projectService.updateTask(world.organization, world.account, task.get());
    }


    @When("^the user accept a task$")
    public void theUserAcceptATask() {
        this.projectTasksController.approveTask(world.auth, world.lastTask.getId());
    }

    @When("^the user deny a task$")
    public void theUserDenyATask() {
        this.projectTasksController.denyTask(world.auth, world.lastTask.getId());

    }

    @When("^the user archive a task$")
    public void theUserArchiveATask() {
        this.projectTasksController.archiveTask(world.auth, world.lastTask.getId());
    }

    @Then("^the task has been updated$")
    public void theTaskHasBeenUpdated() {
        Optional<Task> task = projectService.getTaskByRemoteID(world.account, remoteID);
        Assert.assertTrue(task.isPresent());
        Task t = task.get();

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(t.getStartDate());
        Assert.assertEquals(TaskStatus.IN_PROGRESS, t.getTaskStatus());
        Assert.assertEquals(t.getAssigned().getId(), world.account.getId());
        Assert.assertEquals(2.25, t.getOriginalEstimate(), 0);
        Assert.assertEquals(Calendar.JANUARY, calendar.get(Calendar.MONTH));
        Assert.assertEquals(2020, calendar.get(Calendar.YEAR));
        Assert.assertEquals(1, calendar.get(Calendar.DAY_OF_MONTH));

    }

    @Then("^the user has (\\d+) pending task on project$")
    public void theUserHasPendingTaskOnProject(int arg0) throws BusinessException {
        Assert.assertEquals(listProjectTasksByTaskType(TaskStatus.PENDING).size(), arg0);

    }

    @Then("^the user has (\\d+) refused task on project$")
    public void theUserHasRefusedTaskOnProject(int arg0) throws BusinessException {
        Assert.assertEquals(listProjectTasksByTaskType(TaskStatus.REFUSED).size(), arg0);

    }

    @Then("^the user has (\\d+) in progress task on project$")
    public void theUserHasInProgressTaskOnProject(int arg0) throws BusinessException {
        Assert.assertEquals(listProjectTasksByTaskType(TaskStatus.IN_PROGRESS).size(), arg0);

    }

    @Then("^the user has (\\d+) done task on project$")
    public void theUserHasDoneTaskOnProject(int arg0) throws BusinessException {
        Assert.assertEquals(listProjectTasksByTaskType(TaskStatus.DONE).size(), arg0);

    }

    @Then("^the user has (\\d+) archived task on project$")
    public void theUserHasArchivedTaskOnProject(int arg0) throws BusinessException {
        Assert.assertEquals(listProjectTasksByTaskType(TaskStatus.ARCHIVED).size(), arg0);
    }



    List<Task> listProjectTasksByTaskType(TaskStatus taskStatus) throws BusinessException {
        return projectService.listProjectTasks(world.account, world.lastProject)
                .stream()
                .filter(task -> task.getTaskStatus() == taskStatus)
                .collect(Collectors.toList());
    }
}
