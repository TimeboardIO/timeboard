package timeboard.projects;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import timeboard.core.TimeboardAuthentication;
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

        this.projectSyncService.syncProjectTasks(authentication.getCurrentOrganization(), actor, project, serviceName, creds);


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
