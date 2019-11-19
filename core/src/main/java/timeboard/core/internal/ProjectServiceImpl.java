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

import timeboard.core.internal.rules.*;
import timeboard.core.api.*;
import timeboard.core.api.exceptions.BusinessException;
import timeboard.core.internal.rules.milestone.ActorIsProjectMemberByMilestone;
import timeboard.core.internal.rules.milestone.MilestoneHasNoTask;
import timeboard.core.internal.rules.task.ActorIsProjectMemberbyTask;
import timeboard.core.internal.rules.task.TaskHasNoImputation;
import timeboard.core.model.*;
import org.apache.aries.jpa.template.JpaTemplate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceScope;
import org.osgi.service.log.LogService;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.*;
import java.util.Calendar;
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


    public ProjectServiceImpl(){}

    public ProjectServiceImpl(JpaTemplate jpa){
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
    public Project getProjectByName(String projectName) {
        return jpa.txExpr(em -> {
            Project data = em.createQuery("select p from Project p where p.name = :name", Project.class)
                    .setParameter("name", projectName)
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

            this.logService.log(LogService.LOG_INFO, "Project " + p.getName() + " deleted");
            return p;
        });
    }

    @Override
    public Project updateProject(Project project) throws BusinessException {
        return jpa.txExpr(em -> {
            em.merge(project);
            em.flush();

            this.logService.log(LogService.LOG_INFO, "Project " + project.getName() + " updated");
            return project;
        });
    }

    @Override
    public ProjectDashboard projectDashboard(Project project) {

        return jpa.txExpr(em -> {

            TypedQuery<Object[]> q = em.createQuery("select " +
                    "COALESCE(sum(t.estimateWork),0) as estimateWork, " +
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

            return new ProjectDashboard(project.getQuotation(), (Double) EWandRTBD[0], (Double) EWandRTBD[1], effortSpent);

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

            this.logService.log(LogService.LOG_INFO, "Project " + project.getName() + " updated");
            return project;
        });

    }

    @Override
    public void save(ProjectMembership projectMembership) {
        this.jpa.tx(entityManager -> {
            entityManager.persist(projectMembership);
        });
        this.logService.log(LogService.LOG_INFO, "User " + projectMembership.getMember().getName() + " add to project "+projectMembership.getProject().getName());

    }


        /* -- TASKS -- */

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
    public Task createTask(User actor, Long projectID, String taskName, String taskComment,
                           Date startDate, Date endDate,  double OE, Long taskTypeID, User assignedUser) {
        Project p = jpa.txExpr(em -> em.find(Project.class, projectID));
        return this.createTask(actor,  p, taskName, taskComment, startDate, endDate, OE, taskTypeID, assignedUser);
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
            newTask.setEstimateWork(OE);
            newTask.setName(taskName);
            newTask.setComments(taskComment);
            newTask.setStartDate(startDate);
            newTask.setEndDate(endDate);
            newTask.setComments(taskComment);
            final TaskRevision taskRevision = new TaskRevision(actor, newTask, OE, assignedUser, TaskStatus.PENDING);
            newTask.getRevisions().add(taskRevision);
            newTask.setLatestRevision(taskRevision);
            entityManager.persist(newTask);

            entityManager.merge(project);
            newTask.setProject(project);
            entityManager.flush();

            this.logService.log(LogService.LOG_INFO, "Task " + taskName + " created by "+actor.getName()+" in project "+project.getName());

            return newTask;
        });
    }

    @Override
    public Task createTaskWithMilestone(User actor,
                                        Project project,
                                        String taskName,
                                        String taskComment,
                                        Date startDate,
                                        Date endDate,
                                        double OE,
                                        Long taskTypeID,
                                        User assignedUser,
                                        Milestone milestone
    ) {
        return this.jpa.txExpr(entityManager -> {
            Task newTask = new Task();
            newTask.setTaskType(this.findTaskTypeByID(taskTypeID));
            newTask.setEstimateWork(OE);
            newTask.setName(taskName);
            newTask.setComments(taskComment);
            newTask.setStartDate(startDate);
            newTask.setEndDate(endDate);
            newTask.setComments(taskComment);
            final TaskRevision taskRevision = new TaskRevision(actor, newTask, OE, assignedUser, TaskStatus.PENDING);
            newTask.getRevisions().add(taskRevision);
            newTask.setLatestRevision(taskRevision);
            entityManager.persist(newTask);

            entityManager.merge(project);
            newTask.setProject(project);
            if(milestone != null) {
                entityManager.merge(milestone);
            }
            newTask.setMilestone(milestone);

            entityManager.flush();

            this.logService.log(LogService.LOG_INFO, "Task " + taskName + " created by "+actor.getName()+" in project "+project.getName());

            return newTask;
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
                           User assignedUser,
                           String origin,
                           String remotePath,
                           String remoteId
                           ) {
        return this.jpa.txExpr(entityManager -> {
            Task newTask = new Task();
            newTask.setTaskType(this.findTaskTypeByID(taskTypeID));
            final TaskRevision taskRevision = new TaskRevision(actor, newTask, OE, assignedUser, TaskStatus.PENDING);
            newTask.getRevisions().add(taskRevision);
            newTask.setLatestRevision(taskRevision);
            newTask.setOrigin(origin);
            newTask.setRemotePath(remotePath);
            newTask.setRemoteId(remoteId);

            newTask.setName(taskName);
            newTask.setComments(taskComment);
            newTask.setStartDate(startDate);
            newTask.setEndDate(endDate);
            newTask.setComments(taskComment);

            entityManager.persist(newTask);
            entityManager.merge(project);
            newTask.setProject(project);
            entityManager.flush();

            this.logService.log(LogService.LOG_INFO, "Task " + taskName + " created by "+actor.getName()+" in project "+project.getName());

            return newTask;
        });
    }

    public Task updateTask(User actor, final Task task) {
        return this.updateTask(actor, task);
    }

    @Override
    public void createTasks(final User actor, final List<Task> taskList) {
        this.jpa.tx(entityManager -> {
            for (Task newTask : taskList) {
                final TaskRevision taskRevision = new TaskRevision(actor, newTask, 0, null, TaskStatus.PENDING);
                newTask.getRevisions().add(taskRevision);
                newTask.setLatestRevision(taskRevision);
                entityManager.persist(taskRevision);
                entityManager.persist(newTask);
                this.logService.log(LogService.LOG_INFO, "User " + actor + " tasks "+newTask.getName()+" on "+newTask.getStartDate());
            }
            this.logService.log(LogService.LOG_INFO, "User " + actor + " created "+taskList.size()+" tasks ");

            entityManager.flush();
        });
    }

    @Override
    public void updateTasks(User actor, List<Task> taskList) {
        this.jpa.tx(entityManager -> {
            for (Task task : taskList) {
                entityManager.merge(task);
            }
            this.logService.log(LogService.LOG_INFO, "User " + actor + " updated "+taskList.size()+" tasks ");
            entityManager.flush();
        });
    }

    @Override
    public void deleteTasks(User actor, List<Task> taskList) {
        this.jpa.tx(entityManager -> {
            for (Task task : taskList) {
                entityManager.merge(task);
            }
            this.logService.log(LogService.LOG_WARNING, "User " + actor + " deleted "+taskList.size()+" tasks ");
            entityManager.flush();
        });
    }

    @Override
    public Task addRevisionToTask(User actor, final Task task, TaskRevision rev) {
        return this.jpa.txExpr(entityManager -> {
            final Task taskFromDB = entityManager.find(Task.class, task.getId());
            if(rev != null) {
                taskFromDB.setLatestRevision(rev);
                rev.setTask(taskFromDB);
                entityManager.persist(rev);
            }
            taskFromDB.setTaskType(task.getTaskType());
            taskFromDB.setName(task.getName());
            taskFromDB.setComments(task.getComments());
            taskFromDB.setStartDate(task.getStartDate());
            taskFromDB.setEndDate(task.getEndDate());
            taskFromDB.setEstimateWork(task.getEstimateWork());

            entityManager.flush();
            this.logService.log(LogService.LOG_INFO, "Task " + task.getId() + " updated by "+actor.getName()+" in project "+task.getProject().getName());
            return taskFromDB;
        });
    }

    @Override
    public Task updateTaskWithMilestone(User actor, final Task task, TaskRevision rev, Milestone milestone) {
        return this.jpa.txExpr(entityManager -> {
            final Task taskFromDB = entityManager.find(Task.class, task.getId());
            taskFromDB.setLatestRevision(rev);

            taskFromDB.setTaskType(task.getTaskType());
            taskFromDB.setName(task.getName());
            taskFromDB.setComments(task.getComments());
            taskFromDB.setStartDate(task.getStartDate());
            taskFromDB.setEndDate(task.getEndDate());
            taskFromDB.setEstimateWork(task.getEstimateWork());
            taskFromDB.setMilestone(milestone);

            rev.setTask(taskFromDB);
            entityManager.persist(rev);

            entityManager.flush();

            this.logService.log(LogService.LOG_INFO, "Task " + task.getId() + " updated by "+actor.getName()+" in project "+task.getProject().getName());

            return taskFromDB;
        });
    }

    @Override
    public Task getTaskByID(long id) {
        return this.jpa.txExpr(entityManager -> {
            return entityManager.find(Task.class, id);
        });
    }

    public List<AbstractTask> getTasksByName(String name) {

        final List<AbstractTask> tasks =  new ArrayList<>();
        try{
            this.jpa.tx(entityManager -> {

                TypedQuery<Task> query = entityManager.createQuery("select distinct t from Task t left join fetch t.imputations  where t.name = :name", Task.class);
                query.setParameter("name", name );

                tasks.addAll(query.getResultList());
            });
        }catch (Exception e){
            // handle JPA Exceptions
        }

        try{
            this.jpa.tx(entityManager -> {
                TypedQuery<DefaultTask> query = entityManager.createQuery("select distinct t from DefaultTask t left join fetch t.imputations where t.name = :name", DefaultTask.class);
                query.setParameter("name", name );
                tasks.addAll(query.getResultList());
            });
        }catch (Exception e){
            //handle JPA Exceptions
        }

       return tasks;
    }

    private UpdatedTaskResult updateTaskImputation(User actor, Long taskID, Date day, double val, EntityManager entityManager){
        Calendar c = Calendar.getInstance();
        c.setTime(day);
        c.set(Calendar.HOUR_OF_DAY, 2);

        AbstractTask task = entityManager.find(AbstractTask.class, taskID);
        // special actions when task is a project task
        Task projectTask = (Task.class.isInstance(task)) ? (Task) task : null;

        // Task is available for imputations if this is a default task (not a project task) or task status is not pending
        boolean taskAvailableForImputations = (projectTask == null || projectTask.getLatestRevision().getTaskStatus() != TaskStatus.PENDING);

        TypedQuery<Imputation> q = entityManager.createQuery("select i from Imputation i  where i.task.id = :taskID and i.day = :day", Imputation.class);
        q.setParameter("taskID", taskID);
        q.setParameter("day", c.getTime());

        List<Imputation> existingImputations = q.getResultList();
        if (existingImputations.isEmpty() && val > 0.0 && val <= 1.0 && taskAvailableForImputations) {
            //No imputation for current task and day
            Imputation i = new Imputation();
            i.setDay(c.getTime());
            i.setTask(task);
            i.setUser(actor);
            i.setValue(val);
            if(projectTask != null){ //project task
                projectTask.updateCurrentRemainsToBeDone(actor,projectTask.getRemainsToBeDone() - val);
            }
            entityManager.persist(i);
        }

        if (!existingImputations.isEmpty()) {
            Imputation i = existingImputations.get(0);

            if(projectTask != null){ //project task
                if (i.getValue() < val) {
                    projectTask.updateCurrentRemainsToBeDone(actor,projectTask.getRemainsToBeDone() - Math.abs(val - i.getValue()));
                }
                if (i.getValue() > val) {
                    projectTask.updateCurrentRemainsToBeDone(actor,projectTask.getRemainsToBeDone() + Math.abs(i.getValue() - val));
                }
            }
            if (val == 0) {
                entityManager.remove(i);
            } else {
                i.setValue(val);
                entityManager.persist(i);
            }
        }
        this.logService.log(LogService.LOG_INFO, "User " + actor.getName() + " updated imputations for task "+task.getId()+"("+day+") in project "+((projectTask!= null) ? projectTask.getProject().getName() : "default") +" with value "+ val);

        if(projectTask != null) { //project task
            return new UpdatedTaskResult(projectTask.getProject().getId(), task.getId(), projectTask.getEffortSpent(), projectTask.getRemainsToBeDone(), projectTask.getEstimateWork(), projectTask.getReEstimateWork());
        }else{
            return new UpdatedTaskResult(0, task.getId(), 0, 0, 0, 0);
        }
    }

    @Override
    public UpdatedTaskResult updateTaskImputation(User actor, Long taskID, Date day, double val) {
        return this.jpa.txExpr(entityManager -> {
            UpdatedTaskResult updatedTaskResult = this.updateTaskImputation(actor, taskID, day, val, entityManager);
            entityManager.flush();
            return updatedTaskResult;
        });
    }


    @Override
    public List<UpdatedTaskResult> updateTaskImputations(User actor, List<Imputation> imputationsList) {
        return this.jpa.txExpr(entityManager -> {
            List<UpdatedTaskResult> result = new ArrayList<>();
            for(Imputation imputation : imputationsList){
                UpdatedTaskResult updatedTaskResult = this.updateTaskImputation(actor, imputation.getTask().getId(), imputation.getDay(), imputation.getValue(), entityManager);
                result.add(updatedTaskResult);
            }
            entityManager.flush();
            return result;
        });
    }


    @Override
    public UpdatedTaskResult updateTaskRTBD(User actor, Long taskID, double rtbd) {
        return this.jpa.txExpr(entityManager -> {
            Task task = entityManager.find(Task.class, taskID);
            task.setRemainsToBeDone(actor, rtbd);
            entityManager.flush();

            this.logService.log(LogService.LOG_INFO, "User " + actor.getName() + " updated remain to be done for task "+taskID+" in project "+task.getProject().getName()+" with value "+ rtbd);

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
                                    "t.endDate >= :ds " +
                                    "and t.startDate <= :de " +
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
    public List<DefaultTask> listDefaultTasks(Date ds, Date de) {

       return this.jpa.txExpr(entityManager -> {

            TypedQuery<DefaultTask> q = entityManager
                    .createQuery("select distinct t from DefaultTask t left join fetch t.imputations where " +
                                    "t.endDate >= :ds " +
                                    "and t.startDate <= :de "
                            , DefaultTask.class);
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
            return null;
        });

        if (exp != null) {
            throw exp;
        }
        this.logService.log(LogService.LOG_INFO, "Task " + taskID + " deleted by "+actor.getName());

    }

    @Override
    public List<EffortSpent> getEffortSpentByTaskAndPeriod(long taskId, Date startTaskDate, Date endTaskDate) {

        return this.jpa.txExpr(entityManager -> {
            TypedQuery<Object[]> query = (TypedQuery<Object[]>) entityManager.createNativeQuery("select " +
                    "i.day as date, SUM(value) OVER (ORDER BY day) AS sumPreviousValue " +
                    "from Imputation i  where i.task_id = :taskId and i.day >= :startTaskDate and i.day <= :endTaskDate");
            query.setParameter("taskId", taskId);
            query.setParameter("startTaskDate", startTaskDate);
            query.setParameter("endTaskDate", endTaskDate);

            return query.getResultList()
                    .stream()
                    .map(x -> new EffortSpent((Date) x[0], (Double) x[1]))
                    .collect(Collectors.toList());
        });
    }

    @Override
    public List<EffortLeft> getEffortLeftByTask(long taskId) {
        return this.jpa.txExpr(entityManager -> {
            TypedQuery<Object[]> query = (TypedQuery<Object[]>) entityManager.createNativeQuery("select " +
            "tr.revisionDate as date, tr.remainsToBeDone as effortLeft  " +
            "from TaskRevision tr " +
            "where tr.task_id = :taskId and tr.id IN ( " +
                    "SELECT MAX(trBis.id) " +
                    "FROM TaskRevision trBis " +
                    "GROUP BY trBis.task_id, DATE_FORMAT(trBis.revisionDate, \"%d/%m/%Y\")" +
             ");");

            query.setParameter("taskId", taskId);

            return query.getResultList()
                    .stream()
                    .map(x -> new EffortLeft((Date) x[0], (Double) x[1]))
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
        }catch (Exception e){
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
    public Map<String, Task> searchExistingTasksFromOrigin(Project project, String origin, String remotePath) {

        return this.jpa.txExpr(entityManager -> {
            Map<String, Task> map = new HashMap<>();
            TypedQuery<Task> q = entityManager.createQuery("select t from Task t where t.project = :project " +
                    "and t.origin = :origin " +
                    "and t.remotePath = :remotePath ", Task.class);
            q.setParameter("project", project);
            q.setParameter("origin", origin);
            q.setParameter("remotePath", remotePath);
            q.getResultList().forEach( task -> {
                map.put(task.getRemoteId(), task);
            });
            return map;
        });

    }

    @Override
    public List<Milestone> listProjectMilestones(Project project) {
        return this.jpa.txExpr(entityManager -> {
            TypedQuery<Milestone> q = entityManager.createQuery("select m from Milestone m where m.project = :project", Milestone.class);
            q.setParameter("project", project);
            return q.getResultList();
        });
    }

    @Override
    public Milestone getMilestoneById(long id) {
        return this.jpa.txExpr(entityManager -> {
            return entityManager.find(Milestone.class, id);
        });
    }

    @Override
    public Milestone createMilestone(String name, Date date, MilestoneType type, Map<String, String> attributes, Set<Task> tasks, Project project) {
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
    public Milestone updateMilestone(Milestone milestone) {
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
        this.logService.log(LogService.LOG_INFO, "Milestone " + milestoneID + " deleted by "+actor.getName());
    }

    @Override
    public List<Long> listTaskIdsByMilestone(Milestone milestone) {
        return this.jpa.txExpr(entityManager -> {
            TypedQuery<Long> q = entityManager.createQuery("select t.id from Task t where t.milestone = :milestone", Long.class);
            q.setParameter("milestone", milestone);
            return q.getResultList();
        });
    }

    @Override
    public Milestone addTasksToMilestone(Long currentMilestoneId, List<Long> selectedTaskIds, List<Long> oldTaskIds) {
        return this.jpa.txExpr(em -> {
            Milestone m = em.find(Milestone.class, currentMilestoneId);

            oldTaskIds.forEach(id -> {
                Task tr = em.find(Task.class, id);
                tr.setMilestone(null);
                m.getTasks().removeIf(task -> task.getId() == tr.getId());
             });

            selectedTaskIds.forEach(id -> {
                Task tr = em.find(Task.class, id);
                tr.setMilestone(m);
                m.getTasks().add(tr);
            });

            return m;
        });
    }
}
