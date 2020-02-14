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
import org.junit.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import timeboard.core.api.UpdatedTaskResult;
import timeboard.core.api.exceptions.BusinessException;
import timeboard.core.model.Imputation;
import timeboard.core.model.Task;
import timeboard.core.model.ValidationStatus;

import java.util.Calendar;
import java.util.Optional;

public class TimesheetFeatureTest extends TimeboardTest {

    @Autowired
    private TimeboardWorld world;

    private double imputationSum = 0;

    @Then("^the user has imputations all tasks of week$")
    public void theUserHasImputationsAllTasksOfWeek() throws Throwable {

        double localSum = 0.0;
        Task task = (Task) projectService.getTaskByID(world.account, world.lastTask.getId());
        Calendar start = Calendar.getInstance();
        Calendar end = Calendar.getInstance();
        start.setTime(task.getStartDate());
        end.setTime(task.getEndDate());

        while (start.before(end)) {
            Optional<Imputation> imputation = projectService.getImputation(world.account, task, start.getTime());
            Assert.assertTrue(imputation.isPresent());
            localSum += imputation.get().getValue();
            start.add(Calendar.DAY_OF_YEAR, 1);
        }

        Assert.assertEquals(localSum, imputationSum, 0);
    }

    @When("^the user add imputations all days of task$")
    public void theUserAddImputationsAllDaysOfTask() throws BusinessException {

        Task task = world.lastTask;

        Calendar start = Calendar.getInstance();
        Calendar end = Calendar.getInstance();
        start.setTime(task.getStartDate());
        end.setTime(task.getEndDate());
        while (start.before(end)) {
            UpdatedTaskResult result = timesheetService.updateTaskImputation(world.organization, world.account, task, start.getTime(), 1.0);
            Assert.assertNotNull(result);
            start.add(Calendar.DAY_OF_YEAR, 1);
            imputationSum++;
        }
    }

    @When("^the user submit his timesheet$")
    public void theUserSubmitHisTimesheet() throws BusinessException {

        Calendar c = Calendar.getInstance();
        timesheetService.submitTimesheet(world.organization, world.account, c.get(Calendar.YEAR), c.get(Calendar.WEEK_OF_YEAR));
    }

    @Then("^the timesheet is submitted$")
    public void theTimesheetIsSubmitted() {
        Calendar c = Calendar.getInstance();
        Optional<ValidationStatus> status = timesheetService.getTimesheetValidationStatus(
                world.organization, world.account, c.get(Calendar.YEAR), c.get(Calendar.WEEK_OF_YEAR));

        Assert.assertTrue(status.isPresent());
    }
}
