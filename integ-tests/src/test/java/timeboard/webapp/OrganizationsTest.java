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
import timeboard.core.api.UserService;
import timeboard.core.api.exceptions.BusinessException;
import timeboard.core.model.Account;
import timeboard.core.model.MembershipRole;
import timeboard.core.model.Organization;

import java.util.Collections;
import java.util.UUID;

@RunWith(SpringRunner.class)
public class OrganizationsTest extends TimeboardTest {

    @Autowired
    private UserService userService;

    @Autowired
    private OrganizationService organizationService;


    @Test
    public void testRemoveOrganizationMember() throws BusinessException {

        final Organization org = this.organizationService.createOrganization("testOrg", Collections.emptyMap());
        final Account a1 = this.userService.userProvisionning(UUID.randomUUID().toString(), "test1@test.fr");
        SecurityUtils.signIn(org, a1);

        final Account a2 = this.userService.userProvisionning(UUID.randomUUID().toString(), "test2@test.fr");

        this.organizationService.addMember(a1, org, a1, MembershipRole.OWNER);
        this.organizationService.addMember(a1, org, a2, MembershipRole.CONTRIBUTOR);

        Assert.assertEquals(2, this.organizationService.getOrganizationByID(a1, org.getId()).get().getMembers().size());

        this.organizationService.removeMember(a1, org, a2);

        Assert.assertEquals(1, this.organizationService.getOrganizationByID(a1, org.getId()).get().getMembers().size());

    }

}
