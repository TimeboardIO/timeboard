package timeboard.core.internal;

/*-
 * #%L
 * core
 * %%
 * Copyright (C) 2019 Timeboard
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

import timeboard.core.internal.rules.RuleSet;
import timeboard.core.api.*;
import timeboard.core.api.exceptions.BusinessException;
import timeboard.core.internal.rules.ActorIsProjectMember;
import timeboard.core.internal.rules.Rule;
import timeboard.core.internal.rules.TaskHasNoImputation;
import timeboard.core.model.*;
import org.apache.aries.jpa.template.JpaTemplate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceScope;
import org.osgi.service.log.LogService;

import javax.persistence.TypedQuery;
import java.util.*;
import java.util.stream.Collectors;

@Component(
        service = ProjectService.class,
        immediate = true
)
public class ProjectServiceImpl implements ProjectService {

    @Reference
    private LogService logService;

    @Reference
    private UserService userService;

    @Reference(target = "(osgi.unit.name=timeboard-pu)", scope = ReferenceScope.BUNDLE)
    private JpaTemplate jpa;


    @Override
    public void addProjectToProjectCluster(ProjectCluster projectCluster, Project newProject) {
        this.jpa.tx(entityManager -> {
            ProjectCluster c = entityManager.find(ProjectCluster.class, projectCluster.getId());
            Project p = entityManager.find(Project.class, newProject.getId());

            p.getClusters().add(c);

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
    public List<Project> listProjects(User user) {
        return jpa.txExpr(em -> {
            TypedQuery<Project> query = em.createQuery("select p from Project p join fetch p.members m where m.member = :user", Project.class);
            query.setParameter("user", user);
            return query.getResultList();
        });
    }

    @Override
    public Project getProjectByID(User actor, Long projectId) {
        return jpa.txExpr(em -> {
            Project data = em.createQuery("select p from Project p join fetch p.members m where p.id = :projectID and  m.member = :user", Project.class)
                    .setParameter("user", actor)
                    .setParameter("projectID", projectId)
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
    public ProjectDashboard projectDashboard(Project project) {

        return jpa.txExpr(em -> {

            TypedQuery<Object[]> q = em.createQuery("select " +
                    "COALESCE(sum(t.latestRevision.estimateWork),0) as estimateWork, " +
                    "COALESCE(sum(t.latestRevision.remainsToBeDone),0) as remainsToBeDone " +
                    "from Task t " +
                    "where t.project = :project ", Object[].class);

            q.setParameter("project", project);

            Object[] EWandRTBD = q.getSingleResult();

            TypedQuery<Double> effortSpentQuery = em.createQuery("select COALESCE(sum(i.value),0) " +
                    "from Task t left outer join t.imputations i " +
                    "where t.project = :project ", Double.class);

            effortSpentQuery.setParameter("project", project);

            final Double effortSpent = effortSpentQuery.getSingleResult();

            return new ProjectDashboard((Double) EWandRTBD[0], (Double) EWandRTBD[1], effortSpent);

        });
    }


    @Override
    public Project updateProject(Project project, Map<Long, ProjectRole> memberships) throws BusinessException {

        return this.jpa.txExpr(entityManager -> {

            entityManager.merge(project);

            //Update existing membership
            List<Long> membershipToRemove = new ArrayList<>();

            List<Long> currentMembers = project.getMembers().stream().map(pm -> pm.getMember().getId()).collect(Collectors.toList());
            List<Long> membershipToAdd = memberships.keySet().stream().filter(mID -> currentMembers.contains(mID) == false).collect(Collectors.toList());


            project.getMembers().forEach(projectMembership -> {
                if (memberships.containsKey(projectMembership.getMember().getId())) {
                    // Update existing user membership role
                    projectMembership.setRole(memberships.get(projectMembership.getMember().getId()));
                } else {
                    // Store user to removed
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
                projectMembership.setMember(this.userService.findUserByID(aLong));
                entityManager.persist(projectMembership);
                project.getMembers().add(projectMembership);
            });

            entityManager.flush();


            return project;
        });

    }

    @Override
    public void save(ProjectMembership projectMembership) {
        this.jpa.tx(entityManager -> {
            entityManager.persist(projectMembership);
        });
    }

    @Override
    public void deleteProjectClusterByID(Long clusterID) {
        this.jpa.tx(entityManager -> {
            ProjectCluster pc = entityManager.find(ProjectCluster.class, clusterID);
            entityManager.remove(pc);
            entityManager.flush();
        });
    }

    @Override
    public void updateProjectClusters(List<ProjectCluster> updatedProjectCluster, Map<Long, Long> clusterParent) {

        this.jpa.tx(entityManager -> {
            updatedProjectCluster.forEach(projectCluster -> {
                if (clusterParent.containsKey(projectCluster.getId())) {
                    ProjectCluster parentCluster = entityManager.find(ProjectCluster.class, clusterParent.get(projectCluster.getId()));
                    if (parentCluster != null) {
                        projectCluster.setParent(parentCluster);
                    }
                }
                entityManager.merge(projectCluster);
            });
            entityManager.flush();
        });

    }

    @Override
    public List<Task> listProjectTasks(Project project) {
        return this.jpa.txExpr(entityManager -> {
            TypedQuery<Task> q = entityManager.createQuery("select t from Task t where t.project = :project", Task.class);
            q.setParameter("project", project);
            return q.getResultList();
        });
    }

    @Override
    public List<Task> listUserTasks(User user) {
        return this.jpa.txExpr(entityManager -> {
            TypedQuery<Task> q = entityManager.createQuery("select t from Task t where t.latestRevision.assigned = :user", Task.class);
            q.setParameter("user", user);
            return q.getResultList();
        });
    }

    @Override
    public List<TaskType> listTaskType() {
        return this.jpa.txExpr(entityManager -> {
            TypedQuery<TaskType> q = entityManager.createQuery("select tt from TaskType tt", TaskType.class);
            return q.getResultList();
        });
    }

    @Override
    public Task createTask(User actor,
                           Project project,
                           String taskName,
                           String taskComment,
                           Date startDate,
                           Date endDate,
                           double OE,
                           Long taskTypeID,
                           User assignedUser
                           ) {
        return this.jpa.txExpr(entityManager -> {
            Task newTask = new Task();
            newTask.setTaskType(this.findTaskTypeByID(taskTypeID));
            final TaskRevision taskRevision = new TaskRevision(actor, newTask, taskName, taskComment, startDate, endDate, OE, OE, assignedUser);
            newTask.getRevisions().add(taskRevision);
            newTask.setLatestRevision(taskRevision);
            entityManager.persist(newTask);

            entityManager.merge(project);
            newTask.setProject(project);
            entityManager.flush();
            return newTask;
        });
    }

    @Override
    public Task updateTask(User actor, final Task task, TaskRevision rev) {
        return this.jpa.txExpr(entityManager -> {
            final Task taskFromDB = entityManager.find(Task.class, task.getId());
            taskFromDB.setLatestRevision(rev);
            rev.setTask(taskFromDB);
            entityManager.persist(rev);
            entityManager.flush();
            return taskFromDB;
        });
    }

    @Override
    public Task getTask(long id) {
        return this.jpa.txExpr(entityManager -> {
            return entityManager.find(Task.class, id);
        });
    }

    @Override
    public UpdatedTaskResult updateTaskImputation(User actor, Long taskID, Date day, double val) {
        return this.jpa.txExpr(entityManager -> {

            Calendar c = Calendar.getInstance();
            c.setTime(day);
            c.set(Calendar.HOUR_OF_DAY, 2);

            final Task task = entityManager.find(Task.class, taskID);

            TypedQuery<Imputation> q = entityManager.createQuery("select i from Imputation i  where i.task.id = :taskID and i.day = :day", Imputation.class);
            q.setParameter("taskID", taskID);
            q.setParameter("day", c.getTime());

            List<Imputation> existingImputations = q.getResultList();
            if (existingImputations.isEmpty() && val > 0.0 && val <= 1.0) {
                //No imputation for current task and day
                Imputation i = new Imputation();
                i.setDay(c.getTime());
                i.setTask(task);
                i.setValue(val);
                task.updateCurrentRemainsToBeDone(actor,task.getRemainsToBeDone() - val);
                entityManager.persist(i);
            }

            if (!existingImputations.isEmpty()) {
                Imputation i = existingImputations.get(0);

                if (i.getValue() < val) {
                    task.updateCurrentRemainsToBeDone(actor,task.getRemainsToBeDone() - Math.abs(val - i.getValue()));
                }
                if (i.getValue() > val) {
                    task.updateCurrentRemainsToBeDone(actor,task.getRemainsToBeDone() + Math.abs(i.getValue() - val));
                }
                if (val == 0) {
                    entityManager.remove(i);
                } else {
                    i.setValue(val);
                }
            }

            entityManager.flush();

            return new UpdatedTaskResult(task.getProject().getId(), task.getId(), task.getEffortSpent(), task.getRemainsToBeDone(), task.getEstimateWork(), task.getReEstimateWork());
        });
    }

    @Override
    public UpdatedTaskResult updateTaskRTBD(User actor, Long taskID, double rtbd) {
        return this.jpa.txExpr(entityManager -> {
            Task task = entityManager.find(Task.class, taskID);
            task.setRemainsToBeDone(actor, rtbd);
            entityManager.flush();
            return new UpdatedTaskResult(task.getProject().getId(), task.getId(), task.getEffortSpent(), task.getRemainsToBeDone(), task.getEstimateWork(), task.getReEstimateWork());
        });
    }

    @Override
    public TaskType findTaskTypeByID(Long taskTypeID) {
        if(taskTypeID == null){
            return null;
        }
        return this.jpa.txExpr(entityManager -> entityManager.find(TaskType.class, taskTypeID));
    }


    @Override
    public List<TaskRevision> findAllTaskRevisionByTaskID(User actor, Long taskID) {
        return this.jpa.txExpr(entityManager -> {

            TypedQuery<TaskRevision> q = entityManager
                    .createQuery("select t from TaskRevision t left join fetch t.task where " +
                                    "t.task.id = :taskID" +
                                    "group by t.task " +
                                    "having max(t.revisionDate)"
                            , TaskRevision.class);
            q.setParameter("taskID", taskID);
            return q.getResultList();
        });
    }


    @Override
    public List<ProjectTasks> listTasksByProject(User actor, Date ds, Date de) {
        final List<ProjectTasks> projectTaskRevisions = new ArrayList<>();

        this.jpa.tx(entityManager -> {


            TypedQuery<Task> q = entityManager
                    .createQuery("select distinct t from Task t left join fetch t.imputations where " +
                                    "t.latestRevision.endDate >= :ds " +
                                    "and t.latestRevision.startDate <= :de " +
                                    "and t.latestRevision.assigned = :actor "
                            , Task.class);
            q.setParameter("ds", ds);
            q.setParameter("de", de);
            q.setParameter("actor", actor);
            List<Task> tasks = q.getResultList();

            //rebalance task by project
            final Map<Project, List<Task>> rebalanced = new HashMap<>();
            tasks.forEach(task -> {
                if (!rebalanced.containsKey(task.getProject())) {
                    rebalanced.put(task.getProject(), new ArrayList<>());
                }
                rebalanced.get(task.getProject()).add(task);
            });

            rebalanced.forEach((project, ts) -> {
                projectTaskRevisions.add(new ProjectTasks(project, ts));
            });

        });
        return projectTaskRevisions;
    }


    @Override
    public void deleteTaskByID(User actor, long taskID) throws BusinessException {

        RuleSet<Task> ruleSet = new RuleSet<>();
        ruleSet.addRule(new TaskHasNoImputation());
        ruleSet.addRule(new ActorIsProjectMember());

        BusinessException exp = this.jpa.txExpr(entityManager -> {
            Task task = entityManager.find(Task.class, taskID);

            Set<Rule> wrongRules = ruleSet.evaluate(actor, task);
            if (!wrongRules.isEmpty()) {
                return new BusinessException(wrongRules);
            }

            entityManager.remove(task);
            entityManager.flush();
            return null;
        });

        if (exp != null) {
            throw exp;
        }
    }


    @Override
    public ProjectCluster findProjectsClusterByID(long cluster) {
        return this.jpa.txExpr(entityManager -> entityManager.find(ProjectCluster.class, cluster));
    }

    @Override
    public void saveProjectCluster(ProjectCluster projectCluster) {
        this.jpa.tx(entityManager -> {
            entityManager.persist(projectCluster);
        });
    }

    @Override
    public List<TreeNode> computeClustersTree() {
        return this.jpa.txExpr(entityManager -> {
            TreeNode root = new TreeNode(null);
            List<ProjectCluster> projectClusters = entityManager.createQuery("select pc from ProjectCluster pc order by pc.parent", ProjectCluster.class).getResultList();
            projectClusters.forEach(projectCluster -> {
                root.insert(projectCluster);
            });
            return root.getChildren();
        });
    }

    @Override
    public List<ProjectCluster> listProjectClusters() {
        return this.jpa.txExpr(entityManager -> {
            List<ProjectCluster> projectClusters = entityManager.createQuery("select pc from ProjectCluster pc", ProjectCluster.class).getResultList();

            return projectClusters;
        });
    }

}
