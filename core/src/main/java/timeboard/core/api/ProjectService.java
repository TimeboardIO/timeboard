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

import javax.persistence.EntityManager;
import java.util.*;

public interface ProjectService {

    String ORIGIN_TIMEBOARD = "timeboard";

    /*
    === Projects ===
    */

    Project createProject(Long orgID, Account owner, String projectName) throws BusinessException;

    List<Project> listProjects(Account owner, Long orgID);

    Project getProjectByID(Account actor, Long orgID, Long projectID) throws BusinessException;

    Project archiveProjectByID(Account actor, Project project) throws BusinessException;

    Project updateProject(Account actor, Project project) throws BusinessException;

    ProjectDashboard projectDashboard(Account actor, Project project) throws BusinessException;

    void save(Account actor, ProjectMembership projectMembership) throws BusinessException;

    /*
     == Tasks ==
     */

    List<Task> listUserTasks(Long orgID, Account account);

    List<Task> listProjectTasks(Account account, Project project) throws BusinessException;

    AbstractTask getTaskByID(Account account, long id) throws BusinessException;

    List<ProjectTasks> listTasksByProject(Long orgID, Account actor, Date ds, Date de);

    Task createTask(final Long orgID,
                    final Account actor,
                    final Project project,
                    final String taskName,
                    final String taskComment,
                    final Date startDate,
                    final Date endDate,
                    final double originalEstimate,
                    final Long taskTypeID,
                    final Account assignedAccountID,
                    final String origin,
                    final String remotePath,
                    final String remoteId,
                    final TaskStatus taskStatus,
                    final Batch batch);

    /**
     * Update task in database
     *
     * @param orgID relevant {@link Organization} ID
     * @param actor issuer {@link Account}
     * @param task  {@link Task} to update in database
     * @return updated {@link Task}
     */
    Task updateTask(final Long orgID,
                    final Account actor,
                    final Task task);

    void updateTasks(Account actor, List<Task> taskList);

    Imputation getImputationByDayByTask(EntityManager entityManager, Date day, AbstractTask task, Account account);

    UpdatedTaskResult updateTaskEffortLeft(Account actor, Task task, double effortLeft) throws BusinessException;

    void deleteTaskByID(Account actor, long taskID) throws BusinessException;

    void deleteTasks(Account actor, List<Task> taskList);


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


    TASData generateTasData(Account user, Project project, int month, int year);

    /*
     == Rule ==
     */

    boolean isProjectOwner(Account user, Project project);

    List<Batch> getBatchList(Account user, Project project, BatchType batchType) throws BusinessException;

    List<BatchType> listProjectUsedBatchType(Account actor, Project project) throws BusinessException;

    Optional<Imputation> getImputation(Account user, DefaultTask task, Date day);

    List<Account> findOwnersOfAnyUserProject(Account user);

    boolean isOwnerOfAnyUserProject(Account owner, Account user);
}
