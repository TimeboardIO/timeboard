package timeboard.projects;

/*-
 * #%L
 * webui
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

import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import timeboard.core.api.ProjectService;
import timeboard.core.api.VacationService;
import timeboard.core.api.exceptions.BusinessException;
import timeboard.core.model.*;
import timeboard.core.security.TimeboardAuthentication;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * Display project dashboard.
 */
@Controller
@RequestMapping("/projects/{projectID}/calendar")
public class ProjectTeamCalendarController {


    @Autowired
    public ProjectService projectService;

    @Autowired
    public VacationService vacationService;

    @GetMapping
    protected String handleGet(final TimeboardAuthentication authentication,
                               @PathVariable final Long projectID, final Model model) throws BusinessException {

        final Account actor = authentication.getDetails();

        final Project project = this.projectService.getProjectByID(actor, authentication.getCurrentOrganization(), projectID);

        model.addAttribute("project", project);

        return "project_calendar.html";
    }

    @GetMapping(value = "/list/{yearNum}/{monthNum}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, List<CalendarEvent>>> listTags(final TimeboardAuthentication authentication,
                                                                     @PathVariable final Long projectID,
                                                                     @PathVariable final Integer yearNum,
                                                                     @PathVariable final Integer monthNum) throws BusinessException {
        final Account actor = authentication.getDetails();
        final Project project = this.projectService.getProjectByIdWithAllMembers(actor, projectID);

        // get existing vacation request for month/year
        final Map<Account, List<VacationRequest>> accountVacationRequestMap
                = this.vacationService.listProjectMembersVacationRequests(actor, project, monthNum, yearNum);

        // add member with no vacation request in interval
        project.getMembers().stream()
                .map(ProjectMembership::getMember)
                .forEach(m ->
                        accountVacationRequestMap.computeIfAbsent(m, t -> new ArrayList<VacationRequest>()));

        // re-balance key to user screen name and wrap request to ui calendar
        final Map<String, List<CalendarEvent>> newMap = accountVacationRequestMap.entrySet().stream()
                .collect(Collectors.toMap(e -> e.getKey().getScreenName(), e -> CalendarEvent.requestToWrapperList(e.getValue())));

        return ResponseEntity.ok(newMap);
    }


    public static class CalendarEventWrapper implements Serializable {

        private String name;
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        private Date date;
        private double value;
        private int type; // 0 MORNING - 1 FULL DAY - 2 AFTERNOON

        public CalendarEventWrapper() {
        }

        public CalendarEventWrapper(final Imputation imputation) {
            this.date = imputation.getDay();
            this.value = imputation.getValue();
            this.type = 1;
            this.name = imputation.getAccount().getScreenName();
        }

        public String getName() {
            return name;
        }

        public void setName(final String name) {
            this.name = name;
        }


        public Date getDate() {
            return date;
        }

        public void setDate(Date date) {
            this.date = date;
        }

        public int getType() {
            return type;
        }

        public void setType(int type) {
            this.type = type;
        }

        public double getValue() {
            return value;
        }

        public void setValue(double value) {
            this.value = value;
        }
    }

}
