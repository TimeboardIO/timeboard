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
@RequestMapping("/projects/{project}" + ProjectTeamCalendarController.URL)
public class ProjectTeamCalendarController extends ProjectBaseController {

    public static final String URL = "/calendar";

    @Autowired
    public ProjectService projectService;

    @Autowired
    public VacationService vacationService;

    @GetMapping
    protected String handleGet(final TimeboardAuthentication authentication,
                               @PathVariable final Project project, final Model model) throws BusinessException {

        model.addAttribute("project", project);
        this.initModel(model, authentication, project);

        return "project_calendar.html";
    }

    @GetMapping(value = "/list/{yearNum}/{monthNum}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, List<CalendarEvent>>> listTeam(final TimeboardAuthentication authentication,
                                                                     @PathVariable final Project project,
                                                                     @PathVariable final Integer yearNum,
                                                                     @PathVariable final Integer monthNum) throws BusinessException {
        final Account actor = authentication.getDetails();

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


    @GetMapping(value = "/list_batches/{yearNum}/{monthNum}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<CalendarEvent>> listBatches(final TimeboardAuthentication authentication,
                                                                     @PathVariable final Project project,
                                                                     @PathVariable final Integer yearNum,
                                                                     @PathVariable final Integer monthNum) throws BusinessException {
        final Account actor = authentication.getDetails();

        final List<Batch> batchList = this.projectService.getBatchList(actor, project, null);
        final List<Batch> filteredBatchList = batchList.stream()
                .filter(batch -> batch.getDate() != null)
                .collect(Collectors.toList());
        final List<CalendarEvent> calendarEvents = CalendarEvent.batchListToWrapperList(filteredBatchList);

        return ResponseEntity.ok(calendarEvents);
    }

}
