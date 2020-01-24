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

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringRunner;
import timeboard.core.api.OrganizationService;
import timeboard.core.api.ProjectService;
import timeboard.core.api.UserService;
import timeboard.core.api.exceptions.BusinessException;
import timeboard.core.model.Account;
import timeboard.core.model.MembershipRole;
import timeboard.core.model.Organization;
import timeboard.core.model.Project;

import java.util.Collections;
import java.util.UUID;

@RunWith(SpringRunner.class)
public class ProjectsTest extends TimeboardTest {

    @Autowired
    private UserService userService;

    @Autowired
    private OrganizationService organizationService;

    @Autowired
    private ProjectService projectService;

    @Test
    public void testCreateProject() throws BusinessException {

        final Organization org = this.organizationService.createOrganization("testOrg", Collections.emptyMap());
        final Account a1 = this.userService.userProvisionning(UUID.randomUUID().toString(), "test1@test.fr");
        SecurityUtils.signIn(org, a1);
        this.organizationService.addMembership(a1, org, a1, MembershipRole.OWNER);

        final Project project = this.projectService.createProject(a1, "SampleProject");

        Assert.assertNotNull(project);
        Assert.assertNotNull(project.getId());
        Assert.assertEquals(project.getName(), "SampleProject");

        final Project projectFromDB = this.projectService.getProjectByID(a1, org.getId(), project.getId());
        Assert.assertEquals(project.getId(), projectFromDB.getId());
    }

}
