package kronops.sample;

/*-
 * #%L
 * sample-data
 * %%
 * Copyright (C) 2019 Kronops
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

import kronops.core.api.bp.ProjectServiceBP;
import kronops.core.api.dao.ProjectMembershipDAO;
import kronops.core.api.dao.UserDAO;
import kronops.core.api.exceptions.BusinessException;
import kronops.core.model.Project;
import kronops.core.model.ProjectMembership;
import kronops.core.model.ProjectRole;
import kronops.core.model.User;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.Date;

@Component(
        service = UserLoader.class,
        immediate = true
)
public class UserLoader {

    @Reference
    UserDAO userDAO;

    @Reference
    ProjectServiceBP projectServiceBP;

    @Reference
    ProjectMembershipDAO projectMembershipDAO;


    @Activate
    public void load() throws BusinessException {
        for (int i = 0; i < 10; i++) {
            User u = new User();
            u.setName("kronops" + i);
            u.setPassword("kronops" + i);
            u.setEmail("user" + i + "@kronops.com");
            u.setProjectManager(true);
            u.setImputationFutur(true);
            u.setBeginWorkDate(new Date());
            u.setFirstName("User" + i);
            u.setLogin("kronops" + i);
            u.setAccountCreationTime(new Date());
            try {
                this.userDAO.createUser(u);
            } catch (BusinessException e) {
                e.printStackTrace();
            }
        }


        User u1 = this.userDAO.findUserByLogin("kronops1");
        User u2 = this.userDAO.findUserByLogin("kronops2");

        Project newProject = this.projectServiceBP.createProject(u1, "Test Project");


        ProjectMembership projectMembership = new ProjectMembership(newProject, u2, ProjectRole.CONTRIBUTOR);

        this.projectMembershipDAO.save(projectMembership);

    }


}
