package timeboard.core.api;

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

import timeboard.core.api.exceptions.BusinessException;
import timeboard.core.model.*;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface ProjectService {

    String ORIGIN_TIMEBOARD = "timeboard";

    /*
    === Projects ===
    */

    Project createProject(Account owner, String projectName) throws BusinessException;

    List<Project> listProjects(Account owner);

    Project getProjectByID(Account actor, Long projectID) throws BusinessException;

    Project getProjectByIdWithAllMembers(Account actor, Long projectId) throws BusinessException;

    Project getProjectByName(Account account, String projectArg) throws BusinessException;

    Project deleteProjectByID(Account actor, Project project) throws BusinessException;

    Project updateProject(Account actor, Project project) throws BusinessException;

    /**
     * Update a project.
     * @param project
     * @param memberships Key : userID, Value : user role for project param
     */
    Project updateProject(Account actor, Project project, Map<Long, ProjectRole> memberships) throws BusinessException;

    ProjectDashboard projectDashboard(Account actor, Project project) throws BusinessException;

    void save(Account actor, ProjectMembership projectMembership) throws BusinessException;

    /*
     == Tasks ==
     */

    List<Task> listUserTasks(Account account);

    List<Task> listProjectTasks(Account account, Project project) throws BusinessException;

    AbstractTask getTaskByID(Account account, long id) throws BusinessException;

    List<AbstractTask> getTasksByName(Account actor, String name);

    List<ProjectTasks> listTasksByProject(Account actor, Date ds, Date de);

    Task createTask(Account actor, Project project, String taskName, String taskComment,
                    Date startDate, Date endDate, double originalEstimate, Long taskTypeID, Account assignedAccountID, String origin, String remotePath, String remoteId, Milestone milestone);

    void createTasks(Account actor, List<Task> taskList);

    Task updateTask(Account actor, Task task);

    void updateTasks(Account actor, List<Task> taskList);

    UpdatedTaskResult updateTaskEffortLeft(Account actor, Task task, double effortLeft) throws BusinessException;

    void deleteTaskByID(Account actor, long taskID) throws BusinessException;

    void deleteTasks(Account actor, List<Task> taskList);

    /**
     * Search existing task from specific origin.
     * @param project target project
     * @param origin source (Github, GitLab, Jira, ...)
     * @param remotePath string key of source characteristics (owner, repository, ...)
     * @return list of task corresponding to the origin
     */
    Map<String, Task> searchExistingTasksFromOrigin(Account actor, Project project, String origin, String remotePath) throws BusinessException;

    List<TaskRevision> findAllTaskRevisionByTaskID(Account actor, Long taskID);



    /*
     == Imputations ==
     */
    /**
     * Return effort spent for a task.
     * @return List all effort spent for a task.
     */
    List<EffortHistory> getEffortSpentByTaskAndPeriod(Account actor, Task task, Date startTaskDate, Date endTaskDate) throws BusinessException;

    List<EffortHistory> getTaskEffortLeftHistory(Account actor, Task task) throws BusinessException;

    UpdatedTaskResult updateTaskImputation(Account actor, AbstractTask task, Date day, double imputation) throws BusinessException;

    List<UpdatedTaskResult> updateTaskImputations(Account actor, List<Imputation> imputationsList);



    /*
     == Default Tasks ==
     */
    List<DefaultTask> listDefaultTasks(Date ds, Date de);

    /**
     * Create a default task.
     * @return DefaultTask
     */
    DefaultTask createDefaultTask(DefaultTask task) throws BusinessException;

    /**
     * Update a default task.
     * @return DefaultTask
     */
    DefaultTask updateDefaultTask(DefaultTask dataEvent);



    /*
     == Milestones ==
     */
    /**
     * Return all milestones for a project.
     * @param project project
     * @return List milestones
     */
    List<Milestone> listProjectMilestones(Account actor, Project project) throws BusinessException;

    /**
     * Search milestone by id.
     * @param id milestone's id
     * @return Milestone
     */
    Milestone getMilestoneById(Account actor, long id) throws BusinessException;

    /**
     * Create a milestone.
     * @return Milestone
     */
    Milestone createMilestone(Account actor, String name, Date date, MilestoneType type, Map<String, String> attributes, Set<Task> tasks, Project project) throws BusinessException;

    /**
     * Update a milestone.
     * @return Milestone
     */
    Milestone updateMilestone(Account actor, Milestone milestone) throws BusinessException;

    /**
     * Delete a milestone.
     * @param milestoneID
     */
    void deleteMilestoneByID(Account actor, long milestoneID) throws BusinessException;

    List<Task> listTasksByMilestone(Account actor, Milestone milestone) throws BusinessException;

    Milestone addTasksToMilestone(Account actor, Milestone currentMilestone, List<Task> newTasks, List<Task> oldTasks) throws BusinessException;

    TaskType createTaskType(String name);

    /**
     * Return task types.
     * @return List all task types.
     */
    List<TaskType> listTaskType();

    TaskType findTaskTypeByID(Long taskTypeID);


    /*
     == Rule ==
     */
    boolean isProjectOwner(Account account, Project project);
}
