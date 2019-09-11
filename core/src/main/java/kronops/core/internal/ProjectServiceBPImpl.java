package kronops.core.internal;

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

import kronops.core.api.ProjectServiceBP;
import kronops.core.api.TreeNode;
import kronops.core.api.UserServiceBP;
import kronops.core.api.exceptions.BusinessException;
import kronops.core.model.*;
import org.apache.aries.jpa.template.JpaTemplate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceScope;
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
    private TransactionManager transactionManager;

    @Reference
    private LogService logService;

    @Reference
    private UserServiceBP userServiceBP;

    @Reference(target = "(osgi.unit.name=kronops-pu)", scope = ReferenceScope.BUNDLE)
    private JpaTemplate jpa;


    @Override
    public void addProjectToProjectCluster(ProjectCluster projectCluster, Project newProject) {
        this.jpa.tx(entityManager -> {
            ProjectCluster c = entityManager.find(ProjectCluster.class, projectCluster.getId());
            Project p = entityManager.find(Project.class, newProject.getId());

            p.setCluster(c);

            entityManager.flush();
        });
    }

    @Override
    public Project createProject(User owner, String projectName) throws BusinessException {

        return this.jpa.txExpr(entityManager -> {

            Project newProject = new Project();
            newProject.setName(projectName);
            newProject.setStartDate(new Date());
            entityManager.persist(newProject);

            ProjectMembership ownerMembership = new ProjectMembership(newProject, owner, ProjectRole.OWNER);
            entityManager.persist(ownerMembership);

            this.logService.log(LogService.LOG_INFO, "Project " + projectName + " created by user " + owner.getId());

            return newProject;

        });

    }


    @Override
    public List<Project> listProjects() {
        return jpa.txExpr(em -> {
            List<Project> data = em.createQuery("select p from Project p", Project.class)
                    .getResultList();
            return data;
        });
    }

    @Override
    public Project getProject(Long projectId) {

        return jpa.txExpr(em -> {
            Project data = em.createQuery("select p from Project p where p.id = :id", Project.class)
                    .setParameter("id", projectId)
                    .getSingleResult();
            return data;
        });

    }

    @Override
    public Project deleteProjectByID(Long projectID) {
        return jpa.txExpr(em -> {
            Project p = em.find(Project.class, projectID);
            em.remove(p);
            em.flush();
            return p;
        });
    }

    @Override
    public Project updateProject(Project project) throws BusinessException {
        return jpa.txExpr(em -> {
            em.merge(project);
            em.flush();
            return project;
        });
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
            projectMembership.setMember(this.userServiceBP.findUserByID(aLong));
            project.getMembers().add(projectMembership);
        });

        return project;

    }

    @Override
    public void save(ProjectMembership projectMembership) {
        this.jpa.tx(entityManager -> {
            entityManager.persist(projectMembership);
        });
    }

    @Override
    public void saveProjectCluster(ProjectCluster projectCluster) {
        this.jpa.tx(entityManager -> {
            entityManager.persist(projectCluster);
        });
    }

    @Override
    public TreeNode listProjectCluster() {
        return this.jpa.txExpr(entityManager -> {
            TreeNode root = new TreeNode(null);
            List<ProjectCluster> projectClusters = entityManager.createQuery("select pc from ProjectCluster pc", ProjectCluster.class).getResultList();
             projectClusters.forEach(projectCluster -> {
                 root.insert(projectCluster);
             });
            return root;
        });
    }

}
