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
import org.springframework.ui.ConcurrentModel;
import timeboard.core.model.Organization;
import timeboard.home.HomeController;

import java.util.Collections;
import java.util.UUID;

public class HomeFeatureBTest extends TimeboardTest {

    @Autowired
    protected HomeController homeController;
    @Autowired
    private TimeboardWorld world;
    private Organization organisationA;
    private Organization organisationB;


    @Given("^user with an existing account and (\\d+) project in (\\d+) org \\(A\\) and (\\d+) in an other org \\(B\\)$")
    public void user_with_an_existing_account_and_project_in_org_A_and_in_an_other_org_B(final int arg1, final int arg2, final int arg3) throws Throwable {
        world.model = new ConcurrentModel();
        world.account = this.accountService.userProvisioning(UUID.randomUUID().toString(), "test2");

        this.organisationA = this.organizationService.createOrganization(world.account, "Integration A", Collections.emptyMap());
        this.organisationB = this.organizationService.createOrganization(world.account, "Integration B", Collections.emptyMap());


        world.auth = SecurityUtils.signIn(organisationA, world.account);
        this.projectService.createProject(this.organisationA, world.account, "TestProjectOrgA");

        world.auth = SecurityUtils.signIn(organisationB, world.account);
        this.projectService.createProject(this.organisationB, world.account, "TestProjectOrgB");

        world.auth = SecurityUtils.signIn(organisationA, world.account);


    }

    @When("^the user calls /home on org A$")
    public void the_user_calls_home_on_org_A() throws Throwable {
        this.homeController.handleGet(world.auth, world.model);
    }

    @Then("^the user receives (\\d+) project from org A$")
    public void the_user_receives_project_form_org_A(final int arg1) throws Throwable {
        Assert.assertEquals(world.model.asMap().get(HomeController.NB_PROJECTS), arg1);
    }

}
