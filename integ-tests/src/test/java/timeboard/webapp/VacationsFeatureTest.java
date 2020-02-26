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

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.junit.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.ConcurrentModel;
import timeboard.core.api.VacationService;
import timeboard.core.api.exceptions.BusinessException;
import timeboard.core.model.*;
import timeboard.core.security.TimeboardAuthentication;
import timeboard.projects.ProjectsController;
import timeboard.vacations.VacationsController;

import java.util.*;
import java.util.Calendar;
import java.util.stream.Collectors;

public class VacationsFeatureTest extends TimeboardTest {

    @Autowired
    protected VacationService vacationService;
    @Autowired
    protected VacationsController vacationsController;
    @Autowired
    protected GlobalStepDefinitions globalStepDefinitions;

    @Autowired
    private TimeboardWorld world;
    private Project project;

    private String remoteID = new Random().nextInt() + "";

    @Given("^user with an existing account and (\\d+) projects and (\\d+) vacations$")
    public void userWithAnExistingAccountAndProjectsAndVacations(final int arg0, final int arg1) throws Throwable {
        globalStepDefinitions.user_with_an_existing_account_and_projects(arg0);

        for (int i = 0; i < arg1; i++) {
            Calendar startDate = Calendar.getInstance();
            Calendar endDate = Calendar.getInstance();
            endDate.add(Calendar.DAY_OF_YEAR, 3);
            String label = "My best vacations " + (i+1);
            VacationsController.VacationRequestWrapper vacationRequestWrapper = this.getExampleVacationRequestWrapper(startDate, endDate, label);
            vacationsController.createRequest(world.auth, vacationRequestWrapper);
        }
    }

    @When("^the user create a vacation$")
    public void theUserCreateAVacation() {

        Calendar startDate = Calendar.getInstance();
        Calendar endDate = Calendar.getInstance();
        endDate.add(Calendar.DAY_OF_YEAR, 3);
        String label = "My best vacations";

        VacationsController.VacationRequestWrapper vacationRequestWrapper = this.getExampleVacationRequestWrapper(startDate, endDate, label);
        vacationsController.createRequest(world.auth, vacationRequestWrapper);

    }

    @When("^the user delete a pending vacation$")
    public void theUserDeleteAPendingVacation() throws BusinessException {
        List<VacationRequest> vacations = vacationService.listVacationRequestsByUser(world.account, world.organization)
                .stream()
                .filter(v -> v.getStatus() == VacationRequestStatus.PENDING)
                .collect(Collectors.toList());
        vacationsController.deleteRequest(world.auth, vacations.stream().findFirst().get());
    }

    @When("^the user approve a pending vacation$")
    public void theUserApproveAPendingVacation() throws BusinessException {
        List<VacationRequest> vacations = vacationService.listVacationRequestsByUser(world.account, world.organization)
                .stream()
                .filter(v -> v.getStatus() == VacationRequestStatus.PENDING)
                .collect(Collectors.toList());
        vacationsController.approveRequest(world.auth, vacations.stream().findFirst().get());
    }

    @When("^the user deny a pending vacation$")
    public void theUserDenyAPendingVacation() throws BusinessException {
        List<VacationRequest> vacations = vacationService.listVacationRequestsByUser(world.account, world.organization)
                .stream()
                .filter(v -> v.getStatus() == VacationRequestStatus.PENDING)
                .collect(Collectors.toList());
        vacationsController.rejectRequest(world.auth, vacations.stream().findFirst().get());
    }

    @Then("^the user has (\\d+) vacations$")
    public void theUserHasVacations(int arg0) throws BusinessException {
        List<VacationRequest> vacations = vacationService.listVacationRequestsByUser(world.account, world.organization);
        Assert.assertEquals(vacations.size(), arg0);

    }

    @Then("^the user has (\\d+) pending vacations$")
    public void theUserHasPendingVacations(int arg0) throws BusinessException {
        List<VacationRequest> vacations = vacationService.listVacationRequestsByUser(world.account, world.organization);
        Assert.assertEquals(vacations.stream().filter(v -> v.getStatus() == VacationRequestStatus.PENDING).count(), arg0);

    }

    @Then("^the user has (\\d+) accepted vacations$")
    public void theUserHasAcceptedVacations(int arg0) throws BusinessException {
        List<VacationRequest> vacations = vacationService.listVacationRequestsByUser(world.account, world.organization);
        Assert.assertEquals(vacations.stream().filter(v -> v.getStatus() == VacationRequestStatus.ACCEPTED).count(), arg0);

    }

    @Then("^the user has (\\d+) rejected vacations$")
    public void theUserHasRejectedVacations(int arg0) throws BusinessException {
        List<VacationRequest> vacations = vacationService.listVacationRequestsByUser(world.account, world.organization);
        Assert.assertEquals(vacations.stream().filter(v -> v.getStatus() == VacationRequestStatus.REJECTED).count(), arg0);

    }









    private VacationsController.VacationRequestWrapper getExampleVacationRequestWrapper(Calendar startDate, Calendar endDate, String label) {
        VacationsController.VacationRequestWrapper vacationRequestWrapper = new VacationsController.VacationRequestWrapper();
        vacationRequestWrapper.setRecursive(false); // non recursive
        vacationRequestWrapper.setStart(startDate.getTime());
        vacationRequestWrapper.setEnd(endDate.getTime());
        vacationRequestWrapper.setHalfStart(false); //morning and afternoon of start day
        vacationRequestWrapper.setHalfEnd(true); //only morning of end day
        vacationRequestWrapper.setLabel(label);
        vacationRequestWrapper.setAssigneeID(world.account.getId());
        vacationRequestWrapper.setAssigneeName(world.account.getScreenName());
        return vacationRequestWrapper;
    }
}
