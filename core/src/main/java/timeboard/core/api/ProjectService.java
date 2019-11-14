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

import java.util.*;

public interface ProjectService {



    /*
    === Projects ===
     */
    Project createProject(User owner, String projectName) throws BusinessException;

    List<Project> listProjects(User owner);

    Project getProjectByID(User owner, Long projectID);

    Project getProjectByName(String projectArg);

    Project deleteProjectByID(Long projectID);

    Project updateProject(Project project) throws BusinessException;

    ProjectDashboard projectDashboard(Project project);

    /**
     * @param project
     * @param memberships Key : userID, Value : user role for project param
     */
    Project updateProject(Project project, Map<Long, ProjectRole> memberships) throws BusinessException;

    void save(ProjectMembership projectMembership);




    /*
     == Tasks ==
     */

    List<Task> listProjectTasks(Project project);


    List<Task> listUserTasks(User user);

    /**
     * @return List all task types.
     */
    List<TaskType> listTaskType();

    Task createTask(User actor, Project project, String taskName, String taskComment, Date startDate, Date endDate, double OE, Long taskTypeID, User assignedUserID);

    Task createTaskWithMilestone(User actor,
                                 Project project,
                                 String taskName,
                                 String taskComment,
                                 Date startDate,
                                 Date endDate,
                                 double OE,
                                 Long taskTypeID,
                                 User assignedUser,
                                 Milestone milestone
    );

    Task createTask(User actor, Project project, String taskName, String taskComment,
                    Date startDate, Date endDate, double OE, Long taskTypeID, User assignedUserID, String origin, String remotePath, String remoteId);

    Task updateTask(User actor, Task task, TaskRevision rev);

    Task updateTask(User actor, Task task);

    void createTasks(User actor, List<Task> taskList);

    void updateTasks(User actor, List<Task> taskList);

    void deleteTasks(User actor, List<Task> taskList);

    Task updateTaskWithMilestone(User actor, Task task, TaskRevision rev, Milestone milestone);

    AbstractTask getTask(long id);
    List<AbstractTask> getTasksByName(String name);


    void deleteTaskByID(User actor, long taskID) throws BusinessException;

    List<ProjectTasks> listTasksByProject(User actor, Date ds, Date de);

    UpdatedTaskResult updateTaskRTBD(User actor, Long taskID, double rtbd);

    TaskType findTaskTypeByID(Long taskTypeID);

    /**
     * Search existing task from specific origin
     * @param project target project
     * @param origin source (Github, GitLab, Jira, ...)
     * @param remotePath string key of source characteristics (owner, repository, ...)
     * @return list of task corresponding to the origin
     */
    Map<String, Task> searchExistingTasksFromOrigin(Project project, String origin, String remotePath);

    List<TaskRevision> findAllTaskRevisionByTaskID(User actor, Long taskID);


    /*
     == Imputations ==
     */
    /**
     * @return List all effort spent for a task.
     */
    List<EffortSpent> getESByTaskAndPeriod(long taskId, Date startTaskDate, Date endTaskDate);

    List<EffortEstimate> getEstimateByTask(long taskId);

    UpdatedTaskResult updateTaskImputation(User actor, Long taskID, Date day, double imputation);

    List<UpdatedTaskResult> updateTaskImputations(User actor, List<Imputation> imputationsList);


    /*
     == Default Tasks ==
     */

    /**
     * Create a default task
     * @return DefaultTask
     */
    DefaultTask createDefaultTask(DefaultTask task) throws BusinessException;

    /**
     * Update a default task
     * @return DefaultTask
     */
    DefaultTask updateDefaultTask(DefaultTask dataEvent);

    List<DefaultTask> listDefaultTasks(Date ds, Date de);


    /*
     == Milestones ==
     */

    /**
     * Return all milestones for a project
     * @param project project
     * @return List milestones
     */
    List<Milestone> listProjectMilestones(Project project);

    /**
     * Search milestone by id
     * @param id milestone's id
     * @return Milestone
     */
    Milestone getMilestoneById(long id);

    /**
     * Create a milestone
     * @return Milestone
     */
    Milestone createMilestone(String name, Date date, MilestoneType type, Map<String, String> attributes, Set<Task> tasks, Project project);

    /**
     * Update a milestone
     * @return Milestone
     */
    Milestone updateMilestone(Milestone milestone);

    /**
     * Delete a milestone
     * @param milestoneID
     */
    void deleteMilestoneByID(User actor, long milestoneID) throws BusinessException;


}
