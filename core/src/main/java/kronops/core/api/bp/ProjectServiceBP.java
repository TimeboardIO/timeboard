package kronops.core.api.bp;

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
import kronops.core.model.Project;
import kronops.core.model.ProjectRole;
import kronops.core.model.User;

import java.util.List;
import java.util.Map;

public interface ProjectServiceBP {

    Project createProject(User owner, String projectName) throws BusinessException;

    Project saveProject(Project project) throws BusinessException;

    List<Project> getProjects();

    Project getProject(Long projectId);

    Project deleteProjectByID(Long projectID);

    Project updateProject(Project project) throws BusinessException;

    /**
     * @param project
     * @param memberships Key : userID, Value : user role for project param
     */
    Project updateProject(Project project, Map<Long, ProjectRole> memberships) throws BusinessException;
}
