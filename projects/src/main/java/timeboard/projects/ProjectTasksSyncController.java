package timeboard.projects;

/*-
 * #%L
 * projects
 * %%
 * Copyright (C) 2019 - 2020 Timeboard
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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.quartz.CronScheduleBuilder;
import org.quartz.JobExecutionContext;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import timeboard.core.security.TimeboardAuthentication;
import timeboard.core.api.ProjectService;
import timeboard.core.api.exceptions.BusinessException;
import timeboard.core.api.sync.ProjectSyncCredentialField;
import timeboard.core.api.sync.ProjectSyncService;
import timeboard.core.model.Account;
import timeboard.core.model.Project;

import java.util.List;
import java.util.Optional;


@Controller
@RequestMapping("/projects/{projectID}/tasks/sync")
public class ProjectTasksSyncController {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Autowired
    public ProjectService projectService;

    @Autowired
    public ProjectSyncService projectSyncService;

    @Autowired
    private Scheduler scheduler;

    @PostMapping(value = "/{serviceName}", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    protected String importFromJIRA(
            TimeboardAuthentication authentication,
            @PathVariable Long projectID,
            @PathVariable String serviceName,
            @RequestBody MultiValueMap<String, String> formBody) throws BusinessException, JsonProcessingException {

        final Account actor = authentication.getDetails();
        final Project project = this.projectService.getProjectByID(actor, authentication.getCurrentOrganization(), projectID);

        final List<ProjectSyncCredentialField> creds = this.projectSyncService.getServiceFields(serviceName);

        creds.forEach(field -> {
            if(formBody.containsKey(field.getFieldKey())){
                field.setValue(formBody.get(field.getFieldKey()).get(0));
            }
        });

        // Job launched directly
        this.projectSyncService.syncProjectTasks(authentication.getCurrentOrganization(), actor, project, serviceName, creds);

        // Job launched recursively
        CronScheduleBuilder cronScheduleBuilder = CronScheduleBuilder.cronSchedule("0 0/1 * 1/1 * ? *");
        this.projectSyncService.syncProjectTasksWithSchedule(authentication.getCurrentOrganization(), actor, project,
                serviceName, creds, cronScheduleBuilder);



        return "redirect:/projects/"+projectID+"/tasks";
    }

    @GetMapping(value = "/inProgress")
    protected ResponseEntity handleGet(TimeboardAuthentication authentication, @PathVariable Long projectID)
            throws JsonProcessingException, SchedulerException {


        Optional<JobExecutionContext> jobInProgress = scheduler.getCurrentlyExecutingJobs()
                    .stream()
                    .filter(jobExecutionContext -> jobExecutionContext.getJobDetail().getKey().getName().equals(projectID.toString()))
                    .findFirst();

        if(jobInProgress.isPresent()) {
            return ResponseEntity.status(HttpStatus.OK).body(MAPPER.writeValueAsString("IN_PROGRESS"));
        }else{
            return ResponseEntity.status(HttpStatus.OK).body(MAPPER.writeValueAsString("NO_JOB"));
        }
    }
}
