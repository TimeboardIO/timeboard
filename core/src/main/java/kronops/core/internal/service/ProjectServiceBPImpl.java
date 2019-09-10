package kronops.core.internal.service;

/*-
 * #%L
 * core
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
import kronops.core.api.dao.ProjectDAO;
import kronops.core.api.dao.ProjectMembershipDAO;
import kronops.core.api.dao.UserDAO;
import kronops.core.api.exceptions.BusinessException;
import kronops.core.model.Project;
import kronops.core.model.ProjectMembership;
import kronops.core.model.ProjectRole;
import kronops.core.model.User;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.log.LogService;

import javax.transaction.TransactionManager;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component(
        service = ProjectServiceBP.class,
        immediate = true
)
public class ProjectServiceBPImpl implements ProjectServiceBP {


    @Reference
    public TransactionManager transactionManager;

    @Reference
    public ProjectDAO projectDAO;

    @Reference
    public ProjectMembershipDAO projectMembershipDAO;

    @Reference
    public LogService logService;

    @Reference
    public UserDAO userDAO;


    @Override
    public Project createProject(User owner, String projectName) throws BusinessException {

        Project newProject = new Project();
        newProject.setName(projectName);
        newProject.setStartDate(new Date());
        this.projectDAO.save(newProject);

        ProjectMembership ownerMembership = new ProjectMembership(newProject, owner, ProjectRole.OWNER);
        this.projectMembershipDAO.save(ownerMembership);


        this.logService.log(LogService.LOG_INFO, "Project " + projectName + " created by user " + owner.getId());

        return newProject;
    }

    @Override
    public Project saveProject(Project project) throws BusinessException {
        return this.projectDAO.save(project);
    }

    @Override
    public List<Project> getProjects() {
        return this.projectDAO.findAll();
    }

    @Override
    public Project getProject(Long projectId) {
        return this.projectDAO.findById(projectId);
    }

    @Override
    public Project deleteProjectByID(Long projectID) {
        final Project project = this.getProject(projectID);
        this.projectDAO.delete(project);
        return project;
    }

    @Override
    public Project updateProject(Project project) throws BusinessException {
        this.projectDAO.update(project);
        return project;
    }


    @Override
    public Project updateProject(Project project, Map<Long, ProjectRole> memberships) throws BusinessException {


        //Update existing membership
        List<Long> membershipToRemove = new ArrayList<>();

        List<Long> currentMembers = project.getMembers().stream().map(pm -> pm.getMember().getId()).collect(Collectors.toList());
        List<Long> membershipToAdd = memberships.keySet().stream().filter(mID -> currentMembers.contains(mID) == false).collect(Collectors.toList());


        project.getMembers().forEach(projectMembership -> {
            if (memberships.containsKey(projectMembership.getMember().getId())) {
                projectMembership.setRole(memberships.get(projectMembership.getMember().getId()));
            } else {
                membershipToRemove.add(projectMembership.getMember().getId());
            }
        });

        //Remove membership
        project.getMembers().removeIf(projectMembership -> membershipToRemove.contains(projectMembership.getMember().getId()));

        //Add new membership
        membershipToAdd.forEach((aLong) -> {
            ProjectMembership projectMembership = new ProjectMembership();
            projectMembership.setProject(project);
            projectMembership.setRole(memberships.get(aLong));
            projectMembership.setMember(this.userDAO.findUserByID(aLong));
            project.getMembers().add(projectMembership);
        });

        return project;

    }

}
