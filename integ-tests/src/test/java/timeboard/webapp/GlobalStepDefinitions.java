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
import org.aspectj.weaver.World;
import org.junit.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.ConcurrentModel;
import org.springframework.ui.Model;
import timeboard.core.model.Account;
import timeboard.core.model.Organization;
import timeboard.core.security.TimeboardAuthentication;
import timeboard.home.HomeController;

import java.util.Collections;
import java.util.Random;
import java.util.UUID;

public class GlobalStepDefinitions extends TimeboardTest {

    @Autowired
    protected HomeController homeController;

    @Autowired
    private TimeboardWorld world;

    @Given("^user with an existing account and (\\d+) projects$")
    public void user_with_an_existing_account_and_projects(final int arg1) throws Throwable {

        world.model = new ConcurrentModel();
        world.account = this.accountService.userProvisionning(UUID.randomUUID().toString(), "test");

        world.organization = this.organizationService.createOrganization(world.account, "Integration"+ new Random().nextInt(), Collections.emptyMap());

        Assert.assertNotNull(world.account);
        world.auth = new TimeboardAuthentication(world.account);
        world.auth.setCurrentOrganization(world.organization);
        SecurityContextHolder.getContext().setAuthentication(world.auth);

        for (int i = 0; i < arg1; i++) {
            this.projectService.createProject(world.organization, world.account, "TestProject"+i);
        }
    }



}
