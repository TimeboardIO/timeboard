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
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import timeboard.core.api.exceptions.BusinessException;
import timeboard.core.model.Project;
import timeboard.projects.ProjectSetupController;
import timeboard.projects.ProjectsController;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

public class ProjectFeatureATest extends TimeboardTest {

    @Autowired
    protected ProjectSetupController projectSetupController;
    @Autowired
    private TimeboardWorld world;

    @When("^the user create a project$")
    public void theUserCreateAProject() throws BusinessException {
        projectService.createProject(world.organization, world.account, "BrandNewProject");
    }

    @When("^the user update a project$")
    public void theUserUpdateAProject() throws Exception {
        ProjectSetupController.ProjectConfigForm projectConfigForm = new ProjectSetupController.ProjectConfigForm();
        projectConfigForm.setComments("comments");
        projectConfigForm.setName("BrandNewProject 2 !");
        projectConfigForm.setQuotation(50);
        projectSetupController.updateProjectConfiguration(world.auth, world.lastProject.getId(),
                projectConfigForm, this.attributes);
    }

    @When("^the user archive a project$")
    public void theUserArchiveAProject() throws BusinessException {
        this.projectService.archiveProjectByID(world.account, world.lastProject);
    }

    @Then("^the project has been archived$")
    public void theProjectHasBeenArchived() throws BusinessException {
        Assert.assertFalse(world.lastProject.isEnable());
    }

    @Then("^the user has (\\d+) projects$")
    public void the_user_has_projects(final int arg1) throws Throwable {
        Assert.assertEquals(projectService.listProjects(world.account, world.organization).size(), arg1);
    }

    @Then("^the project has been updated$")
    public void theProjectHasBeenUpdated() throws BusinessException {
        Project updatedProject = projectService.getProjectByID(world.account, world.organization, world.lastProject.getId());
        Assert.assertEquals(updatedProject.getComments(), "comments");
        Assert.assertEquals(updatedProject.getName(), "BrandNewProject 2 !");
        Assert.assertEquals(50, updatedProject.getQuotation(), 0.0);
    }



    private RedirectAttributes attributes = new RedirectAttributes() {
        @Override
        public RedirectAttributes addAttribute(String s, Object o) {
            return null;
        }

        @Override
        public RedirectAttributes addAttribute(Object o) {
            return null;
        }

        @Override
        public RedirectAttributes addAllAttributes(Collection<?> collection) {
            return null;
        }

        @Override
        public Model addAllAttributes(Map<String, ?> map) {
            return null;
        }

        @Override
        public RedirectAttributes mergeAttributes(Map<String, ?> map) {
            return null;
        }

        @Override
        public boolean containsAttribute(String s) {
            return false;
        }

        @Override
        public Map<String, Object> asMap() {
            return null;
        }

        @Override
        public RedirectAttributes addFlashAttribute(String s, Object o) {
            return null;
        }

        @Override
        public RedirectAttributes addFlashAttribute(Object o) {
            return null;
        }

        @Override
        public Map<String, ?> getFlashAttributes() {
            return null;
        }
    };
}
