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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
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

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
import java.util.Calendar;
import java.util.*;
import java.util.stream.Collectors;

@Component
@Transactional
public class ProjectServiceImpl implements ProjectService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProjectServiceImpl.class);
    private static final String VACATION_TASK_NAME = "Congés";

    @Autowired
    private UserService userService;

    @Reference(target = "(osgi.unit.name=timeboard-pu)", scope = ReferenceScope.BUNDLE)
    private JpaTemplate jpa;

    @Autowired
    private EntityManager em;


    public ProjectServiceImpl(JpaTemplate jpa) {
     this.jpa = jpa;
    }

    @Override
    @Transactional
    public Project createProject(User owner, String projectName) throws BusinessException {
        em.merge(owner);
        Project newProject = new Project();
        newProject.setName(projectName);
        newProject.setStartDate(new Date());
        em.persist(newProject);

        ProjectMembership ownerMembership = new ProjectMembership(newProject, owner, ProjectRole.OWNER);
        em.persist(ownerMembership);

        LOGGER.info("Project " + projectName + " created by user " + owner.getId());
        return newProject;
    }


    @Override
    public List<Project> listProjects(User user) {
        TypedQuery<Project> query = em.createQuery("select p from Project p join fetch p.members m where m.member = :user", Project.class);
        query.setParameter("user", user);
        return query.getResultList();
    }

    @Override
    public Project getProjectByID(User actor, Long projectId) {
        Project data = em.createQuery("select p from Project p join fetch p.members m where p.id = :projectID and  m.member = :user", Project.class)
                .setParameter("user", actor)
                .setParameter("projectID", projectId)
                .getSingleResult();
        return data;
    }

    @Override
    public Project getProjectByIdWithAllMembers(User actor, Long projectId) throws BusinessException {
        Project project = em.createQuery("select p from Project p where p.id = :projectId", Project.class)
                .setParameter("projectId", projectId)
                .getSingleResult();
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

        Project data = em.createQuery("select p from Project p where p.name = :name", Project.class)
                .setParameter("name", projectName)
                .getSingleResult();
        if (!data.getMembers().contains(actor)) {
            return null;
        }

        RuleSet<Project> ruleSet = new RuleSet<>();
        ruleSet.addRule(new ActorIsProjectMember());
        Set<Rule> wrongRules = ruleSet.evaluate(actor, data);
        if (!wrongRules.isEmpty()) {
            throw new BusinessException(wrongRules);
        }

        return data;
    }

    @Override
    public Project deleteProjectByID(User actor, Project project) throws BusinessException {
        RuleSet<Project> ruleSet = new RuleSet<>();
        ruleSet.addRule(new ActorIsProjectOwner());
        Set<Rule> wrongRules = ruleSet.evaluate(actor, project);
        if (!wrongRules.isEmpty()) {
            throw new BusinessException(wrongRules);
        }

        em.remove(project);
        em.flush();

        LOGGER.info("Project " + project.getName() + " deleted");
        return project;
    }

    @Override
    public Project updateProject(User actor, Project project) throws BusinessException {
        RuleSet<Project> ruleSet = new RuleSet<>();
        ruleSet.addRule(new ActorIsProjectOwner());
        Set<Rule> wrongRules = ruleSet.evaluate(actor, project);
        if (!wrongRules.isEmpty()) {
            throw new BusinessException(wrongRules);
        }

        em.merge(project);
        em.flush();

        LOGGER.info("Project " + project.getName() + " updated");
        return project;
    }

    @Override
    public ProjectDashboard projectDashboard(User actor, Project project) throws BusinessException {
        RuleSet<Project> ruleSet = new RuleSet<>();
        ruleSet.addRule(new ActorIsProjectMember());
        Set<Rule> wrongRules = ruleSet.evaluate(actor, project);
        if (!wrongRules.isEmpty()) {
            throw new BusinessException(wrongRules);
        }


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

    }


    @Override
    public Project updateProject(User actor, Project project, Map<Long, ProjectRole> memberships) throws BusinessException {

        RuleSet<Project> ruleSet = new RuleSet<>();
        ruleSet.addRule(new ActorIsProjectOwner());
        Set<Rule> wrongRules = ruleSet.evaluate(actor, project);
        if (!wrongRules.isEmpty()) {
            throw new BusinessException(wrongRules);
        }

        em.merge(project);

        //Update existing membership
        List<Long> membershipToRemove = new ArrayList<>();


        //Update existing membership
        project.getMembers().forEach(projectMembership -> {
            if (memberships.containsKey(projectMembership.getMember().getId())) {
                // Update existing user membership role
                projectMembership.setRole(memberships.get(projectMembership.getMember().getId()));
                em.merge(projectMembership);
            } else {
                // Store user to removed
                membershipToRemove.add(projectMembership.getMembershipID());
            }
        });

        //Remove old membership
        membershipToRemove.forEach(idToRemove -> {
            project.getMembers().removeIf(member -> member.getMembershipID() == idToRemove);
            ProjectMembership pmToRemove = em.find(ProjectMembership.class, idToRemove);
            if (pmToRemove != null) {
                em.remove(pmToRemove);
            }
        });
        em.merge(project);


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
            em.persist(projectMembership);
            project.getMembers().add(projectMembership);
        });

        em.flush();

        LOGGER.info("Project " + project.getName() + " updated");
        return project;
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
    public void save(User actor, ProjectMembership projectMembership) throws BusinessException {

        RuleSet<Project> ruleSet = new RuleSet<>();
        ruleSet.addRule(new ActorIsProjectOwner());
        Set<Rule> wrongRules = ruleSet.evaluate(actor, projectMembership.getProject());
        if (!wrongRules.isEmpty()) {
            throw new BusinessException(wrongRules);
        }

        em.persist(projectMembership);

        LOGGER.info("User " + projectMembership.getMember().getName() + " add to project " + projectMembership.getProject().getName());

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

        TypedQuery<Task> q = em.createQuery("select t from Task t where t.project = :project", Task.class);
        q.setParameter("project", project);
        return q.getResultList();

    }

    @Override
    public List<Task> listUserTasks(User user) {
        TypedQuery<Task> q = em.createQuery("select t from Task t where t.assigned = :user", Task.class);
        q.setParameter("user", user);
        return q.getResultList();

    }

    @Override
    public List<TaskType> listTaskType() {
        TypedQuery<TaskType> q = em.createQuery("select tt from TaskType tt", TaskType.class);
        return q.getResultList();
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
            em.merge(milestone);
        }
        newTask.setMilestone(milestone);

        em.persist(newTask);
        em.merge(project);
        newTask.setProject(project);
        em.flush();

        TimeboardSubjects.TASK_EVENTS.onNext(new TaskEvent(TimeboardEventType.CREATE, newTask, actor));
        LOGGER.info("Task " + taskName + " created by " + actor.getName() + " in project " + project.getName());

        return newTask;
    }

    @Override
    public Task updateTask(User actor, final Task task) {
        //TODO check actor permissions
        if (task.getProject().isMember(actor)) {
            em.merge(task);
            em.flush();
        }
        return task;
    }

    @Override
    public void createTasks(final User actor, final List<Task> taskList) {
        for (Task newTask : taskList) {  //TODO create task here
           LOGGER.info("User " + actor + " tasks " + newTask.getName() + " on " + newTask.getStartDate());
        }
        LOGGER.info("User " + actor + " created " + taskList.size() + " tasks ");

        em.flush();

    }

    @Override
    public void updateTasks(User actor, List<Task> taskList) {
        for (Task task : taskList) {
            em.merge(task);
        }
        LOGGER.info("User " + actor + " updated " + taskList.size() + " tasks ");
        em.flush();

        taskList.stream().forEach(task -> {
            TimeboardSubjects.TASK_EVENTS.onNext(new TaskEvent(TimeboardEventType.UPDATE, task, actor));
        });

    }

    @Override
    public void deleteTasks(User actor, List<Task> taskList) {
        for (Task task : taskList) {
            em.merge(task);
        }
        LOGGER.info("User " + actor + " deleted " + taskList.size() + " tasks ");
        em.flush();
    }


    @Override
    public AbstractTask getTaskByID(User user, long id) throws BusinessException {
        AbstractTask task = em.find(AbstractTask.class, id);
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

    private AbstractTask getTasksByName(String name) {

        final AbstractTask[] task = new AbstractTask[1];
        try {
            this.jpa.tx(entityManager -> {

                TypedQuery<Task> query = entityManager.createQuery("select distinct t from Task t left join fetch t.imputations  where t.name = :name", Task.class);
                query.setParameter("name", name);

                task[0] = query.getSingleResult();
            });
        } catch (Exception e) {
            // handle JPA Exceptions
        }

        try {
            this.jpa.tx(entityManager -> {
                TypedQuery<DefaultTask> query = entityManager.createQuery("select distinct t from DefaultTask t left join fetch t.imputations where t.name = :name", DefaultTask.class);
                query.setParameter("name", name);
                task[0] = query.getSingleResult();
            });
        } catch (Exception e) {
            //handle JPA Exceptions
        }

        return task[0];

    }

    public List<AbstractTask> getTasksByName(User user, String name) {

        final List<AbstractTask> tasks = new ArrayList<>();
        try {

            TypedQuery<Task> query = em.createQuery("select distinct t from Task t left join fetch t.imputations  where t.name = :name", Task.class);
            query.setParameter("name", name);

            tasks.addAll(query.getResultList());

        } catch (Exception e) {
            // handle JPA Exceptions
        }

        try {
            TypedQuery<DefaultTask> query = em.createQuery("select distinct t from DefaultTask t left join fetch t.imputations where t.name = :name", DefaultTask.class);
            query.setParameter("name", name);
            tasks.addAll(query.getResultList());
        } catch (Exception e) {
            //handle JPA Exceptions
        }

        return tasks;
    }

    @Override
    public List<UpdatedTaskResult> updateTaskImputations(User actor, List<Imputation> imputationsList) {
        List<UpdatedTaskResult> result = new ArrayList<>();
        for (Imputation imputation : imputationsList) {
            UpdatedTaskResult updatedTaskResult = null;
            try {
                updatedTaskResult = this.updateTaskImputation(actor, (Task) imputation.getTask(), imputation.getDay(), imputation.getValue());
            } catch (BusinessException e) {
                e.printStackTrace();
            }
            result.add(updatedTaskResult);
        }
        em.flush();
        return result;
    }

    @Override
    public UpdatedTaskResult updateTaskImputation(User actor, AbstractTask task, Date day, double val) throws BusinessException {
        Calendar c = Calendar.getInstance();
        c.setTime(day);
        c.set(Calendar.HOUR_OF_DAY, 2);

        if (task instanceof Task) {
            return this.updateProjectTaskImputation(actor, (Task) task, day, val, c);
        } else {
            return this.updateDefaultTaskImputation(actor, (DefaultTask) task, day, val, c);
        }
    }

    private UpdatedTaskResult updateProjectTaskImputation(User actor, Task task, Date day, double val, Calendar calendar) throws BusinessException {
        Task projectTask = (Task) this.getTaskByID(actor, task.getId());


            if(projectTask.getTaskStatus() != TaskStatus.PENDING) {
                Imputation existingImputation = this.getImputationByDayByTask(em, calendar.getTime(), projectTask);
                double oldValue = existingImputation != null ? existingImputation.getValue() : 0;

                Imputation updatedImputation = this.actionOnImputation(existingImputation, projectTask, actor, val, calendar.getTime(), em);
                Task updatedTask = em.find(Task.class, projectTask.getId());
                double newEffortLeft = this.updateEffortLeftFromImputationValue(projectTask.getEffortLeft(), oldValue, val);
                updatedTask.setEffortLeft(newEffortLeft);
                LOGGER.info("User " + actor.getName() + " updated imputations for task " + projectTask.getId() + " (" + day + ") in project " + ((projectTask != null) ? projectTask.getProject().getName() : "default") + " with value " + val);

                em.merge(updatedTask);
                em.flush();

                return new UpdatedTaskResult(updatedTask.getProject().getId(), updatedTask.getId(), updatedTask.getEffortSpent(), updatedTask.getEffortLeft(), updatedTask.getOriginalEstimate(), updatedTask.getRealEffort());
            }
            return null;
    }

    private UpdatedTaskResult updateDefaultTaskImputation(User actor, DefaultTask task, Date day, double val, Calendar calendar) throws BusinessException {
        DefaultTask defaultTask = (DefaultTask) this.getTaskByID(actor, task.getId());

        // No matching imputations AND new value is correct (0.0 < val <= 1.0) AND task is available for imputations
        Imputation existingImputation = this.getImputationByDayByTask(em, calendar.getTime(), defaultTask);
        this.actionOnImputation(existingImputation, defaultTask, actor, val, calendar.getTime(), em);

        em.flush();
        LOGGER.info("User " + actor.getName() + " updated imputations for default task " + defaultTask.getId() + "(" + day + ") in project: default with value " + val);

        return new UpdatedTaskResult(0, defaultTask.getId(), 0, 0, 0, 0);

    }

    private Imputation getImputationByDayByTask(EntityManager entityManager, Date day, AbstractTask task) {
        TypedQuery<Imputation> q = entityManager.createQuery("select i from Imputation i  where i.task.id = :taskID and i.day = :day", Imputation.class);
        q.setParameter("taskID", task.getId());
        q.setParameter("day", day);
        return q.getResultList().stream().findFirst().orElse(null);
    }


    private Imputation actionOnImputation(Imputation imputation, AbstractTask task, User actor, double val, Date date, EntityManager entityManager) {

        if (imputation == null) {
            //No imputation for current task and day
            imputation = new Imputation();
            imputation.setDay(date);
            imputation.setTask(task);
            imputation.setUser(actor);
            imputation.setValue(val);
            entityManager.persist(imputation);
        } else  {
            // There is an existing imputation for this day and task
            imputation.setValue(val);
            if (val == 0) {
                //if value equal to 0 then remove imputation
                entityManager.remove(imputation);
            } else {
                // else save new value
                entityManager.persist(imputation);
            }
        }
        entityManager.flush();

        return imputation;
    }

    private double updateEffortLeftFromImputationValue(double currentEffortLeft, double currentImputationValue, double newImputationValue) {
        double newEL = currentEffortLeft; // new effort left
        double diffValue =  Math.abs(newImputationValue - currentImputationValue);

        if (currentImputationValue < newImputationValue) {
            newEL = currentEffortLeft - diffValue;
        }
        if (currentImputationValue > newImputationValue) {
            newEL = currentEffortLeft + diffValue;
        }

        return Math.max(newEL, 0);
    }


    @Override
    public UpdatedTaskResult updateTaskEffortLeft(User actor, Task task, double effortLeft) throws BusinessException {
        RuleSet<Task> ruleSet = new RuleSet<>();
        ruleSet.addRule(new ActorIsProjectMemberbyTask());
        Set<Rule> wrongRules = ruleSet.evaluate(actor, task);
        if (!wrongRules.isEmpty()) {
            throw new BusinessException(wrongRules);
        }
        task.setEffortLeft(effortLeft);
        em.merge(task);
        em.flush();

        LOGGER.info("User " + actor.getName() + " updated effort left for task " + task.getId()
                + " in project " + task.getProject().getName() + " with value " + effortLeft);

        return new UpdatedTaskResult(task.getProject().getId(), task.getId(), task.getEffortSpent(), task.getEffortLeft(), task.getOriginalEstimate(), task.getRealEffort());
    }

    @Override
    public TaskType findTaskTypeByID(Long taskTypeID) {
        if (taskTypeID == null) {
            return null;
        }
        return em.find(TaskType.class, taskTypeID);
    }

    @Override
    public List<TaskRevision> findAllTaskRevisionByTaskID(User actor, Long taskID) {
        TypedQuery<TaskRevision> q = em
                .createQuery("select t from TaskRevision t left join fetch t.task where "
                        + "t.task.id = :taskID"
                        + "group by t.task "
                        + "having max(t.revisionDate)", TaskRevision.class);
        q.setParameter("taskID", taskID);
        return q.getResultList();
    }


    @Override
    public List<ProjectTasks> listTasksByProject(User actor, Date ds, Date de) {
        final List<ProjectTasks> projectTasks = new ArrayList<>();


        TypedQuery<Task> q = em
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


        TypedQuery<DefaultTask> q = em
                .createQuery("select distinct t from DefaultTask t left join fetch t.imputations where "
                        + "t.endDate >= :ds "
                        + "and t.startDate <= :de ", DefaultTask.class);
        q.setParameter("ds", ds);
        q.setParameter("de", de);
        List<DefaultTask> tasks = q.getResultList();

        return q.getResultList();

    }

    @Override
    public void deleteTaskByID(User actor, long taskID) throws BusinessException {

        RuleSet<Task> ruleSet = new RuleSet<>();
        ruleSet.addRule(new TaskHasNoImputation());
        ruleSet.addRule(new ActorIsProjectMemberbyTask());

        Task task = em.find(Task.class, taskID);

        Set<Rule> wrongRules = ruleSet.evaluate(actor, task);
        if (!wrongRules.isEmpty()) {
            throw new BusinessException(wrongRules);
        }

        em.remove(task);
        em.flush();
        TimeboardSubjects.TASK_EVENTS.onNext(new TaskEvent(TimeboardEventType.DELETE, task, actor));


        LOGGER.info("Task " + taskID + " deleted by " + actor.getName());

    }

    @Override
    public List<EffortHistory> getEffortSpentByTaskAndPeriod(User actor, Task task, Date startTaskDate, Date endTaskDate) throws BusinessException {
        RuleSet<Task> ruleSet = new RuleSet<>();
        ruleSet.addRule(new ActorIsProjectMemberbyTask());
        Set<Rule> wrongRules = ruleSet.evaluate(actor, task);
        if (!wrongRules.isEmpty()) {
            throw new BusinessException(wrongRules);
        }

             TypedQuery<Object[]> query = (TypedQuery<Object[]>) em.createNativeQuery("select "
                    + "i.day as date, SUM(value) OVER (ORDER BY day) AS sumPreviousValue "
                    + "from Imputation i  where i.task_id = :taskId and i.day >= :startTaskDate and i.day <= :endTaskDate");
            query.setParameter("taskId", task.getId());
            query.setParameter("startTaskDate", startTaskDate);
            query.setParameter("endTaskDate", endTaskDate);

            return query.getResultList()
                    .stream()
                    .map(x -> new EffortHistory((Date) x[0], (Double) x[1]))
                    .collect(Collectors.toList());
     }


    @Override
    public List<EffortHistory> getTaskEffortLeftHistory(User actor, Task task) throws BusinessException {
        RuleSet<Task> ruleSet = new RuleSet<>();
        ruleSet.addRule(new ActorIsProjectMemberbyTask());
        Set<Rule> wrongRules = ruleSet.evaluate(actor, task);
        if (!wrongRules.isEmpty()) {
            throw new BusinessException(wrongRules);
        }

             TypedQuery<Object[]> query = (TypedQuery<Object[]>) em.createNativeQuery("select "
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
     }

    @Override
    public DefaultTask createDefaultTask(DefaultTask task) throws BusinessException {
        try {
            em.persist(task);
                LOGGER.info("Default task " + task.getName() + " is created.");
                return task;
         } catch (Exception e) {
            throw new BusinessException(e);
        }
    }

    @Override
    public DefaultTask updateDefaultTask(DefaultTask task) {

             em.merge(task);
            em.flush();

            LOGGER.info("Milestone " + task.getName() + " updated");
            return task;

    }

    @Override
    public Map<String, Task> searchExistingTasksFromOrigin(User actor, Project project, String origin, String remotePath) throws BusinessException {

        RuleSet<Project> ruleSet = new RuleSet<>();
        ruleSet.addRule(new ActorIsProjectMember());
        Set<Rule> wrongRules = ruleSet.evaluate(actor, project);
        if (!wrongRules.isEmpty()) {
            throw new BusinessException(wrongRules);
        }

        TypedQuery<Task> q = em.createQuery("select t from Task t where t.project = :project "
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

    }

    @Override
    public List<Milestone> listProjectMilestones(User actor, Project project) throws BusinessException {
        RuleSet<Project> ruleSet = new RuleSet<>();
        ruleSet.addRule(new ActorIsProjectMember());
        Set<Rule> wrongRules = ruleSet.evaluate(actor, project);
        if (!wrongRules.isEmpty()) {
            throw new BusinessException(wrongRules);
        }

        TypedQuery<Milestone> q = em.createQuery("select m from Milestone m where m.project = :project", Milestone.class);
        q.setParameter("project", project);
        return q.getResultList();
    }

    @Override
    public Milestone getMilestoneById(User user, long id) throws BusinessException {

        Milestone milestone = em.find(Milestone.class, id);
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


        Milestone newMilestone = new Milestone();
        newMilestone.setName(name);
        newMilestone.setDate(date);
        newMilestone.setType(type);
        newMilestone.setAttributes(attributes);
        newMilestone.setTasks(tasks);
        newMilestone.setProject(project);

        em.persist(newMilestone);
        LOGGER.info("Milestone " + newMilestone);

        return newMilestone;

    }

    @Override
    public Milestone updateMilestone(User actor, Milestone milestone) throws BusinessException {
        RuleSet<Milestone> ruleSet = new RuleSet<>();
        ruleSet.addRule(new ActorIsProjectMemberByMilestone());
        Set<Rule> wrongRules = ruleSet.evaluate(actor, milestone);
        if (!wrongRules.isEmpty()) {
            throw new BusinessException(wrongRules);
        }
        em.merge(milestone);
        em.flush();

        LOGGER.info("Milestone " + milestone.getName() + " updated");
        return milestone;
    }

    @Override
    public void deleteMilestoneByID(User actor, long milestoneID) throws BusinessException {
        RuleSet<Milestone> ruleSet = new RuleSet<>();
        ruleSet.addRule(new ActorIsProjectMemberByMilestone());
        ruleSet.addRule(new MilestoneHasNoTask());

        Milestone milestone = em.find(Milestone.class, milestoneID);

        Set<Rule> wrongRules = ruleSet.evaluate(actor, milestone);
        if (!wrongRules.isEmpty()) {
            throw new BusinessException(wrongRules);
        }

        em.remove(milestone);
        em.flush();

        LOGGER.info("Milestone " + milestoneID + " deleted by " + actor.getName());
    }

    @Override
    public List<Task> listTasksByMilestone(User actor, Milestone milestone) throws BusinessException {
        RuleSet<Milestone> ruleSet = new RuleSet<>();
        ruleSet.addRule(new ActorIsProjectMemberByMilestone());
        Set<Rule> wrongRules = ruleSet.evaluate(actor, milestone);
        if (!wrongRules.isEmpty()) {
            throw new BusinessException(wrongRules);
        }
        TypedQuery<Task> q = em.createQuery("select t from Task t where t.milestone = :milestone", Task.class);
        q.setParameter("milestone", milestone);
        return q.getResultList();
    }

    @Override
    public Milestone addTasksToMilestone(User actor, Milestone m, List<Task> selectedTaskIds, List<Task> oldTaskIds) throws BusinessException {
        RuleSet<Milestone> ruleSet = new RuleSet<>();
        ruleSet.addRule(new ActorIsProjectMemberByMilestone());
        Set<Rule> wrongRules = ruleSet.evaluate(actor, m);
        if (!wrongRules.isEmpty()) {
            throw new BusinessException(wrongRules);
        }

        oldTaskIds.forEach(tr -> {
            tr.setMilestone(null);
            m.getTasks().removeIf(task -> task.getId() == tr.getId());
        });

        selectedTaskIds.forEach(tr -> {
            tr.setMilestone(m);
            m.getTasks().add(tr);
        });

        return m;
    }

    @Override
    public TaskType createTaskType(String name) {
        TaskType taskType = new TaskType();
        taskType.setTypeName(name);
        em.persist(taskType);
        return taskType;
    }


    @Override
    public TASData generateTasData(User user, Project project, int month, int year) {

        TASData data = new TASData();

        data.setBusinessCode(project.getName());
        data.setMatriculeID(user.getEmail());
        data.setFirstName(user.getFirstName());
        data.setName(user.getName());
        data.setMonth(month);
        data.setYear(year);

        Calendar start = Calendar.getInstance();
        start.set(year, month - 1, 1, 2, 0);
        Calendar end = Calendar.getInstance();
        end.set(year, month, 1, 2, 0);

        Map<Integer, Double> vacationImputations = timesheetService.getTaskImputationForDate(start.getTime(), end.getTime(), user, vacationTask);
        Map<Integer, Double> projectImputations = timesheetService.getProjectImputationSumForDate(start.getTime(), end.getTime(), user, project);
        Map<Integer, Double> otherProjectImputations = new HashMap<>();
        List<Integer> dayMonthNums = new ArrayList<>();
        List<String> dayMonthNames = new ArrayList<>();

        // rolling days in month
        for (int i = start.get(Calendar.DAY_OF_MONTH); start.before(end); start.add(Calendar.DATE, 1), i = start.get(Calendar.DAY_OF_MONTH)) {

            dayMonthNums.add(i);
            dayMonthNames.add(new SimpleDateFormat("EEEE", Locale.ENGLISH).format(start).toLowerCase());

            Double vacationI = vacationImputations.get(i);
            Double projectI = projectImputations.get(i);
            double otherProjectI;

            vacationI = (vacationI == null) ? 0.0 : vacationI; //handling no imputation
            projectI = (projectI == null) ? 0.0 : projectI;

            otherProjectI = 1 - projectI - vacationI; //other project imputation

            otherProjectImputations.put(i, otherProjectI);
            vacationImputations.put(i, vacationI);
            projectImputations.put(i, projectI);
        }

        data.setDayMonthNames(dayMonthNames);
        data.setDayMonthNums(dayMonthNums);

        data.setOffDays(vacationImputations);
        data.setWorkedDays(projectImputations);
        data.setOtherDays(otherProjectImputations);

        return data;
    }
}
