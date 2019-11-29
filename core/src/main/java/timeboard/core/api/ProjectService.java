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

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import timeboard.core.api.exceptions.BusinessException;
import timeboard.core.model.*;

public interface ProjectService {

    String ORIGIN_TIMEBOARD = "timeboard";

    /*
        === Projects ===
         */
    List<Project> listProjects(User owner);

    Project getProjectByID(User actor, Long projectID) throws BusinessException;

    Project getProjectByIdWithAllMembers(User actor, Long projectId) throws BusinessException;

    Project getProjectByName(User user, String projectArg) throws BusinessException;

    Project createProject(User owner, String projectName) throws BusinessException;

    Project updateProject(User actor, Project project) throws BusinessException;

    Project deleteProjectByID(User actor, Project project) throws BusinessException;

    ProjectDashboard projectDashboard(User actor, Project project) throws BusinessException;

    /**
     * Update a project.
     * @param project
     * @param memberships Key : userID, Value : user role for project param
     */
    Project updateProject(User actor, Project project, Map<Long, ProjectRole> memberships) throws BusinessException;

    void save(User actor, ProjectMembership projectMembership) throws BusinessException;

    /*
     == Tasks ==
     */

    List<Task> listUserTasks(User user);

    List<Task> listProjectTasks(User user, Project project) throws BusinessException;

    AbstractTask getTaskByID(User user, long id) throws BusinessException;

    List<AbstractTask> getTasksByName(User actor, String name);

    List<ProjectTasks> listTasksByProject(User actor, Date ds, Date de);

    Task createTask(User actor, Project project, String taskName, String taskComment,
                    Date startDate, Date endDate, double OE, Long taskTypeID, User assignedUserID, String origin, String remotePath, String remoteId, Milestone milestone);

    void createTasks(User actor, List<Task> taskList);

    Task updateTask(User actor, Task task);

    void updateTasks(User actor, List<Task> taskList);

    UpdatedTaskResult updateTaskEffortLeft(User actor, Task task, double effortLeft) throws BusinessException;

    void deleteTaskByID(User actor, long taskID) throws BusinessException;

    void deleteTasks(User actor, List<Task> taskList);

    /**
     * Search existing task from specific origin.
     * @param project target project
     * @param origin source (Github, GitLab, Jira, ...)
     * @param remotePath string key of source characteristics (owner, repository, ...)
     * @return list of task corresponding to the origin
     */
    Map<String, Task> searchExistingTasksFromOrigin(User actor, Project project, String origin, String remotePath) throws BusinessException;

    List<TaskRevision> findAllTaskRevisionByTaskID(User actor, Long taskID);



    /*
     == Imputations ==
     */
    /**
     * Return effort spent for a task.
     * @return List all effort spent for a task.
     */
    List<EffortHistory> getEffortSpentByTaskAndPeriod(User actor, Task task, Date startTaskDate, Date endTaskDate) throws BusinessException;

    List<EffortHistory> getTaskEffortLeftHistory(User actor, Task task) throws BusinessException;

    UpdatedTaskResult updateTaskImputation(User actor, AbstractTask task, Date day, double imputation);

    List<UpdatedTaskResult> updateTaskImputations(User actor, List<Imputation> imputationsList);



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
    List<Milestone> listProjectMilestones(User actor, Project project) throws BusinessException;

    /**
     * Search milestone by id.
     * @param id milestone's id
     * @return Milestone
     */
    Milestone getMilestoneById(User actor, long id) throws BusinessException;

    /**
     * Create a milestone.
     * @return Milestone
     */
    Milestone createMilestone(User actor, String name, Date date, MilestoneType type, Map<String, String> attributes, Set<Task> tasks, Project project) throws BusinessException;

    /**
     * Update a milestone.
     * @return Milestone
     */
    Milestone updateMilestone(User actor, Milestone milestone) throws BusinessException;

    /**
     * Delete a milestone.
     * @param milestoneID
     */
    void deleteMilestoneByID(User actor, long milestoneID) throws BusinessException;

    List<Task> listTasksByMilestone(User actor, Milestone milestone) throws BusinessException;

    Milestone addTasksToMilestone(User actor, Milestone currentMilestone, List<Task> newTasks, List<Task> oldTasks) throws BusinessException;

    TaskType createTaskType(String name);

    /**
     * Return task types.
     * @return List all task types.
     */
    List<TaskType> listTaskType();

    TaskType findTaskTypeByID(Long taskTypeID);
}
