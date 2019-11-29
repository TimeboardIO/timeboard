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

import java.util.*;
import java.util.Calendar;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import org.apache.aries.jpa.template.JpaTemplate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceScope;
import org.osgi.service.log.LogService;
import timeboard.core.api.*;
import timeboard.core.api.exceptions.BusinessException;
import timeboard.core.internal.events.TaskEvent;
import timeboard.core.internal.events.TimeboardEventType;
import timeboard.core.internal.rules.Rule;
import timeboard.core.internal.rules.RuleSet;
import timeboard.core.internal.rules.milestone.ActorIsProjectMemberByMilestone;
import timeboard.core.internal.rules.milestone.MilestoneHasNoTask;
import timeboard.core.internal.rules.project.ActorIsProjectMember;
import timeboard.core.internal.rules.project.ActorIsProjectOwner;
import timeboard.core.internal.rules.task.ActorIsProjectMemberbyTask;
import timeboard.core.internal.rules.task.TaskHasNoImputation;
import timeboard.core.model.*;

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

    public ProjectServiceImpl() {

    }

    public ProjectServiceImpl(JpaTemplate jpa) {
     this.jpa = jpa;
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
    public Project getProjectByIdWithAllMembers(User actor, Long projectId) throws BusinessException {
        Project project =  jpa.txExpr(em -> {
            return em.createQuery("select p from Project p where p.id = :projectId", Project.class)
                    .setParameter("projectId", projectId)
                    .getSingleResult();
        });
        RuleSet<Project> ruleSet = new RuleSet<>();
        ruleSet.addRule(new ActorIsProjectMember());
        Set<Rule> wrongRules = ruleSet.evaluate(actor, project);
        if (!wrongRules.isEmpty()) {
            throw new BusinessException(wrongRules);
        }

        return project;

    }

    @Override
    public Project getProjectByName(User actor, String projectName) throws BusinessException {

        Project project = jpa.txExpr(em -> {
            Project data = em.createQuery("select p from Project p where p.name = :name", Project.class)
                    .setParameter("name", projectName)
                    .getSingleResult();
            if (!data.getMembers().contains(actor)) {
                return null;
            }
            return data;
        });

        RuleSet<Project> ruleSet = new RuleSet<>();
        ruleSet.addRule(new ActorIsProjectMember());
        Set<Rule> wrongRules = ruleSet.evaluate(actor, project);
        if (!wrongRules.isEmpty()) {
            throw new BusinessException(wrongRules);
        }

        return project;
    }

    @Override
    public Project deleteProjectByID(User actor, Project project) throws BusinessException {
        RuleSet<Project> ruleSet = new RuleSet<>();
        ruleSet.addRule(new ActorIsProjectOwner());
        Set<Rule> wrongRules = ruleSet.evaluate(actor, project);
        if (!wrongRules.isEmpty()) {
            throw new BusinessException(wrongRules);
        }

        return jpa.txExpr(em -> {
            em.remove(project);
            em.flush();

            this.logService.log(LogService.LOG_INFO, "Project " + project.getName() + " deleted");
            return project;
        });
    }

    @Override
    public Project updateProject(User actor, Project project) throws BusinessException {
        RuleSet<Project> ruleSet = new RuleSet<>();
        ruleSet.addRule(new ActorIsProjectOwner());
        Set<Rule> wrongRules = ruleSet.evaluate(actor, project);
        if (!wrongRules.isEmpty()) {
            throw new BusinessException(wrongRules);
        }

        return jpa.txExpr(em -> {
            em.merge(project);
            em.flush();

            this.logService.log(LogService.LOG_INFO, "Project " + project.getName() + " updated");
            return project;
        });
    }

    @Override
    public ProjectDashboard projectDashboard(User actor, Project project) throws BusinessException {
        RuleSet<Project> ruleSet = new RuleSet<>();
        ruleSet.addRule(new ActorIsProjectMember());
        Set<Rule> wrongRules = ruleSet.evaluate(actor, project);
        if (!wrongRules.isEmpty()) {
            throw new BusinessException(wrongRules);
        }


        return jpa.txExpr(em -> {

            TypedQuery<Object[]> q = em.createQuery("select "
                    + "COALESCE(sum(t.originalEstimate),0) as originalEstimate, "
                    + "COALESCE(sum(t.effortLeft),0) as effortLeft "
                    + "from Task t "
                    + "where t.project = :project ", Object[].class);

            q.setParameter("project", project);

            Object[] originalEstimateAndEffortLeft = q.getSingleResult();

            TypedQuery<Double> effortSpentQuery = em.createQuery("select COALESCE(sum(i.value),0) "
                    + "from Task t left outer join t.imputations i "
                    + "where t.project = :project ", Double.class);

            effortSpentQuery.setParameter("project", project);

            final Double effortSpent = effortSpentQuery.getSingleResult();

            return new ProjectDashboard(project.getQuotation(), (Double) originalEstimateAndEffortLeft[0], (Double) originalEstimateAndEffortLeft[1], effortSpent);

        });
    }


    @Override
    public Project updateProject(User actor, Project project, Map<Long, ProjectRole> memberships) throws BusinessException {

        RuleSet<Project> ruleSet = new RuleSet<>();
        ruleSet.addRule(new ActorIsProjectOwner());
        Set<Rule> wrongRules = ruleSet.evaluate(actor, project);
        if (!wrongRules.isEmpty()) {
            throw new BusinessException(wrongRules);
        }

        return this.jpa.txExpr(entityManager -> {

            entityManager.merge(project);

            //Update existing membership
            List<Long> membershipToRemove = new ArrayList<>();


            //Update existing membership
            project.getMembers().forEach(projectMembership -> {
                if (memberships.containsKey(projectMembership.getMember().getId())) {
                    // Update existing user membership role
                    projectMembership.setRole(memberships.get(projectMembership.getMember().getId()));
                    entityManager.merge(projectMembership);
                } else {
                    // Store user to removed
                    membershipToRemove.add(projectMembership.getMembershipID());
                }
            });

            //Remove old membership
            membershipToRemove.forEach(idToRemove -> {
                project.getMembers().removeIf(member -> member.getMembershipID() == idToRemove);
                ProjectMembership pmToRemove = entityManager.find(ProjectMembership.class, idToRemove);
                if (pmToRemove != null) {
                    entityManager.remove(pmToRemove);
                }
            });
            entityManager.merge(project);


            //Add new membership
            List<Long> currentMembers = project.getMembers()
                    .stream()
                    .map(pm -> pm.getMember().getId())
                    .collect(Collectors.toList());
            List<Long> membershipToAdd = memberships.keySet()
                    .stream()
                    .filter(membershipId -> currentMembers.contains(membershipId) == false)
                    .collect(Collectors.toList());

            membershipToAdd.forEach((membershipId) -> {
                ProjectMembership projectMembership = new ProjectMembership();
                projectMembership.setProject(project);
                projectMembership.setRole(memberships.get(membershipId));
                projectMembership.setMember(this.userService.findUserByID(membershipId));
                entityManager.persist(projectMembership);
                project.getMembers().add(projectMembership);
            });

            entityManager.flush();

            this.logService.log(LogService.LOG_INFO, "Project " + project.getName() + " updated");
            return project;
        });

    }

    @Override
    public void save(User actor, ProjectMembership projectMembership) throws BusinessException {

        RuleSet<Project> ruleSet = new RuleSet<>();
        ruleSet.addRule(new ActorIsProjectOwner());
        Set<Rule> wrongRules = ruleSet.evaluate(actor, projectMembership.getProject());
        if (!wrongRules.isEmpty()) {
            throw new BusinessException(wrongRules);
        }

        this.jpa.tx(entityManager -> {
            entityManager.persist(projectMembership);
        });
        this.logService.log(LogService.LOG_INFO, "User " + projectMembership.getMember().getName() + " add to project " + projectMembership.getProject().getName());

    }


    /* -- TASKS -- */

    @Override
    public List<Task> listProjectTasks(User actor, Project project) throws BusinessException {

        RuleSet<Project> ruleSet = new RuleSet<>();
        ruleSet.addRule(new ActorIsProjectMember());
        Set<Rule> wrongRules = ruleSet.evaluate(actor, project);
        if (!wrongRules.isEmpty()) {
            throw new BusinessException(wrongRules);
        }

        return this.jpa.txExpr(entityManager -> {
            TypedQuery<Task> q = entityManager.createQuery("select t from Task t where t.project = :project", Task.class);
            q.setParameter("project", project);
            return q.getResultList();
        });
    }

    @Override
    public List<Task> listUserTasks(User user) {
        return this.jpa.txExpr(entityManager -> {
            TypedQuery<Task> q = entityManager.createQuery("select t from Task t where t.assigned = :user", Task.class);
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
                           double originalEstimate,
                           Long taskTypeID,
                           User assignedUser,
                           String origin,
                           String remotePath,
                           String remoteId,
                           Milestone milestone
                           ) {
        Task newTaskDB = this.jpa.txExpr(entityManager -> {
            Task newTask = new Task();
            newTask.setTaskType(this.findTaskTypeByID(taskTypeID));
            newTask.setOrigin(origin);
            newTask.setRemotePath(remotePath);
            newTask.setRemoteId(remoteId);
            newTask.setName(taskName);
            newTask.setComments(taskComment);
            newTask.setStartDate(startDate);
            newTask.setEndDate(endDate);
            newTask.setComments(taskComment);
            newTask.setEffortLeft(originalEstimate);
            newTask.setOriginalEstimate(originalEstimate);
            newTask.setTaskStatus(TaskStatus.PENDING);
            newTask.setAssigned(assignedUser);
            if (milestone != null) {
                entityManager.merge(milestone);
            }
            newTask.setMilestone(milestone);

            entityManager.persist(newTask);
            entityManager.merge(project);
            newTask.setProject(project);
            entityManager.flush();

            TimeboardSubjects.TASK_EVENTS.onNext(new TaskEvent(TimeboardEventType.CREATE, newTask, actor));
            this.logService.log(LogService.LOG_INFO, "Task " + taskName + " created by " + actor.getName() + " in project " + project.getName());

            return newTask;
        });

        return newTaskDB;
    }

    @Override
    public Task updateTask(User actor, final Task task) {
        return this.jpa.txExpr(entityManager -> {
            //TODO check actor permissions
            if (task.getProject().isMember(actor)) {
                entityManager.merge(task);
                entityManager.flush();
            }
            return task;
        });
    }

    @Override
    public void createTasks(final User actor, final List<Task> taskList) {
        this.jpa.tx(entityManager -> {
            for (Task newTask : taskList) {  //TODO create task here
                this.logService.log(LogService.LOG_DEBUG, "User " + actor + " tasks " + newTask.getName() + " on " + newTask.getStartDate());
            }
            this.logService.log(LogService.LOG_INFO, "User " + actor + " created " + taskList.size() + " tasks ");

            entityManager.flush();
        });
    }

    @Override
    public void updateTasks(User actor, List<Task> taskList) {
        this.jpa.tx(entityManager -> {
            for (Task task : taskList) {
                entityManager.merge(task);
            }
            this.logService.log(LogService.LOG_INFO, "User " + actor + " updated " + taskList.size() + " tasks ");
            entityManager.flush();

            taskList.stream().forEach(task -> {
                TimeboardSubjects.TASK_EVENTS.onNext(new TaskEvent(TimeboardEventType.UPDATE, task, actor));
            });

        });
    }

    @Override
    public void deleteTasks(User actor, List<Task> taskList) {
        this.jpa.tx(entityManager -> {
            for (Task task : taskList) {
                entityManager.merge(task);
            }
            this.logService.log(LogService.LOG_WARNING, "User " + actor + " deleted " + taskList.size() + " tasks ");
            entityManager.flush();
        });
    }





    @Override
    public AbstractTask getTaskByID(User user, long id) throws BusinessException {
        AbstractTask task =  this.jpa.txExpr(entityManager -> {
            return entityManager.find(AbstractTask.class, id);
        });
        if (task instanceof Task) {
            RuleSet<Task> ruleSet = new RuleSet<>();
            ruleSet.addRule(new ActorIsProjectMemberbyTask());
            Set<Rule> wrongRules = ruleSet.evaluate(user, (Task) task);
            if (!wrongRules.isEmpty()) {
                throw new BusinessException(wrongRules);
            }
        }

        return task;
    }

    public List<AbstractTask> getTasksByName(User user, String name) {

        final List<AbstractTask> tasks =  new ArrayList<>();
        try {
            this.jpa.tx(entityManager -> {

                TypedQuery<Task> query = entityManager.createQuery("select distinct t from Task t left join fetch t.imputations  where t.name = :name", Task.class);
                query.setParameter("name", name);

                tasks.addAll(query.getResultList());
            });
        } catch (Exception e) {
            // handle JPA Exceptions
        }

        try {
            this.jpa.tx(entityManager -> {
                TypedQuery<DefaultTask> query = entityManager.createQuery("select distinct t from DefaultTask t left join fetch t.imputations where t.name = :name", DefaultTask.class);
                query.setParameter("name", name);
                tasks.addAll(query.getResultList());
            });
        } catch (Exception e) {
            //handle JPA Exceptions
        }

       return tasks;
    }

    private UpdatedTaskResult updateTaskImputation(User actor, Task task, Date day, double val, EntityManager entityManager) {
        Calendar c = Calendar.getInstance();
        c.setTime(day);
        c.set(Calendar.HOUR_OF_DAY, 2);


        // Task is available for imputations if this is a default task (not a project task) or task status is not pending
        boolean taskAvailableForImputations = (task == null || task.getTaskStatus() != TaskStatus.PENDING);

        //DB Query
        TypedQuery<Imputation> q = entityManager.createQuery("select i from Imputation i  where i.task.id = :taskID and i.day = :day", Imputation.class);
        q.setParameter("taskID", task.getId());
        q.setParameter("day", c.getTime());

        // No matching imputations AND new value is correct (0.0 < val <= 1.0) AND task is available for imputations
        List<Imputation> existingImputations = q.getResultList();

        if (taskAvailableForImputations) {
            this.addOrUpdateOrDeleteImputation(existingImputations.isEmpty() ? null : existingImputations.get(0),
                    task, actor, val,c.getTime(), entityManager);
        }

        entityManager.merge(task);
        this.logService.log(LogService.LOG_INFO, "User " + actor.getName() + " updated imputations for task " + task.getId()
                + "(" + day + ") in project " + ((task != null) ? task.getProject().getName() : "default") + " with value " + val);

        if (task != null) { //project task
            return new UpdatedTaskResult(task.getProject().getId(), task.getId(), task.getEffortSpent(), task.getEffortLeft(), task.getOriginalEstimate(), task.getRealEffort());
        } else {
            return new UpdatedTaskResult(0, task.getId(), 0, 0, 0, 0);
        }
    }


    private void addOrUpdateOrDeleteImputation(Imputation i, AbstractTask task, User actor, double val, Date date, EntityManager entityManager) {
        Task projectTask = (task instanceof Task) ? (Task) task : null;

        if (i == null && (val > 0.0) && (val <= 1.0)) { 
            //No imputation for current task and day
            i = new Imputation();
            i.setDay(date);
            i.setTask(task);
            i.setUser(actor);
            i.setValue(val);
            updateEffortLeftFromImputationValue(projectTask, 0, val);
            entityManager.persist(i);
        } else  { // There is an existing imputation for this day and task
            updateEffortLeftFromImputationValue(projectTask, i.getValue(), val);
            i.setValue(val);
            if (val == 0) {  //if value equal to 0 then remove imputation
                entityManager.remove(i);
            } else { // else save new value
                entityManager.persist(i);
            }
        }
    }

    private void updateEffortLeftFromImputationValue(Task projectTask, double currentImputationValue, double newImputationValue) {
        if (projectTask == null) {
            return;
        }
        double currentEL = projectTask.getEffortLeft();
        double newEL = currentEL; // new effort left

        if (currentEL == 0) {
            return;
        }
        double diffValue =  Math.abs(newImputationValue - currentImputationValue);

        if (currentImputationValue < newImputationValue) {
            newEL = currentEL - diffValue;
        }
        if (currentImputationValue > newImputationValue) {
            newEL = currentEL + diffValue;
        }
        projectTask.setEffortLeft(Math.max(newEL, 0));
    }

    @Override
        public UpdatedTaskResult updateTaskImputation(User actor, AbstractTask task, Date day, double val) {
        return this.jpa.txExpr(entityManager -> {
            UpdatedTaskResult updatedTaskResult = this.updateTaskImputation(actor, (Task) task, day, val, entityManager);
            entityManager.flush();
            return updatedTaskResult;
        });
    }


    @Override
    public List<UpdatedTaskResult> updateTaskImputations(User actor, List<Imputation> imputationsList) {
        return this.jpa.txExpr(entityManager -> {
            List<UpdatedTaskResult> result = new ArrayList<>();
            for (Imputation imputation : imputationsList) {
                UpdatedTaskResult updatedTaskResult = this.updateTaskImputation(actor, (Task) imputation.getTask(), imputation.getDay(), imputation.getValue(), entityManager);
                result.add(updatedTaskResult);
            }
            entityManager.flush();
            return result;
        });
    }


    @Override
    public UpdatedTaskResult updateTaskEffortLeft(User actor, Task task, double effortLeft) throws BusinessException {
        RuleSet<Task> ruleSet = new RuleSet<>();
        ruleSet.addRule(new ActorIsProjectMemberbyTask());
        Set<Rule> wrongRules = ruleSet.evaluate(actor, task);
        if (!wrongRules.isEmpty()) {
            throw new BusinessException(wrongRules);
        }
        return this.jpa.txExpr(entityManager -> {
            task.setEffortLeft(effortLeft);
            entityManager.flush();

            this.logService.log(LogService.LOG_INFO, "User " + actor.getName() + " updated effort left for task " + task.getId()
                    + " in project " + task.getProject().getName() + " with value " + effortLeft);

            return new UpdatedTaskResult(task.getProject().getId(), task.getId(), task.getEffortSpent(), task.getEffortLeft(), task.getOriginalEstimate(), task.getRealEffort());
        });
    }

    @Override
    public TaskType findTaskTypeByID(Long taskTypeID) {
        if (taskTypeID == null) {
            return null;
        }
        return this.jpa.txExpr(entityManager -> entityManager.find(TaskType.class, taskTypeID));
    }

    @Override
    public List<TaskRevision> findAllTaskRevisionByTaskID(User actor, Long taskID) {
        return this.jpa.txExpr(entityManager -> {
            TypedQuery<TaskRevision> q = entityManager
                    .createQuery("select t from TaskRevision t left join fetch t.task where "
                                    + "t.task.id = :taskID"
                                    + "group by t.task "
                                    + "having max(t.revisionDate)", TaskRevision.class);
            q.setParameter("taskID", taskID);
            return q.getResultList();
        });
    }


    @Override
    public List<ProjectTasks> listTasksByProject(User actor, Date ds, Date de) {
        final List<ProjectTasks> projectTasks = new ArrayList<>();

        this.jpa.tx(entityManager -> {


            TypedQuery<Task> q = entityManager
                    .createQuery("select distinct t from Task t left join fetch t.imputations where "
                                    + "t.endDate >= :ds "
                                    + "and t.startDate <= :de "
                                    + "and t.assigned = :actor ", Task.class);
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
                projectTasks.add(new ProjectTasks(project, ts));
            });

        });
        return projectTasks;
    }

    @Override
    public List<DefaultTask> listDefaultTasks(Date ds, Date de) {

       return this.jpa.txExpr(entityManager -> {

            TypedQuery<DefaultTask> q = entityManager
                    .createQuery("select distinct t from DefaultTask t left join fetch t.imputations where "
                                    + "t.endDate >= :ds "
                                    + "and t.startDate <= :de ", DefaultTask.class);
            q.setParameter("ds", ds);
            q.setParameter("de", de);
            List<DefaultTask> tasks = q.getResultList();

           return q.getResultList();

        });
    }

    @Override
    public void deleteTaskByID(User actor, long taskID) throws BusinessException {

        RuleSet<Task> ruleSet = new RuleSet<>();
        ruleSet.addRule(new TaskHasNoImputation());
        ruleSet.addRule(new ActorIsProjectMemberbyTask());

        BusinessException exp = this.jpa.txExpr(entityManager -> {
            Task task = entityManager.find(Task.class, taskID);

            Set<Rule> wrongRules = ruleSet.evaluate(actor, task);
            if (!wrongRules.isEmpty()) {
                return new BusinessException(wrongRules);
            }

            entityManager.remove(task);
            entityManager.flush();
            TimeboardSubjects.TASK_EVENTS.onNext(new TaskEvent(TimeboardEventType.DELETE, task, actor));

            return null;
        });

        if (exp != null) {
            throw exp;
        }
        this.logService.log(LogService.LOG_INFO, "Task " + taskID + " deleted by " + actor.getName());

    }

    @Override
    public List<EffortHistory> getEffortSpentByTaskAndPeriod(User actor, Task task, Date startTaskDate, Date endTaskDate) throws BusinessException {
        RuleSet<Task> ruleSet = new RuleSet<>();
        ruleSet.addRule(new ActorIsProjectMemberbyTask());
        Set<Rule> wrongRules = ruleSet.evaluate(actor, task);
        if (!wrongRules.isEmpty()) {
            throw new BusinessException(wrongRules);
        }

        return this.jpa.txExpr(entityManager -> {
            TypedQuery<Object[]> query = (TypedQuery<Object[]>) entityManager.createNativeQuery("select "
                    + "i.day as date, SUM(value) OVER (ORDER BY day) AS sumPreviousValue "
                    + "from Imputation i  where i.task_id = :taskId and i.day >= :startTaskDate and i.day <= :endTaskDate");
            query.setParameter("taskId", task.getId());
            query.setParameter("startTaskDate", startTaskDate);
            query.setParameter("endTaskDate", endTaskDate);

            return query.getResultList()
                    .stream()
                    .map(x -> new EffortHistory((Date) x[0], (Double) x[1]))
                    .collect(Collectors.toList());
        });
    }


    @Override
    public List<EffortHistory> getTaskEffortLeftHistory(User actor, Task task) throws BusinessException {
        RuleSet<Task> ruleSet = new RuleSet<>();
        ruleSet.addRule(new ActorIsProjectMemberbyTask());
        Set<Rule> wrongRules = ruleSet.evaluate(actor, task);
        if (!wrongRules.isEmpty()) {
            throw new BusinessException(wrongRules);
        }

        return this.jpa.txExpr(entityManager -> {
            TypedQuery<Object[]> query = (TypedQuery<Object[]>) entityManager.createNativeQuery("select "
            + "tr.revisionDate as date, tr.effortLeft as effortLeft  "
            + "from TaskRevision tr "
            + "where tr.task_id = :taskId and tr.id IN ( "
                    + "SELECT MAX(trBis.id) "
                    + "FROM TaskRevision trBis "
                    + "GROUP BY trBis.task_id, DATE_FORMAT(trBis.revisionDate, \"%d/%m/%Y\")"
             + ");");

            query.setParameter("taskId", task.getId());

            return query.getResultList()
                    .stream()
                    .map(x -> new EffortHistory((Date) x[0], (Double) x[1]))
                    .collect(Collectors.toList());
        });
    }

    @Override
    public DefaultTask createDefaultTask(DefaultTask task) throws BusinessException {
        try {
            return this.jpa.txExpr(entityManager -> {
                entityManager.persist(task);

                this.logService.log(LogService.LOG_INFO, "Default task " + task.getName() + " is created.");
                return task;
            });
        } catch (Exception e) {
            throw new BusinessException(e);
        }
    }

    @Override
    public DefaultTask updateDefaultTask(DefaultTask task) {

        return jpa.txExpr(em -> {
            em.merge(task);
            em.flush();

            this.logService.log(LogService.LOG_INFO, "Milestone " + task.getName() + " updated");
            return task;
        });


    }

    @Override
    public Map<String, Task> searchExistingTasksFromOrigin(User actor, Project project, String origin, String remotePath) throws BusinessException {

        RuleSet<Project> ruleSet = new RuleSet<>();
        ruleSet.addRule(new ActorIsProjectMember());
        Set<Rule> wrongRules = ruleSet.evaluate(actor, project);
        if (!wrongRules.isEmpty()) {
            throw new BusinessException(wrongRules);
        }

        return this.jpa.txExpr(entityManager -> {
            TypedQuery<Task> q = entityManager.createQuery("select t from Task t where t.project = :project "
                    + "and t.origin = :origin "
                    + "and t.remotePath = :remotePath ", Task.class);
            q.setParameter("project", project);
            q.setParameter("origin", origin);
            q.setParameter("remotePath", remotePath);

            Map<String, Task> map = new HashMap<>();
            q.getResultList().forEach(task -> {
                map.put(task.getRemoteId(), task);
            });
            return map;
        });

    }

    @Override
    public List<Milestone> listProjectMilestones(User actor, Project project) throws BusinessException {
        RuleSet<Project> ruleSet = new RuleSet<>();
        ruleSet.addRule(new ActorIsProjectMember());
        Set<Rule> wrongRules = ruleSet.evaluate(actor, project);
        if (!wrongRules.isEmpty()) {
            throw new BusinessException(wrongRules);
        }

        return this.jpa.txExpr(entityManager -> {
            TypedQuery<Milestone> q = entityManager.createQuery("select m from Milestone m where m.project = :project", Milestone.class);
            q.setParameter("project", project);
            return q.getResultList();
        });
    }

    @Override
    public Milestone getMilestoneById(User user, long id) throws BusinessException {

        Milestone milestone = this.jpa.txExpr(entityManager -> {
            return entityManager.find(Milestone.class, id);
        });
        RuleSet<Milestone> ruleSet = new RuleSet<>();
        ruleSet.addRule(new ActorIsProjectMemberByMilestone());
        Set<Rule> wrongRules = ruleSet.evaluate(user, milestone);
        if (!wrongRules.isEmpty()) {
            throw new BusinessException(wrongRules);
        }
        return milestone;
    }

    @Override
    public Milestone createMilestone(User actor, String name, Date date, MilestoneType type, Map<String, String> attributes, Set<Task> tasks, Project project) throws BusinessException {

        RuleSet<Project> ruleSet = new RuleSet<>();
        ruleSet.addRule(new ActorIsProjectMember());
        Set<Rule> wrongRules = ruleSet.evaluate(actor, project);
        if (!wrongRules.isEmpty()) {
            throw new BusinessException(wrongRules);
        }

        return this.jpa.txExpr(entityManager -> {

            Milestone newMilestone = new Milestone();
            newMilestone.setName(name);
            newMilestone.setDate(date);
            newMilestone.setType(type);
            newMilestone.setAttributes(attributes);
            newMilestone.setTasks(tasks);
            newMilestone.setProject(project);

            entityManager.persist(newMilestone);
            this.logService.log(LogService.LOG_INFO, "Milestone " + newMilestone);

            return newMilestone;

        });
    }

    @Override
    public Milestone updateMilestone(User actor, Milestone milestone) throws BusinessException {
        RuleSet<Milestone> ruleSet = new RuleSet<>();
        ruleSet.addRule(new ActorIsProjectMemberByMilestone());
        Set<Rule> wrongRules = ruleSet.evaluate(actor, milestone);
        if (!wrongRules.isEmpty()) {
            throw new BusinessException(wrongRules);
        }
        return jpa.txExpr(em -> {
            em.merge(milestone);
            em.flush();

            this.logService.log(LogService.LOG_INFO, "Milestone " + milestone.getName() + " updated");
            return milestone;
        });
    }

    @Override
    public void deleteMilestoneByID(User actor, long milestoneID) throws BusinessException {
        RuleSet<Milestone> ruleSet = new RuleSet<>();
        ruleSet.addRule(new ActorIsProjectMemberByMilestone());
        ruleSet.addRule(new MilestoneHasNoTask());

        BusinessException exp = this.jpa.txExpr(entityManager -> {
            Milestone milestone = entityManager.find(Milestone.class, milestoneID);

            Set<Rule> wrongRules = ruleSet.evaluate(actor, milestone);
            if (!wrongRules.isEmpty()) {
                return new BusinessException(wrongRules);
            }

            entityManager.remove(milestone);
            entityManager.flush();
            return null;
        });

        if (exp != null) {
            throw exp;
        }
        this.logService.log(LogService.LOG_INFO, "Milestone " + milestoneID + " deleted by " + actor.getName());
    }

    @Override
    public List<Task> listTasksByMilestone(User actor, Milestone milestone) throws BusinessException {
        RuleSet<Milestone> ruleSet = new RuleSet<>();
        ruleSet.addRule(new ActorIsProjectMemberByMilestone());
        Set<Rule> wrongRules = ruleSet.evaluate(actor, milestone);
        if (!wrongRules.isEmpty()) {
            throw new BusinessException(wrongRules);
        }
        return this.jpa.txExpr(entityManager -> {
            TypedQuery<Task> q = entityManager.createQuery("select t from Task t where t.milestone = :milestone", Task.class);
            q.setParameter("milestone", milestone);
            return q.getResultList();
        });
    }

    @Override
    public Milestone addTasksToMilestone(User actor, Milestone m, List<Task> selectedTaskIds, List<Task> oldTaskIds) throws BusinessException {
        RuleSet<Milestone> ruleSet = new RuleSet<>();
        ruleSet.addRule(new ActorIsProjectMemberByMilestone());
        Set<Rule> wrongRules = ruleSet.evaluate(actor, m);
        if (!wrongRules.isEmpty()) {
            throw new BusinessException(wrongRules);
        }

        return this.jpa.txExpr(em -> {
            oldTaskIds.forEach(tr -> {
                tr.setMilestone(null);
                m.getTasks().removeIf(task -> task.getId() == tr.getId());
             });

            selectedTaskIds.forEach(tr -> {
                tr.setMilestone(m);
                m.getTasks().add(tr);
            });

            return m;
        });
    }

    @Override
    public TaskType createTaskType(String name) {
        return this.jpa.txExpr(entityManager -> {
            TaskType taskType = new TaskType();
            taskType.setTypeName(name);
            entityManager.persist(taskType);
            return taskType;
        });
    }
}
