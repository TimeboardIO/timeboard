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
import org.springframework.ui.Model;
import timeboard.core.api.ThreadLocalStorage;
import timeboard.core.model.Account;
import timeboard.core.model.MembershipRole;
import timeboard.core.model.Organization;
import timeboard.core.model.OrganizationMembership;
import timeboard.core.security.TimeboardAuthentication;
import timeboard.home.HomeController;

import java.util.Collections;
import java.util.UUID;

public class HomeFeatureATest extends TimeboardTest {

    @Autowired
    protected HomeController homeController;

    private Account account;
    private Organization organisation;
    private TimeboardAuthentication auth;
    private Model model;



    @Given("^user with an existing account and (\\d+) project$")
    public void user_with_an_existing_account_and_project(int arg1) throws Throwable {

        this.model = new ConcurrentModel();
        this.organisation = this.organizationService.createOrganization("Integration", Collections.emptyMap());

        this.account = this.userService.userProvisionning(UUID.randomUUID().toString(), "test");
        Assert.assertNotNull(this.account);
        this.auth = new TimeboardAuthentication(this.account);
        this.auth.setCurrentOrganization(this.organisation.getId());
        this.account.getOrganizations().add(new OrganizationMembership(this.organisation, this.account, MembershipRole.OWNER));
        SecurityContextHolder.getContext().setAuthentication(this.auth);
        ThreadLocalStorage.setCurrentOrgId(this.organisation.getId());

        this.projectService.createProject(this.account, "TestProject");
    }

    @When("^the user calls /home$")
    public void the_client_calls_home() throws Throwable {
        this.homeController.handleGet(this.auth, this.model);
    }

    @Then("^the user receives (\\d+) project$")
    public void the_user_receives_project(int arg1) throws Throwable {
        Assert.assertEquals(this.model.asMap().get(HomeController.NB_PROJECTS), 1);
    }

}
