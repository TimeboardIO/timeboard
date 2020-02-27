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
import org.springframework.beans.factory.annotation.Value;
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
    private Calendar myStartDate = Calendar.getInstance();
    private Calendar myEndDate = Calendar.getInstance();
    private Calendar dateDuringVacation = Calendar.getInstance();
    private Calendar next3Week = Calendar.getInstance();



    private VacationsController.VacationRequestWrapper getExampleVacationRequestWrapper(String label) {

        VacationsController.VacationRequestWrapper vacationRequestWrapper = new VacationsController.VacationRequestWrapper();
        vacationRequestWrapper.setRecursive(false); // non recursive
        vacationRequestWrapper.setStart(myStartDate.getTime());
        vacationRequestWrapper.setEnd(myEndDate.getTime());
        vacationRequestWrapper.setHalfStart(false); //morning and afternoon of start day
        vacationRequestWrapper.setHalfEnd(true); //only morning of end day
        vacationRequestWrapper.setLabel(label);
        vacationRequestWrapper.setAssigneeID(world.account.getId());
        vacationRequestWrapper.setAssigneeName(world.account.getScreenName());
        return vacationRequestWrapper;
    }

    private VacationsController.VacationRequestWrapper getExampleRecursiveVacationRequestWrapper(String label) {

        VacationsController.VacationRequestWrapper vacationRequestWrapper = new VacationsController.VacationRequestWrapper();
        vacationRequestWrapper.setRecursive(true); // recursive
        vacationRequestWrapper.setRecurrenceDay(Calendar.MONDAY); // all mondays
        vacationRequestWrapper.setRecurrenceType("MORNING"); // only morning
        vacationRequestWrapper.setStart(myStartDate.getTime());
        vacationRequestWrapper.setEnd(myEndDate.getTime());
        vacationRequestWrapper.setLabel(label);
        vacationRequestWrapper.setAssigneeID(world.account.getId());
        vacationRequestWrapper.setAssigneeName(world.account.getScreenName());
        return vacationRequestWrapper;
    }

    @Given("^start and end date of vacations$")
    public void startAndEndDateOfVacations() {

        myStartDate = Calendar.getInstance();
        myStartDate.setFirstDayOfWeek(Calendar.MONDAY);
        myStartDate.add(Calendar.WEEK_OF_YEAR, 1);
        myStartDate.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);

        dateDuringVacation = Calendar.getInstance();
        dateDuringVacation.setFirstDayOfWeek(Calendar.MONDAY);
        dateDuringVacation.add(Calendar.WEEK_OF_YEAR, 1);
        dateDuringVacation.set(Calendar.DAY_OF_WEEK, Calendar.TUESDAY);

        myEndDate = Calendar.getInstance();
        myEndDate.setFirstDayOfWeek(Calendar.MONDAY);
        myEndDate.add(Calendar.WEEK_OF_YEAR, 1);
        myEndDate.set(Calendar.DAY_OF_WEEK, Calendar.WEDNESDAY);
    }

    @Given("^start and end date of recursive vacations$")
    public void startAndEndDateOfRecursiveVacations() {
        myStartDate = Calendar.getInstance();
        myStartDate.setFirstDayOfWeek(Calendar.MONDAY);
        myStartDate.add(Calendar.WEEK_OF_YEAR, 1);
        myStartDate.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);

        myEndDate = Calendar.getInstance();
        myEndDate.setFirstDayOfWeek(Calendar.MONDAY);
        myEndDate.add(Calendar.WEEK_OF_YEAR, 2);
        myEndDate.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);

        next3Week = Calendar.getInstance();
        next3Week.setFirstDayOfWeek(Calendar.MONDAY);
        next3Week.add(Calendar.WEEK_OF_YEAR, 3);
        next3Week.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
    }

    @Given("^user with an existing account and (\\d+) projects and (\\d+) vacations$")
    public void userWithAnExistingAccountAndProjectsAndVacations(final int arg0, final int arg1) throws Throwable {
        globalStepDefinitions.user_with_an_existing_account_and_projects(arg0);

        for (int i = 0; i < arg1; i++) {
            String label = "My best vacations " + (i+1);

            VacationsController.VacationRequestWrapper vacationRequestWrapper = this.getExampleVacationRequestWrapper(label);
            vacationsController.createRequest(world.auth, vacationRequestWrapper);
        }
    }

    @Given("^user with an existing account and (\\d+) projects and (\\d+) recursive vacations$")
    public void userWithAnExistingAccountAndProjectsAndRecursiveVacations(int arg0, int arg1) throws Throwable {
        globalStepDefinitions.user_with_an_existing_account_and_projects(arg0);

        for (int i = 0; i < arg1; i++) {
            String label = "My best Recursive vacations " + (i+1);

            VacationsController.VacationRequestWrapper vacationRequestWrapper = this.getExampleRecursiveVacationRequestWrapper(label);
            vacationsController.createRequest(world.auth, vacationRequestWrapper);
        }

    }



    @When("^the user create a vacation$")
    public void theUserCreateAVacation() {
        String label = "My best vacations";

        VacationsController.VacationRequestWrapper vacationRequestWrapper = this.getExampleVacationRequestWrapper(label);
        vacationsController.createRequest(world.auth, vacationRequestWrapper);

    }

    @When("^the user create a recursive vacation$")
    public void theUserCreateARecursiveVacation() {
        String label = "My monday morning recursive vacations";

        VacationsController.VacationRequestWrapper vacationRequestWrapper = this.getExampleRecursiveVacationRequestWrapper(label);
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


    @When("^the user delete a accepted vacation$")
    public void theUserDeleteAAcceptedVacation() throws BusinessException {
        List<VacationRequest> vacations = vacationService.listVacationRequestsByUser(world.account, world.organization)
                .stream()
                .filter(v -> v.getStatus() == VacationRequestStatus.ACCEPTED)
                .collect(Collectors.toList());
        vacationsController.deleteRequest(world.auth, vacations.stream().findFirst().get());
    }

    @When("^the user accept a pending vacation$")
    public void theUserAcceptAPendingVacation() throws BusinessException {
        List<VacationRequest> vacations = vacationService.listVacationRequestsByUser(world.account, world.organization)
                .stream()
                .filter(v -> v.getStatus() == VacationRequestStatus.PENDING)
                .collect(Collectors.toList());
        vacationsController.approveRequest(world.auth, vacations.stream().findFirst().get());
    }

    @When("^the user reject a pending vacation$")
    public void theUserRejectAPendingVacation() throws BusinessException {
        List<VacationRequest> vacations = vacationService.listVacationRequestsByUser(world.account, world.organization)
                .stream()
                .filter(v -> v.getStatus() == VacationRequestStatus.PENDING)
                .collect(Collectors.toList());
        vacationsController.rejectRequest(world.auth, vacations.stream().findFirst().get());
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

    @Then("^the user has (\\d+) pending recursive vacations$")
    public void theUserHasRecursiveVacations(int arg0) {
        List<VacationRequest> vacations = vacationService.listVacationRequestsByUser(world.account, world.organization);
        Assert.assertEquals(vacations.stream().filter(v -> v instanceof RecursiveVacationRequest
                && v.getStatus() == VacationRequestStatus.PENDING).count(), arg0); // pending recursive vacation
    }

    @Then("^the user has (\\d+) accepted recursive vacations$")
    public void theUserHasAcceptedRecursiveVacations(int arg0) {
        List<VacationRequest> vacations = vacationService.listVacationRequestsByUser(world.account, world.organization);
        Assert.assertEquals(vacations.stream().filter(v -> v instanceof RecursiveVacationRequest
                && v.getStatus() == VacationRequestStatus.ACCEPTED).count(), arg0);
    }

    @Then("^the user has (\\d+) rejected recursive vacations$")
    public void theUserHasRejectedRecursiveVacations(int arg0) {
            List<VacationRequest> vacations = vacationService.listVacationRequestsByUser(world.account, world.organization);
            Assert.assertEquals(vacations.stream().filter(v -> v instanceof RecursiveVacationRequest
                    && v.getStatus() == VacationRequestStatus.REJECTED).count(), arg0);
    }

    @Then("^the user has imputations on his vacations$")
    public void theUserHasImputationsOnHisVacations() {
        DefaultTask vacationTask = this.vacationService.getVacationTask(world.account, world.organization.getId());
        //first day
        Imputation firstImputation = this.projectService.getImputation(world.account, vacationTask, myStartDate.getTime()).orElse(null);
        Assert.assertNotNull(firstImputation);
        Assert.assertEquals(firstImputation.getValue(), 1, 0);
        //all day
        Imputation imputation = this.projectService.getImputation(world.account, vacationTask, dateDuringVacation.getTime()).orElse(null);
        Assert.assertNotNull(imputation);
        Assert.assertEquals(imputation.getValue(), 1, 0);
        //last day
        Imputation lastImputation = this.projectService.getImputation(world.account, vacationTask, myEndDate.getTime()).orElse(null);
        Assert.assertNotNull(lastImputation);
        Assert.assertEquals(lastImputation.getValue(), 0.5, 0);
    }

    @Then("^the user has no imputations on his vacations$")
    public void theUserHasNoImputationsOnHisVacations() {
        DefaultTask vacationTask = this.vacationService.getVacationTask(world.account, world.organization.getId());
        //first day
        Imputation firstImputation = this.projectService.getImputation(world.account, vacationTask, myStartDate.getTime()).orElse(null);
        Assert.assertNull(firstImputation);
        //all day
        Imputation imputation = this.projectService.getImputation(world.account, vacationTask, dateDuringVacation.getTime()).orElse(null);
        Assert.assertNull(imputation);
        //last day
        Imputation lastImputation = this.projectService.getImputation(world.account, vacationTask, myEndDate.getTime()).orElse(null);
        Assert.assertNull(lastImputation);
    }

    @Then("^the user has imputations on his recursive vacations$")
    public void theUserHasImputationsOnHisRecursiveVacations() {
        DefaultTask vacationTask = this.vacationService.getVacationTask(world.account, world.organization.getId());
        // monday in 1 week
        Imputation firstImputation = this.projectService.getImputation(world.account, vacationTask, myStartDate.getTime()).orElse(null);
        Assert.assertNotNull(firstImputation);
        Assert.assertEquals(firstImputation.getValue(), 0.5, 0);
        // monday in 2 week
        Imputation lastImputation = this.projectService.getImputation(world.account, vacationTask, myEndDate.getTime()).orElse(null);
        Assert.assertNotNull(lastImputation);
        Assert.assertEquals(lastImputation.getValue(), 0.5, 0);
        // monday in 3 week
        Imputation noImputation = this.projectService.getImputation(world.account, vacationTask, next3Week.getTime()).orElse(null);
        Assert.assertNull(noImputation);
    }

    @Then("^the user has no imputations on his recursive vacations$")
    public void theUserHasNoImputationsOnHisRecursiveVacations() {
        DefaultTask vacationTask = this.vacationService.getVacationTask(world.account, world.organization.getId());
        // monday in 1 week
        Imputation firstImputation = this.projectService.getImputation(world.account, vacationTask, myStartDate.getTime()).orElse(null);
        Assert.assertNull(firstImputation);
        // monday in 2 week
        Imputation lastImputation = this.projectService.getImputation(world.account, vacationTask, myEndDate.getTime()).orElse(null);
        Assert.assertNull(lastImputation);
        // monday in 3 week
        Imputation noImputation = this.projectService.getImputation(world.account, vacationTask, next3Week.getTime()).orElse(null);
        Assert.assertNull(noImputation);
    }
}
