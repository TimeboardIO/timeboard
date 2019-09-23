package kronops.core.api;

/*-
 * #%L
 * core
 * %%
 * Copyright (C) 2019 Kronops
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

import kronops.core.api.exceptions.BusinessException;
import kronops.core.model.*;

import java.util.List;
import java.util.Map;

public interface ProjectServiceBP {

    /*
    === Clusters ===
     */
    void addProjectToProjectCluster(ProjectCluster projectCluster, Project newProject);

    void saveProjectCluster(ProjectCluster root);

    List<TreeNode> computeClustersTree();

    List<ProjectCluster> listProjectClusters();

    ProjectCluster findProjectsClusterByID(long cluster);


    /*
    === Projects ===
     */
    Project createProject(User owner, String projectName) throws BusinessException;

    List<Project> listProjects(User user);

    Project getProject(Long projectId);

    Project deleteProjectByID(Long projectID);

    Project updateProject(Project project) throws BusinessException;


    /**
     * @param project
     * @param memberships Key : userID, Value : user role for project param
     */
    Project updateProject(Project project, Map<Long, ProjectRole> memberships) throws BusinessException;

    void save(ProjectMembership projectMembership);

    void deleteProjectClusterByID(Long clusterID);

    void updateProjectClusters(List<ProjectCluster> updatedProjectCluster, Map<Long, Long> clusterParent);


    /*
     == Tasks ==
     */

    List<Task> listProjectTasks(Project project);

    Task createTask(Project project, Task task);

    Task updateTask(Task task);

    Task getTask(long id);

    void deleteTaskByID(long taskID) throws BusinessException;
}
