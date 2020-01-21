package timeboard.webapp;

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

import java.lang.reflect.Member;
import java.util.Collections;
import java.util.Optional;
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
