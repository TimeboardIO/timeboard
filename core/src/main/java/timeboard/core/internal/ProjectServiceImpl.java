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
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import timeboard.core.api.*;
import timeboard.core.api.exceptions.BusinessException;
import timeboard.core.internal.rules.Rule;
import timeboard.core.internal.rules.RuleSet;
import timeboard.core.internal.rules.batch.ActorIsProjectMemberByBatch;
import timeboard.core.internal.rules.batch.BatchHasNoTask;
import timeboard.core.internal.rules.project.ActorIsProjectMember;
import timeboard.core.internal.rules.project.ActorIsProjectOwner;
import timeboard.core.internal.rules.task.ActorIsProjectMemberbyTask;
import timeboard.core.internal.rules.task.TaskHasNoImputation;
import timeboard.core.model.*;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

@Component
@Transactional
public class ProjectServiceImpl implements ProjectService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProjectServiceImpl.class);
    private static final String VACATION_TASK_NAME = "Congés";

    @Autowired
    private UserService userService;

    @Autowired
    private TimesheetService timesheetService;

    @Autowired
    private EntityManager em;

    private DefaultTask vacationTask;

    public static String generateRandomColor(Color mix) {
        Random random = new Random();
        int red = random.nextInt(256);
        int green = random.nextInt(256);
        int blue = random.nextInt(256);

        // mix the color
        if (mix != null) {
            red = (red + mix.getRed()) / 2;
            green = (green + mix.getGreen()) / 2;
            blue = (blue + mix.getBlue()) / 2;
        }

        Color color = new Color(red, green, blue);
        return String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
    }

    @Transactional
    @EventListener()
    protected void init(ContextRefreshedEvent ctx) {
        //TODO change when vacation model totally implemented

        AbstractTask taskFromDB = this.getTasksByName(VACATION_TASK_NAME);
        if (taskFromDB == null) {
            DefaultTask task = new DefaultTask();
            task.setName(VACATION_TASK_NAME);
            task.setStartDate(new Date());
            task.setEndDate(new Date(new Date().getTime() + 999999999999L));
            task.setOrigin("timeboard");
            try {
                em.persist(task);
                vacationTask = task;
            } catch (Exception e) {
                LOGGER.error("Error in vacation task singleton instantiation.");
            }
        } else {
            vacationTask = (DefaultTask) taskFromDB;
        }
    }

    @Override
    @Transactional
    @PreAuthorize("@bpe.checkProjectByUserLimit(#owner)")
    @PostAuthorize("returnObject.organizationID == authentication.currentOrganization")
    public Project createProject(Account owner, String projectName) throws BusinessException {
        Account ownerAccount = this.em.find(Account.class, owner.getId());
        Project newProject = new Project();
        newProject.setName(projectName);
        newProject.setStartDate(new Date());
        newProject.getAttributes()
                .put(Project.PROJECT_COLOR_ATTR, new ProjectAttributValue(ProjectServiceImpl.generateRandomColor(Color.WHITE)));
        em.persist(newProject);

        em.flush();
        ProjectMembership ownerMembership = new ProjectMembership(newProject, ownerAccount, MembershipRole.OWNER);
        em.persist(ownerMembership);

        LOGGER.info("Project " + projectName + " created by user " + owner.getId());
        return newProject;
    }

    @Override
    public List<Project> listProjects(Account account) {
        TypedQuery<Project> query = em.createNamedQuery("ListUserProjects", Project.class);
        query.setParameter("user", account);
        return query.getResultList();
    }

    @Override
        public Project getProjectByID(Account actor, Long projectId) {
        Project data = em.createQuery("select p from Project p join fetch p.members m " +
                "where p.id = :projectID and  m.member = :user", Project.class)
                .setParameter("user", actor)
                .setParameter("projectID", projectId)
                .getSingleResult();
        return data;
    }

    @Override
    public Project getProjectByIdWithAllMembers(Account actor, Long projectId) throws BusinessException {
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
    public Project getProjectByName(Account actor, String projectName) throws BusinessException {

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
    public Project archiveProjectByID(Account actor, Project project) throws BusinessException {
        RuleSet<Project> ruleSet = new RuleSet<>();
        ruleSet.addRule(new ActorIsProjectOwner());
        Set<Rule> wrongRules = ruleSet.evaluate(actor, project);
        if (!wrongRules.isEmpty()) {
            throw new BusinessException(wrongRules);
        }
        em.merge(project);
        project.setEnable(false);
        em.flush();

        LOGGER.info("Project " + project.getName() + " archived by " + actor.getName());
        return project;
    }

    @Override
    public Project updateProject(Account actor, Project project) throws BusinessException {
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
    public ProjectDashboard projectDashboard(Account actor, Project project) throws BusinessException {
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

        return new ProjectDashboard(project.getQuotation(),
                (Double) originalEstimateAndEffortLeft[0],
                (Double) originalEstimateAndEffortLeft[1], effortSpent, new Date());

    }


    @Override
    public Project updateProject(Account actor, Project project, Map<Long, MembershipRole> memberships) throws BusinessException {

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
    public void save(Account actor, ProjectMembership projectMembership) throws BusinessException {

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
    public List<Task> listProjectTasks(Account actor, Project project) throws BusinessException {

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
    public List<Task> listUserTasks(Account account) {
        TypedQuery<Task> q = em.createQuery("select t from Task t where t.assigned = :user", Task.class);
        q.setParameter("user", account);
        return q.getResultList();

    }

    @Override
    public List<TaskType> listTaskType() {
        TypedQuery<TaskType> q = em.createQuery("select tt from TaskType tt where tt.enable = true", TaskType.class);
        return q.getResultList();
    }


    @Override
    @Transactional
    @PreAuthorize("@bpe.checkTaskByProjectLimit(#actor, #project)")
    public Task createTask(Account actor,
                           Project project,
                           String taskName,
                           String taskComment,
                           Date startDate,
                           Date endDate,
                           double originalEstimate,
                           Long taskTypeID,
                           Account assignedAccount,
                           String origin,
                           String remotePath,
                           String remoteId,
                           TaskStatus taskStatus,
                           Batch batch
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
        newTask.setTaskStatus(taskStatus);
        newTask.setAssigned(assignedAccount);
        newTask.setOrganizationID(project.getId());
        if (batch != null) {
            em.merge(batch);
        }
        newTask.addBatch(batch);

        em.persist(newTask);
        em.merge(project);
        newTask.setProject(project);
        em.flush();

        LOGGER.info("Task " + taskName + " created by " + actor.getName() + " in project " + project.getName());

        return newTask;
    }

    @Override
    public Task updateTask(Account actor, final Task task) {
        //TODO check actor permissions
        if (task.getProject().isMember(actor)) {
            em.merge(task);
            em.flush();
        }
        return task;
    }

    @Override
    public void createTasks(final Account actor, final List<Task> taskList) {
        for (Task newTask : taskList) {  //TODO create task here
            LOGGER.info("User " + actor + " tasks " + newTask.getName() + " on " + newTask.getStartDate());
        }
        LOGGER.info("User " + actor + " created " + taskList.size() + " tasks ");

        em.flush();

    }

    @Override
    public void updateTasks(Account actor, List<Task> taskList) {
        for (Task task : taskList) {
            em.merge(task);
        }
        LOGGER.info("User " + actor + " updated " + taskList.size() + " tasks ");
        em.flush();

        /*taskList.stream().forEach(task -> {
            TimeboardSubjects.TASK_EVENTS.onNext(new TaskEvent(TimeboardEventType.UPDATE, task, actor));
        });*/

    }

    @Override
    public void deleteTasks(Account actor, List<Task> taskList) {
        for (Task task : taskList) {
            em.merge(task);
        }
        LOGGER.info("User " + actor + " deleted " + taskList.size() + " tasks ");
        em.flush();

    }


    @Override
    public AbstractTask getTaskByID(Account account, long id) throws BusinessException {
        AbstractTask task = em.find(AbstractTask.class, id);
        if (task instanceof Task) {
            RuleSet<Task> ruleSet = new RuleSet<>();
            ruleSet.addRule(new ActorIsProjectMemberbyTask());
            Set<Rule> wrongRules = ruleSet.evaluate(account, (Task) task);
            if (!wrongRules.isEmpty()) {
                throw new BusinessException(wrongRules);
            }
        }

        return task;
    }

    private AbstractTask getTasksByName(String name) {

        final List<AbstractTask> tasks = new ArrayList<>();
        try {

            TypedQuery<Task> query = em.createQuery("select distinct t from Task t left join fetch t.imputations  where t.name = :name", Task.class);
            query.setParameter("name", name);

            tasks.addAll(query.getResultList());

        } catch (Exception e) {
            // handle JPA Exceptions
        }

        try {
            TypedQuery<DefaultTask> query = em.createQuery("select distinct t " +
                    "from DefaultTask t left join fetch t.imputations where t.name = :name", DefaultTask.class);

            query.setParameter("name", name);
            tasks.addAll(query.getResultList());
        } catch (Exception e) {
            //handle JPA Exceptions
        }

        if (tasks.isEmpty()) {
            return null;
        }

        return tasks.get(0);

    }

    public List<AbstractTask> getTasksByName(Account account, String name) {

        final List<AbstractTask> tasks = new ArrayList<>();
        try {

            TypedQuery<Task> query = em.createQuery("select distinct t " +
                    "from Task t left join fetch t.imputations  " +
                    "where t.name = :name", Task.class);

            query.setParameter("name", name);

            tasks.addAll(query.getResultList());

        } catch (Exception e) {
            // handle JPA Exceptions
        }

        try {
            TypedQuery<DefaultTask> query = em.createQuery("select distinct t " +
                    "from DefaultTask t left join fetch t.imputations " +
                    "where t.name = :name", DefaultTask.class);

            query.setParameter("name", name);
            tasks.addAll(query.getResultList());
        } catch (Exception e) {
            //handle JPA Exceptions
        }

        return tasks;
    }
    @Override
    public Optional<Task> getTaskByRemoteID(Account actor, String id) {
        Task task = null;
        try {
            final TypedQuery<Task> query = this.em.createQuery("select t from Task t where t.remoteId = :remoteID", Task.class);
            query.setParameter("remoteID", id);
            task = query.getSingleResult();
        }catch (Exception e){
        }
        return Optional.ofNullable(task);
    }

    @Override
    public List<UpdatedTaskResult> updateTaskImputations(Account actor, List<Imputation> imputationsList) {
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
    public UpdatedTaskResult updateTaskImputation(Account actor, AbstractTask task, Date day, double val) throws BusinessException {
        Calendar c = Calendar.getInstance();
        c.setTime(day);
        c.set(Calendar.HOUR_OF_DAY, 2);

        if (task instanceof Task) {
            return this.updateProjectTaskImputation(actor, (Task) task, day, val, c);
        } else {
            return this.updateDefaultTaskImputation(actor, (DefaultTask) task, day, val, c);
        }
    }

    private UpdatedTaskResult updateProjectTaskImputation(Account actor,
                                                          Task task,
                                                          Date day,
                                                          double val,
                                                          Calendar calendar) throws BusinessException {

        Task projectTask = (Task) this.getTaskByID(actor, task.getId());


        if (projectTask.getTaskStatus() != TaskStatus.PENDING) {
            Imputation existingImputation = this.getImputationByDayByTask(em, calendar.getTime(), projectTask);
            double oldValue = existingImputation != null ? existingImputation.getValue() : 0;

            Imputation updatedImputation = this.actionOnImputation(existingImputation, projectTask, actor, val, calendar.getTime(), em);
            Task updatedTask = em.find(Task.class, projectTask.getId());
            double newEffortLeft = this.updateEffortLeftFromImputationValue(projectTask.getEffortLeft(), oldValue, val);
            updatedTask.setEffortLeft(newEffortLeft);

            LOGGER.info("User " + actor.getName()
                    + " updated imputations for task "
                    + projectTask.getId() + " (" + day + ") in project "
                    + ((projectTask != null) ? projectTask.getProject().getName() : "default") + " with value " + val);

            em.merge(updatedTask);
            em.flush();

            return new UpdatedTaskResult(updatedTask.getProject().getId(),
                    updatedTask.getId(), updatedTask.getEffortSpent(),
                    updatedTask.getEffortLeft(), updatedTask.getOriginalEstimate(),
                    updatedTask.getRealEffort());
        }
        return null;
    }

    private UpdatedTaskResult updateDefaultTaskImputation(Account actor,
                                                          DefaultTask task,
                                                          Date day, double val, Calendar calendar) throws BusinessException {

        DefaultTask defaultTask = (DefaultTask) this.getTaskByID(actor, task.getId());

        // No matching imputations AND new value is correct (0.0 < val <= 1.0) AND task is available for imputations
        Imputation existingImputation = this.getImputationByDayByTask(em, calendar.getTime(), defaultTask);
        this.actionOnImputation(existingImputation, defaultTask, actor, val, calendar.getTime(), em);

        em.flush();
        LOGGER.info("User " + actor.getName() + " updated imputations for default task "
                + defaultTask.getId() + "(" + day + ") in project: default with value " + val);

        return new UpdatedTaskResult(0, defaultTask.getId(), 0, 0, 0, 0);

    }

    private Imputation getImputationByDayByTask(EntityManager entityManager, Date day, AbstractTask task) {
        TypedQuery<Imputation> q = entityManager.createQuery("select i from Imputation i  " +
                "where i.task.id = :taskID and i.day = :day", Imputation.class);
        q.setParameter("taskID", task.getId());
        q.setParameter("day", day);
        return q.getResultList().stream().findFirst().orElse(null);
    }


    private Imputation actionOnImputation(Imputation imputation,
                                          AbstractTask task,
                                          Account actor,
                                          double val,
                                          Date date,
                                          EntityManager entityManager) {

        if (imputation == null) {
            //No imputation for current task and day
            imputation = new Imputation();
            imputation.setDay(date);
            imputation.setTask(task);
            imputation.setAccount(actor);
            imputation.setValue(val);
            entityManager.persist(imputation);
        } else {
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
        double diffValue = Math.abs(newImputationValue - currentImputationValue);

        if (currentImputationValue < newImputationValue) {
            newEL = currentEffortLeft - diffValue;
        }
        if (currentImputationValue > newImputationValue) {
            newEL = currentEffortLeft + diffValue;
        }

        return Math.max(newEL, 0);
    }


    @Override
    public UpdatedTaskResult updateTaskEffortLeft(Account actor, Task task, double effortLeft) throws BusinessException {
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

        return new UpdatedTaskResult(task.getProject().getId(),
                task.getId(), task.getEffortSpent(), task.getEffortLeft(),
                task.getOriginalEstimate(), task.getRealEffort());
    }

    @Override
    public TaskType findTaskTypeByID(Long taskTypeID) {
        if (taskTypeID == null) {
            return null;
        }
        return em.find(TaskType.class, taskTypeID);
    }



    @Override
    public List<ProjectTasks> listTasksByProject(Account actor, Date ds, Date de) {
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

        rebalanced.forEach((project, ts) -> {
            projectTasks.add(new ProjectTasks(project, ts));
        });


        return projectTasks;
    }

    @Override
    public List<DefaultTask> listDefaultTasks(Date ds, Date de) {
        TypedQuery<DefaultTask> q = em
                .createQuery("select distinct t from DefaultTask t left join fetch t.imputations where "
                        + "t.endDate > :ds "
                        + "and t.startDate <= :de "
                        + "and t.startDate < t.endDate", DefaultTask.class);
        q.setParameter("ds", ds);
        q.setParameter("de", de);
        List<DefaultTask> tasks = q.getResultList();

        return q.getResultList();

    }

    @Override
    public void deleteTaskByID(Account actor, long taskID) throws BusinessException {

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
        /*TimeboardSubjects.TASK_EVENTS.onNext(new TaskEvent(TimeboardEventType.DELETE, task, actor));*/


        LOGGER.info("Task " + taskID + " deleted by " + actor.getName());

    }

    @Override
    public List<ValueHistory> getEffortSpentByTaskAndPeriod(Account actor,
                                                            Task task,
                                                            Date startTaskDate,
                                                            Date endTaskDate) throws BusinessException {

        RuleSet<Task> ruleSet = new RuleSet<>();
        ruleSet.addRule(new ActorIsProjectMemberbyTask());
        Set<Rule> wrongRules = ruleSet.evaluate(actor, task);
        if (!wrongRules.isEmpty()) {
            throw new BusinessException(wrongRules);
        }

        TypedQuery<Object[]> query =
                (TypedQuery<Object[]>) em.createNativeQuery("select "
                + "i.day as date, SUM(value) " +
                        "OVER (ORDER BY day) AS sumPreviousValue "
                + "from Imputation i  where i.task_id = :taskId " +
                        "and i.day >= :startTaskDate and i.day <= :endTaskDate");
        query.setParameter("taskId", task.getId());
        query.setParameter("startTaskDate", startTaskDate);
        query.setParameter("endTaskDate", endTaskDate);

        return query.getResultList()
                .stream()
                .map(x -> new ValueHistory((Date) x[0], (Double) x[1]))
                .collect(Collectors.toList());
    }


    @Override
    public List<ValueHistory> getTaskEffortLeftHistory(Account actor, Task task) throws BusinessException {
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
                .map(x -> new ValueHistory((Date) x[0], (Double) x[1]))
                .collect(Collectors.toList());
    }

    @Override
    public DefaultTask createDefaultTask(Account actor, String name) throws BusinessException {
        try {
            DefaultTask task = new DefaultTask();
            task.setStartDate(new Date());
            Calendar c = Calendar.getInstance();
            c.set(9999, Calendar.DECEMBER, 31);
            task.setEndDate(c.getTime());
            task.setOrigin(actor.getScreenName() + "/" + System.nanoTime());
            task.setName(name);
            em.persist(task);
            em.flush();
            LOGGER.info("Default task " + task.getName() + " is created.");
            return task;
        } catch (Exception e) {
            throw new BusinessException(e);
        }
    }


    @Override
    public void disableDefaultTaskByID(Account actor, long taskID) throws BusinessException {

        DefaultTask task = em.find(DefaultTask.class, taskID);
        task.setEndDate(new Date());

        em.merge(task);
        em.flush();

        LOGGER.info("Default Task " + taskID + " deleted by " + actor.getName());
    }

    @Override
    public Map<String, Task> searchExistingTasksFromOrigin(Account actor,
                                                           Project project,
                                                           String origin,
                                                           String remotePath) throws BusinessException {

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
    public List<Batch> listProjectBatches(Account actor, Project project) throws BusinessException {
        RuleSet<Project> ruleSet = new RuleSet<>();
        ruleSet.addRule(new ActorIsProjectMember());
        Set<Rule> wrongRules = ruleSet.evaluate(actor, project);
        if (!wrongRules.isEmpty()) {
            throw new BusinessException(wrongRules);
        }

        TypedQuery<Batch> q = em.createQuery("select b from Batch b where b.project = :project", Batch.class);
        q.setParameter("project", project);
        return q.getResultList();
    }

    @Override
    public Batch getBatchById(Account account, long id) throws BusinessException {

        Batch batch = em.find(Batch.class, id);
        RuleSet<Batch> ruleSet = new RuleSet<>();
        ruleSet.addRule(new ActorIsProjectMemberByBatch());
        Set<Rule> wrongRules = ruleSet.evaluate(account, batch);
        if (!wrongRules.isEmpty()) {
            throw new BusinessException(wrongRules);
        }
        return batch;
    }

    @Override
    public Batch createBatch(Account actor,
                             String name, Date date, BatchType type,
                             Map<String, String> attributes,
                             Set<Task> tasks, Project project) throws BusinessException {

        RuleSet<Project> ruleSet = new RuleSet<>();
        ruleSet.addRule(new ActorIsProjectMember());
        Set<Rule> wrongRules = ruleSet.evaluate(actor, project);
        if (!wrongRules.isEmpty()) {
            throw new BusinessException(wrongRules);
        }


        Batch newBatch = new Batch();
        newBatch.setName(name);
        newBatch.setDate(date);
        newBatch.setType(type);
        newBatch.setAttributes(attributes);
        newBatch.setTasks(tasks);
        newBatch.setProject(project);

        em.persist(newBatch);
        LOGGER.info("Batch " + newBatch);

        return newBatch;

    }

    @Override
    public Batch updateBatch(Account actor, Batch batch) throws BusinessException {
        RuleSet<Batch> ruleSet = new RuleSet<>();
        ruleSet.addRule(new ActorIsProjectMemberByBatch());
        Set<Rule> wrongRules = ruleSet.evaluate(actor, batch);
        if (!wrongRules.isEmpty()) {
            throw new BusinessException(wrongRules);
        }
        em.merge(batch);
        em.flush();

        LOGGER.info("Batch " + batch.getName() + " updated");
        return batch;
    }

    @Override
    public void deleteBatchByID(Account actor, long batchID) throws BusinessException {
        RuleSet<Batch> ruleSet = new RuleSet<>();
        ruleSet.addRule(new ActorIsProjectMemberByBatch());
        ruleSet.addRule(new BatchHasNoTask());

        Batch batch = em.find(Batch.class, batchID);

        Set<Rule> wrongRules = ruleSet.evaluate(actor, batch);
        if (!wrongRules.isEmpty()) {
            throw new BusinessException(wrongRules);
        }

        em.remove(batch);
        em.flush();

        LOGGER.info("Batch " + batchID + " deleted by " + actor.getName());
    }

    @Override
    public List<Task> listTasksByBatch(Account actor, Batch batch) throws BusinessException {
        RuleSet<Batch> ruleSet = new RuleSet<>();
        ruleSet.addRule(new ActorIsProjectMemberByBatch());
        Set<Rule> wrongRules = ruleSet.evaluate(actor, batch);
        if (!wrongRules.isEmpty()) {
            throw new BusinessException(wrongRules);
        }
        TypedQuery<Task> q = em.createQuery("select distinct t from Task t join t.batches b where b = :batch", Task.class);
        q.setParameter("batch", batch);
        return q.getResultList();
    }


    @Override
    public List<Batch> getBatchList(Account actor, Project project, BatchType batchType) throws BusinessException {
        RuleSet<Project> ruleSet = new RuleSet<>();
        ruleSet.addRule(new ActorIsProjectMember());
        Set<Rule> wrongRules = ruleSet.evaluate(actor, project);
        if (!wrongRules.isEmpty()) {
            throw new BusinessException(wrongRules);
        }
        TypedQuery<Batch> q = em.createQuery("select distinct b from Batch b join b.tasks t where t.project = :project and b.type = :type", Batch.class);
        q.setParameter("project", project);
        q.setParameter("type", batchType);
        return q.getResultList();
    }

    @Override
    public Batch addTasksToBatch(Account actor, Batch b, List<Task> selectedTaskIds, List<Task> oldTaskIds) throws BusinessException {
        RuleSet<Batch> ruleSet = new RuleSet<>();
        ruleSet.addRule(new ActorIsProjectMemberByBatch());
        Set<Rule> wrongRules = ruleSet.evaluate(actor, b);
        if (!wrongRules.isEmpty()) {
            throw new BusinessException(wrongRules);
        }

        oldTaskIds.forEach(tr -> {
            tr.setBatches(null);
            b.getTasks().removeIf(task -> task.getId() == tr.getId());
        });

        selectedTaskIds.forEach(tr -> {
            tr.addBatch(b);
            b.getTasks().add(tr);
        });

        return b;
    }

    @Override
    public TaskType createTaskType(Account actor, String name) {
        TaskType taskType = new TaskType();
        taskType.setTypeName(name);
        em.persist(taskType);
        em.flush();
        LOGGER.info("User " + actor.getScreenName() + " create task type " + name);

        return taskType;
    }



    @Override
    public void disableTaskType(Account actor, TaskType type) {

        type.setEnable(false);

        em.merge(type);
        em.flush();
        LOGGER.info("User " + actor.getScreenName() + " disable task type " + type.getTypeName());

    }


    @Override
    public boolean isProjectOwner(Account account, Project project) {
        return (new ActorIsProjectOwner()).isSatisfied(account, project);
    }




    @Override
    public TASData generateTasData(Account user, Project project, int month, int year) {

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
        Map<Integer, String> comments = new HashMap<>();
        Map<Integer, Double> otherProjectImputations = new HashMap<>();
        List<Integer> dayMonthNums = new ArrayList<>();
        List<String> dayMonthNames = new ArrayList<>();

        // rolling days in month
        for (int i = start.get(Calendar.DAY_OF_MONTH); start.before(end); start.add(Calendar.DATE, 1), i = start.get(Calendar.DAY_OF_MONTH)) {

            dayMonthNums.add(i);
            dayMonthNames.add(new SimpleDateFormat("EEEE", Locale.ENGLISH).format(start.getTime()).toLowerCase());

            Double vacationI = vacationImputations.get(i);
            Double projectI = projectImputations.get(i);
            double otherProjectI;

            vacationI = (vacationI == null) ? 0.0 : vacationI; //handling no imputation
            projectI = (projectI == null) ? 0.0 : projectI;

            otherProjectI = 1 - projectI - vacationI; //other project imputation

            otherProjectImputations.put(i, otherProjectI);
            vacationImputations.put(i, vacationI);
            projectImputations.put(i, projectI);
            comments.put(i, "");
        }

        data.setDayMonthNames(dayMonthNames);
        data.setDayMonthNums(dayMonthNums);

        data.setOffDays(vacationImputations);
        data.setWorkedDays(projectImputations);
        data.setOtherDays(otherProjectImputations);
        data.setComments(comments);

        return data;
    }

}
