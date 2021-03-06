package timeboard.core.api.sync;

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

import org.quartz.CronScheduleBuilder;
import timeboard.core.model.Account;
import timeboard.core.model.Organization;
import timeboard.core.model.Project;

import java.util.List;

public interface ProjectSyncService {

    public static final String ORG_ID = "orgID";
    public static final String ACCOUNT_ID = "accountID";
    public static final String PROJECT_ID = "projectID";
    public static final String SERVICE_NAME = "serviceName";
    public static final String CREDENTIALS = "credentials";

    void syncProjectTasksWithSchedule(final Organization org,
                                      final Account actor,
                                      final Project project,
                                      final String serviceName,
                                      final List<ProjectSyncCredentialField> creds,
                                      CronScheduleBuilder cronScheduleBuilder);

    void syncProjectTasks(final Organization org,
                          final Account actor,
                          final Project project,
                          final String serviceName,
                          final List<ProjectSyncCredentialField> creds);


    List<ProjectSyncCredentialField> getServiceFields(String serviceName);
}
