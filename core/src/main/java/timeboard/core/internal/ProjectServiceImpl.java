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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import timeboard.core.api.*;
import timeboard.core.api.events.TaskEvent;
import timeboard.core.api.events.TimeboardEventType;
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
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

@Component("projectService")
@Transactional
public class ProjectServiceImpl implements ProjectService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProjectServiceImpl.class);

    @Value("${timeboard.tasks.default.vacation}")
    private String defaultVacationTaskName;

    @Autowired
    private TimesheetService timesheetService;

    @Autowired
    private OrganizationService organizationService;

    @Autowired
    private EntityManager em;

    public static String generateRandomColor(final Color mix) {
        final Random random = new Random();
        int red = random.nextInt(256);
        int green = random.nextInt(256);
        int blue = random.nextInt(256);

        // mix the color
        if (mix != null) {
            red = (red + mix.getRed()) / 2;
            green = (green + mix.getGreen()) / 2;
            blue = (blue + mix.getBlue()) / 2;
        }

        final Color color = new Color(red, green, blue);
        return String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
    }

    @Override
    @Transactional
    @PreAuthorize("hasPermission(null,'" + PROJECT_CREATE + "')")
    public Project createProject(final Organization orgID, final Account owner, final String projectName) {
        final Account ownerAccount = this.em.find(Account.class, owner.getId());
        final Project newProject = new Project();
        newProject.setName(projectName);
        newProject.setStartDate(new Date());
        newProject.setOrganizationID(orgID.getId());
        newProject.getAttributes()
                .put(Project.PROJECT_COLOR_ATTR, new ProjectAttributValue(ProjectServiceImpl.generateRandomColor(Color.WHITE)));
        em.persist(newProject);

        em.flush();
        final ProjectMembership ownerMembership = new ProjectMembership(newProject, ownerAccount, MembershipRole.OWNER);
        em.persist(ownerMembership);

        LOGGER.info("Project " + projectName + " created by user " + owner.getId());
        return newProject;
    }

    @Override
    @PreAuthorize("hasPermission(null,'" + PROJECT_LIST + "')")
    @Cacheable(value = "accountProjectsCache", key = "#candidate.getId()")
    public List<Project> listProjects(final Account candidate, final Organization org) {
        final TypedQuery<Project> query = em.createNamedQuery(Project.PROJECT_LIST, Project.class);
        query.setParameter("user", candidate);
        query.setParameter("orgID", org.getId());
        return query.getResultList();
    }

    @Override
    @PreAuthorize("hasPermission(null,'" + PROJECT_COUNT + "')")
    public double countAccountProjectMemberships(final Organization org, final Account candidate) {
        final TypedQuery<Project> query = em.createNamedQuery(Project.PROJECT_LIST, Project.class);
        query.setParameter("user", candidate);
        query.setParameter("orgID", org.getId());
        return query.getResultList().size();
    }

    @Override
    @PostAuthorize("hasPermission(returnObject,'" + PROJECT_VIEW + "')")
    public Project getProjectByID(final Account actor, final Organization org, final Long projectId) {

        Project data = null;
        try {
            data = em.createNamedQuery(Project.PROJECT_GET_BY_ID, Project.class)
                    .setParameter("user", actor)
                    .setParameter("projectID", projectId)
                    .setParameter("orgID", org.getId())
                    .getSingleResult();
        } catch (NoResultException nre) {
            data = null;
        }
        return data;
    }


    @Override
    public List<Account> findOwnersOfAnyUserProject(Account user) {
        final TypedQuery<Account> q = em.createQuery("SELECT DISTINCT m2.member " +
                "FROM ProjectMembership m1 JOIN ProjectMembership m2 " +
                "ON m1.project = m2.project WHERE m1.member = :user AND m2.role = :role", Account.class);
        q.setParameter("user", user);
        q.setParameter("role", MembershipRole.OWNER);
        return q.getResultList();

    }

    @Override
    public boolean isOwnerOfAnyUserProject(Account owner, Account user) {
        final TypedQuery<Account> q = em.createQuery("SELECT DISTINCT m2.member " +
                "FROM ProjectMembership m1 JOIN ProjectMembership m2 " +
                "ON m1.project = m2.project " +
                "WHERE m1.member = :user " +
                "AND m2.member = :owner " +
                "AND m2.role = :role", Account.class);
        q.setParameter("user", user);
        q.setParameter("owner", owner);
        q.setParameter("role", MembershipRole.OWNER);


        try {
            final Account singleResult = q.getSingleResult();
            if (singleResult != null) {
                return true;
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }


    @Override
    @PreAuthorize("hasPermission(#project,'" + PROJECT_ARCHIVE + "')")
    @CacheEvict(value = "accountProjectsCache", key = "#actor.getId()")
    public Project archiveProjectByID(final Account actor, final Project project) throws BusinessException {
        final RuleSet<Project> ruleSet = new RuleSet<>();
        ruleSet.addRule(new ActorIsProjectOwner());
        final Set<Rule> wrongRules = ruleSet.evaluate(actor, project);
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
    @PreAuthorize("hasPermission(#project,'" + PROJECT_SETUP + "')")
    @CacheEvict(value = "accountProjectsCache", key = "#actor.getId()")
    public Project updateProject(final Account actor, final Project project) throws BusinessException {
        final RuleSet<Project> ruleSet = new RuleSet<>();
        ruleSet.addRule(new ActorIsProjectOwner());
        final Set<Rule> wrongRules = ruleSet.evaluate(actor, project);
        if (!wrongRules.isEmpty()) {
            throw new BusinessException(wrongRules);
        }

        em.merge(project);
        em.flush();

        LOGGER.info("Project " + project.getName() + " updated");
        return project;
    }

    @Override
    public ProjectDashboard projectDashboard(final Account actor, final Project project) throws BusinessException {
        final RuleSet<Project> ruleSet = new RuleSet<>();
        ruleSet.addRule(new ActorIsProjectMember());
        final Set<Rule> wrongRules = ruleSet.evaluate(actor, project);
        if (!wrongRules.isEmpty()) {
            throw new BusinessException(wrongRules);
        }


        final TypedQuery<Object[]> q = em.createQuery("select "
                + "COALESCE(sum(t.originalEstimate),0) as originalEstimate, "
                + "COALESCE(sum(t.effortLeft),0) as effortLeft "
                + "from Task t "
                + "where t.project = :project ", Object[].class);

        q.setParameter("project", project);

        final Object[] originalEstimateAndEffortLeft = q.getSingleResult();

        final TypedQuery<Double> effortSpentQuery = em.createQuery("select COALESCE(sum(i.value),0) "
                + "from Task t left outer join t.imputations i "
                + "where t.project = :project ", Double.class);

        effortSpentQuery.setParameter("project", project);

        final Double effortSpent = effortSpentQuery.getSingleResult();

        return new ProjectDashboard(project.getQuotation(),
                (Double) originalEstimateAndEffortLeft[0],
                (Double) originalEstimateAndEffortLeft[1], effortSpent, new Date());

    }


    /* -- TASKS -- */

    @Override
    public void save(final Account actor, final ProjectMembership projectMembership) throws BusinessException {

        final RuleSet<Project> ruleSet = new RuleSet<>();
        ruleSet.addRule(new ActorIsProjectOwner());
        final Set<Rule> wrongRules = ruleSet.evaluate(actor, projectMembership.getProject());
        if (!wrongRules.isEmpty()) {
            throw new BusinessException(wrongRules);
        }

        em.persist(projectMembership);

        LOGGER.info("User " + projectMembership.getMember().getName() + " add to project " + projectMembership.getProject().getName());

    }

    @Override
    @PreAuthorize("hasPermission(#project,'" + TASK_LIST + "')")
    public List<Task> listProjectTasks(final Account actor, final Project project) throws BusinessException {

        final RuleSet<Project> ruleSet = new RuleSet<>();
        ruleSet.addRule(new ActorIsProjectMember());
        final Set<Rule> wrongRules = ruleSet.evaluate(actor, project);
        if (!wrongRules.isEmpty()) {
            throw new BusinessException(wrongRules);
        }

        final TypedQuery<Task> q = em.createQuery("select t from Task t where t.project = :project", Task.class);
        q.setParameter("project", project);
        return q.getResultList();

    }

    @Override
    @Cacheable(value = "accountTasksCache")
    @PreAuthorize("hasPermission(#project,'" + TASK_LIST + "')")
    public List<Task> listUserTasks(Organization org, final Account account) {
        final TypedQuery<Task> q = em.createQuery("select t " +
                "from Task t " +
                "where t.assigned = :user and t.organizationID = :orgID", Task.class);
        q.setParameter("user", account);
        q.setParameter("orgID", org.getId());
        return q.getResultList();

    }

    @Override
    @Transactional
    @PreAuthorize("hasPermission(#project,'TASKS_CREATE')")
    public Task createTask(
            final Organization orgID,
            final Account actor,
            final Project project,
            final String taskName,
            final String taskComment,
            final Date startDate,
            final Date endDate,
            final double originalEstimate,
            final TaskType taskType,
            final Account assignedAccount,
            final String origin,
            final String remotePath,
            final String remoteId,
            final TaskStatus taskStatus,
            final Collection<Batch> batches
    ) {
        final Task newTask = new Task();
        newTask.setTaskType(taskType);
        newTask.setOrigin(origin);
        newTask.setRemotePath(remotePath);
        newTask.setRemoteId(remoteId);
        newTask.setName(taskName);
        newTask.setComments(taskComment);
        if (startDate != null) {
            newTask.setStartDate(new Date(startDate.getTime() + (2 * 60 * 60 * 1000) + 1));
        }
        if (endDate != null) {
            newTask.setEndDate(new Date(endDate.getTime() + (2 * 60 * 60 * 1000) + 1));
        }
        newTask.setComments(taskComment);
        newTask.setEffortLeft(originalEstimate);
        newTask.setOriginalEstimate(originalEstimate);
        newTask.setTaskStatus(taskStatus);
        newTask.setAssigned(assignedAccount);
        newTask.setOrganizationID(orgID.getId());
        if (batches!= null && !batches.isEmpty() ) {
            newTask.setBatches(new HashSet<>());
            newTask.getBatches().addAll(batches);
        }
        em.persist(newTask);
        em.merge(project);
        newTask.setProject(project);
        em.flush();

        LOGGER.info("Task " + taskName + " created by " + actor.getScreenName() + " in project " + project.getName());

        TimeboardSubjects.TASK_EVENTS.onNext(new TaskEvent(TimeboardEventType.CREATE, newTask, actor));

        return newTask;
    }

    @Override
    public Task updateTask(final Organization orgID, final Account actor, final Task task) {
        if (task.getProject().isMember(actor)) {
            task.setOrganizationID(orgID.getId());
            em.merge(task);
            em.flush();
        }
        TimeboardSubjects.TASK_EVENTS.onNext(new TaskEvent(TimeboardEventType.UPDATE, task, actor));

        return task;
    }

    @Override
    public void updateTasks(final Account actor, final List<Task> taskList) {
        for (final Task task : taskList) {
            em.merge(task);
        }
        LOGGER.info("User " + actor + " updated " + taskList.size() + " tasks ");
        em.flush();

    }

    @Override
    public void deleteTasks(final Account actor, final List<Task> taskList) {
        for (final Task task : taskList) {
            em.merge(task);
        }
        LOGGER.info("User " + actor + " deleted " + taskList.size() + " tasks ");
        em.flush();

    }

    @Override
    public AbstractTask getTaskByID(final Account account, final long id) throws BusinessException {
        final AbstractTask task = em.find(AbstractTask.class, id);
        if (task instanceof Task) {
            final RuleSet<Task> ruleSet = new RuleSet<>();
            ruleSet.addRule(new ActorIsProjectMemberbyTask());
            final Set<Rule> wrongRules = ruleSet.evaluate(account, (Task) task);
            if (!wrongRules.isEmpty()) {
                throw new BusinessException(wrongRules);
            }
        }

        return task;
    }

    public Optional<DefaultTask> getDefaultTaskByName(final String name) {
        DefaultTask data;
        try {

            final TypedQuery<DefaultTask> query = em.createQuery(
                    "select distinct t from DefaultTask t left join fetch t.imputations  where t.name = :name",
                    DefaultTask.class);
            query.setParameter("name", name);

            data = query.getSingleResult();

        } catch (final Exception e) {
            // handle JPA Exceptions
            data = null;
        }

        return Optional.ofNullable(data);
    }

    @Override
    public Optional<Task> getTaskByRemoteID(final Account actor, final String id) {
        Task task = null;
        try {
            final TypedQuery<Task> query = this.em.createQuery("select t from Task t where t.remoteId = :remoteID", Task.class);
            query.setParameter("remoteID", id);
            task = query.getSingleResult();
        } catch (final Exception e) {
        }
        return Optional.ofNullable(task);
    }


    @Override
    public Optional<Imputation> getImputation(final Account user, final DefaultTask task, final Date day) {
        final Imputation existingImputation = this.getImputationByDayByTask(em, day, task, user);
        return Optional.ofNullable(existingImputation);
    }

    @Override
    public Imputation getImputationByDayByTask(final EntityManager entityManager, final Date day, final AbstractTask task, final Account account) {
        final TypedQuery<Imputation> q = entityManager.createQuery("select i from Imputation i  " +
                "where i.task.id = :taskID and i.day = :day and i.account = :account", Imputation.class);
        q.setParameter("taskID", task.getId());
        q.setParameter("day", day);
        q.setParameter("account", account);
        return q.getResultList().stream().findFirst().orElse(null);
    }


    @Override
    public UpdatedTaskResult updateTaskEffortLeft(final Account actor, final Task task, final double effortLeft) throws BusinessException {
        final RuleSet<Task> ruleSet = new RuleSet<>();
        ruleSet.addRule(new ActorIsProjectMemberbyTask());
        final Set<Rule> wrongRules = ruleSet.evaluate(actor, task);
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
    public List<ProjectTasks> listTasksByProject(final Organization org, final Account actor, final Date ds, final Date de) {
        final List<ProjectTasks> projectTasks = new ArrayList<>();


        final TypedQuery<Task> q = em
                .createQuery("select distinct t from Task t left join fetch t.imputations where "
                        + "t.endDate >= :ds "
                        + "and t.organizationID = :orgID "
                        + "and t.startDate <= :de "
                        + "and t.assigned = :actor ", Task.class);
        q.setParameter("ds", ds);
        q.setParameter("de", de);
        q.setParameter("actor", actor);
        q.setParameter("orgID", org.getId());
        final List<Task> tasks = q.getResultList();

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
    public void deleteTaskByID(final Account actor, final long taskID) throws BusinessException {

        final RuleSet<Task> ruleSet = new RuleSet<>();
        ruleSet.addRule(new TaskHasNoImputation());
        ruleSet.addRule(new ActorIsProjectMemberbyTask());

        final Task task = em.find(Task.class, taskID);

        final Set<Rule> wrongRules = ruleSet.evaluate(actor, task);
        if (!wrongRules.isEmpty()) {
            throw new BusinessException(wrongRules);
        }

        em.remove(task);
        em.flush();
        TimeboardSubjects.TASK_EVENTS.onNext(new TaskEvent(TimeboardEventType.DELETE, task, actor));


        LOGGER.info("Task " + taskID + " deleted by " + actor.getName());

    }

    @Override
    public List<ValueHistory> getEffortSpentByTaskAndPeriod(final Account actor,
                                                            final Task task,
                                                            final Date startTaskDate,
                                                            final Date endTaskDate) throws BusinessException {

        final RuleSet<Task> ruleSet = new RuleSet<>();
        ruleSet.addRule(new ActorIsProjectMemberbyTask());
        final Set<Rule> wrongRules = ruleSet.evaluate(actor, task);
        if (!wrongRules.isEmpty()) {
            throw new BusinessException(wrongRules);
        }

        final TypedQuery<Object[]> query =
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
    public List<Batch> listProjectBatches(final Account actor, final Project project) throws BusinessException {
        final RuleSet<Project> ruleSet = new RuleSet<>();
        ruleSet.addRule(new ActorIsProjectMember());
        final Set<Rule> wrongRules = ruleSet.evaluate(actor, project);
        if (!wrongRules.isEmpty()) {
            throw new BusinessException(wrongRules);
        }

        final TypedQuery<Batch> q = em.createQuery("select b from Batch b where b.project = :project", Batch.class);
        q.setParameter("project", project);
        return q.getResultList();
    }

    @Override
    public List<BatchType> listProjectUsedBatchType(final Account actor, final Project project) throws BusinessException {
        final RuleSet<Project> ruleSet = new RuleSet<>();
        ruleSet.addRule(new ActorIsProjectMember());
        final Set<Rule> wrongRules = ruleSet.evaluate(actor, project);
        if (!wrongRules.isEmpty()) {
            throw new BusinessException(wrongRules);
        }

        final TypedQuery<BatchType> q = em.createQuery("select distinct b.type from Batch b where b.project = :project", BatchType.class);
        q.setParameter("project", project);
        return q.getResultList();
    }

    @Override
    public Batch getBatchById(final Account account, final long id) throws BusinessException {

        final Batch batch = em.find(Batch.class, id);
        final RuleSet<Batch> ruleSet = new RuleSet<>();
        ruleSet.addRule(new ActorIsProjectMemberByBatch());
        final Set<Rule> wrongRules = ruleSet.evaluate(account, batch);
        if (!wrongRules.isEmpty()) {
            throw new BusinessException(wrongRules);
        }
        return batch;
    }

    @Override
    @PostAuthorize("returnObject.organizationID == authentication.currentOrganization")
    public Batch createBatch(final Account actor,
                             final String name, final Date date, final BatchType type,
                             final Map<String, String> attributes,
                             final Set<Task> tasks, final Project project) throws BusinessException {

        final RuleSet<Project> ruleSet = new RuleSet<>();
        ruleSet.addRule(new ActorIsProjectMember());
        final Set<Rule> wrongRules = ruleSet.evaluate(actor, project);
        if (!wrongRules.isEmpty()) {
            throw new BusinessException(wrongRules);
        }


        final Batch newBatch = new Batch();
        newBatch.setName(name);
        newBatch.setType(type);
        newBatch.setDate(date);
        newBatch.setAttributes(attributes);
        newBatch.setTasks(tasks);
        newBatch.setProject(project);

        em.persist(newBatch);
        LOGGER.info("Batch {} created by {} ", newBatch.getName(), actor.getScreenName());

        return newBatch;

    }

    @Override
    @PreAuthorize("#batch.organizationID == authentication.currentOrganization")
    public Batch updateBatch(final Account actor, final Batch batch) throws BusinessException {
        final RuleSet<Batch> ruleSet = new RuleSet<>();
        ruleSet.addRule(new ActorIsProjectMemberByBatch());
        final Set<Rule> wrongRules = ruleSet.evaluate(actor, batch);
        if (!wrongRules.isEmpty()) {
            throw new BusinessException(wrongRules);
        }
        em.merge(batch);
        em.flush();

        LOGGER.info("Batch " + batch.getName() + " updated");
        return batch;
    }

    @Override
    public void deleteBatchByID(final Account actor, final long batchID) throws BusinessException {
        final RuleSet<Batch> ruleSet = new RuleSet<>();
        ruleSet.addRule(new ActorIsProjectMemberByBatch());
        ruleSet.addRule(new BatchHasNoTask());

        final Batch batch = em.find(Batch.class, batchID);

        final Set<Rule> wrongRules = ruleSet.evaluate(actor, batch);

        if (!wrongRules.isEmpty()) {
            throw new BusinessException(wrongRules);
        }

        em.remove(batch);
        em.flush();

        LOGGER.info("Batch " + batchID + " deleted by " + actor.getName());
    }

    @Override
    public List<Task> listTasksByBatch(final Account actor, final Batch batch) throws BusinessException {
        final RuleSet<Batch> ruleSet = new RuleSet<>();
        ruleSet.addRule(new ActorIsProjectMemberByBatch());
        final Set<Rule> wrongRules = ruleSet.evaluate(actor, batch);
        if (!wrongRules.isEmpty()) {
            throw new BusinessException(wrongRules);
        }
        final TypedQuery<Task> q = em.createQuery("select distinct t from Task t join t.batches b where b = :batch", Task.class);
        q.setParameter("batch", batch);
        return q.getResultList();
    }

    @Override
    public List<Batch> getBatchList(final Account actor, final Project project, final BatchType batchType) throws BusinessException {
        final RuleSet<Project> ruleSet = new RuleSet<>();
        ruleSet.addRule(new ActorIsProjectMember());
        final Set<Rule> wrongRules = ruleSet.evaluate(actor, project);
        if (!wrongRules.isEmpty()) {
            throw new BusinessException(wrongRules);
        }
        final TypedQuery<Batch> q = em.createQuery(
                "select distinct b from Batch b join b.tasks t where t.project = :project and b.type = :type",
                Batch.class);
        q.setParameter("project", project);
        q.setParameter("type", batchType);
        return q.getResultList();
    }

    @Override
    public Batch addTasksToBatch(
            final Account actor,
            final Batch b,
            final List<Task> selectedTaskIds,
            final List<Task> oldTaskIds) throws BusinessException {

        final RuleSet<Batch> ruleSet = new RuleSet<>();
        ruleSet.addRule(new ActorIsProjectMemberByBatch());
        final Set<Rule> wrongRules = ruleSet.evaluate(actor, b);
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
    public boolean isProjectOwner(final Account account, final Project project) {
        return new ActorIsProjectOwner().isSatisfied(account, project);
    }

    @Override
    public TASData generateTasData(final Account user, final Project project, final int month, final int year) {

        final TASData data = new TASData();

        data.setBusinessCode(project.getName());
        data.setMatriculeID(user.getEmail());
        data.setFirstName(user.getFirstName());
        data.setName(user.getName());
        data.setMonth(month);
        data.setYear(year);

        final Calendar start = Calendar.getInstance();
        start.set(year, month - 1, 1, 2, 0);
        final Calendar end = Calendar.getInstance();
        end.set(year, month, 1, 2, 0);

        final Optional<Organization> organization = this.organizationService.getOrganizationByID(user, project.getOrganizationID());

        final Map<Integer, Double> vacationImputations = this.timesheetService.getAllImputationsForAccountOnDateRange(
                organization.get(),
                start.getTime(),
                end.getTime(),
                user,
                new TimesheetService.TimesheetFilter<AbstractTask>(organization.get().getDefaultTasks().stream()
                        .filter(t -> t.getName().matches(defaultVacationTaskName)).findFirst().get()));

        final Map<Integer, Double> projectImputations = this.timesheetService.getAllImputationsForAccountOnDateRange(
                organization.get(),
                start.getTime(),
                end.getTime(),
                user,
                new TimesheetService.TimesheetFilter<Project>(project));
        final Map<Integer, String> comments = new HashMap<>();
        final Map<Integer, Double> otherProjectImputations = new HashMap<>();
        final List<Integer> dayMonthNums = new ArrayList<>();
        final List<String> dayMonthNames = new ArrayList<>();

        // rolling days in month
        for (int i = start.get(Calendar.DAY_OF_MONTH); start.before(end); start.add(Calendar.DATE, 1), i = start.get(Calendar.DAY_OF_MONTH)) {

            dayMonthNums.add(i);
            dayMonthNames.add(new SimpleDateFormat("EEEE", Locale.ENGLISH).format(start.getTime()).toLowerCase());

            Double vacationI = vacationImputations.get(i);
            Double projectI = projectImputations.get(i);
            final double otherProjectI;

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
