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

    String ORIGIN_TIMEBOARD = "timeboard";

    /*
    === Projects ===
    */

    Project createProject(Account owner, String projectName) throws BusinessException;

    List<Project> listProjects(Account owner, Long org);

    Project getProjectByID(Account actor, Long orgID, Long projectID) throws BusinessException;

    Project getProjectByIdWithAllMembers(Account actor, Long projectId) throws BusinessException;

    Project getProjectByName(Account account, String projectArg) throws BusinessException;

    Project archiveProjectByID(Account actor, Project project) throws BusinessException;

    Project updateProject(Account actor, Project project) throws BusinessException;

    /**
     * Update a project.
     *
     * @param project
     * @param memberships Key : userID, Value : user role for project param
     */
    Project updateProject(Account actor, Project project, Map<Long, MembershipRole> memberships) throws BusinessException;

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
                    Date startDate, Date endDate, double originalEstimate,
                    Long taskTypeID, Account assignedAccountID, String origin,
                    String remotePath, String remoteId,
                    TaskStatus taskStatus, Batch batch);

    void createTasks(Account actor, List<Task> taskList);

    Task updateTask(Account actor, Task task);

    void updateTasks(Account actor, List<Task> taskList);

    UpdatedTaskResult updateTaskEffortLeft(Account actor, Task task, double effortLeft) throws BusinessException;

    void deleteTaskByID(Account actor, long taskID) throws BusinessException;

    void deleteTasks(Account actor, List<Task> taskList);

    /**
     * Search existing task from specific origin.
     *
     * @param project    target project
     * @param origin     source (Github, GitLab, Jira, ...)
     * @param remotePath string key of source characteristics (owner, repository, ...)
     * @return list of task corresponding to the origin
     */
    Map<String, Task> searchExistingTasksFromOrigin(Account actor,
                                                    Project project,
                                                    String origin,
                                                    String remotePath) throws BusinessException;



    Optional<Task> getTaskByRemoteID(Account actor, String id);

    /*
     == Imputations ==
     */

    /**
     * Return effort spent for a task.
     *
     * @return List all effort spent for a task.
     */
    List<ValueHistory> getEffortSpentByTaskAndPeriod(Account actor,
                                                     Task task,
                                                     Date startTaskDate,
                                                     Date endTaskDate) throws BusinessException;

    List<ValueHistory> getTaskEffortLeftHistory(Account actor, Task task) throws BusinessException;

    UpdatedTaskResult updateTaskImputation(Account actor,
                                           AbstractTask task,
                                           Date day, double imputation) throws BusinessException;

    List<UpdatedTaskResult> updateTaskImputations(Account actor, List<Imputation> imputationsList);


    /*
     == Default Tasks ==
     */
    List<DefaultTask> listDefaultTasks(Date ds, Date de);

    /**
     * Create a default task.
     *
     * @return DefaultTask
     */
    DefaultTask createDefaultTask(Account actor, String task) throws BusinessException;

    /**
     * default tasks can not be deleted, so they are set disabled and hidden from UI
     *
     * @param actor
     * @param taskID
     * @throws BusinessException
     */
    void disableDefaultTaskByID(Account actor, long taskID) throws BusinessException;


    /*
     == Batches ==
     */

    /**
     * Return all batches for a project.
     *
     * @param project project
     * @return List batches
     */
    List<Batch> listProjectBatches(Account actor, Project project) throws BusinessException;

    /**
     * Search batch by id.
     *
     * @param id batch's id
     * @return Batch
     */
    Batch getBatchById(Account actor, long id) throws BusinessException;

    /**
     * Create a batch.
     *
     * @return Batch
     */
    Batch createBatch(Account actor,
                      String name,
                      Date date,
                      BatchType type,
                      Map<String, String> attributes,
                      Set<Task> tasks, Project project) throws BusinessException;

    /**
     * Update a batch.
     *
     * @return Batch
     */
    Batch updateBatch(Account actor, Batch batch) throws BusinessException;

    /**
     * Delete a batch.
     *
     * @param batchID
     */
    void deleteBatchByID(Account actor, long batchID) throws BusinessException;

    List<Task> listTasksByBatch(Account actor, Batch batch) throws BusinessException;

    Batch addTasksToBatch(Account actor,
                          Batch currentBatch,
                          List<Task> newTasks, List<Task> oldTasks) throws BusinessException;

    TaskType createTaskType(Account actor, String name);

    void disableTaskType(Account actor, TaskType type);

    /**
     * Return task types.
     *
     * @return List all task types.
     */
    List<TaskType> listTaskType();

    TaskType findTaskTypeByID(Long taskTypeID);

    TASData generateTasData(Account user, Project project, int month, int year);
    /*
     == Rule ==
     */

    boolean isProjectOwner(Account user, Project project);

}
