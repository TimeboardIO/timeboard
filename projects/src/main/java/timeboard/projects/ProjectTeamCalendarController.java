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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
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

    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    @GetMapping
    protected String handleGet(TimeboardAuthentication authentication,
                               @PathVariable Long projectID, Model model) throws  BusinessException {

        final Account actor = authentication.getDetails();

        final Project project = this.projectService.getProjectByID(actor, authentication.getCurrentOrganization(), projectID);

        model.addAttribute("project", project);

        return "project_calendar.html";
    }

    @GetMapping(value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, List<CalendarEventWrapper>>> listTags(TimeboardAuthentication authentication,
                                                                            @PathVariable Long projectID) throws BusinessException {
        final Account actor = authentication.getDetails();
        final Project project = this.projectService.getProjectByIdWithAllMembers(actor, projectID);

        List<Imputation> imputations = this.projectService.listTeamVacations(actor, project, 0);

        Map<Account, List<VacationRequest>> accountVacationRequestMap = this.vacationService.listTeamVacationRequests(actor, project);

        Map<String, List<CalendarEventWrapper>> newMap = accountVacationRequestMap.entrySet().stream()
                .collect(Collectors.toMap(e -> e.getKey().getScreenName(), e -> requestToWrapperList(e.getValue())));


        return ResponseEntity.ok(newMap);
    }


    private List<CalendarEventWrapper> requestToWrapperList(List<VacationRequest> requests) {
        List<CalendarEventWrapper> results = new ArrayList<>();

        for (VacationRequest r : requests) {
            results.addAll(requestToWrapper(r));
        }

        return results;
    }


    private List<CalendarEventWrapper> requestToWrapper(VacationRequest request) {
        LinkedList<CalendarEventWrapper> results = new LinkedList<>();

        java.util.Calendar start = java.util.Calendar.getInstance();
        java.util.Calendar end = java.util.Calendar.getInstance();

        start.setTime(request.getStartDate());
        end.setTime(request.getEndDate());
        boolean last = true;
        while(last) {
            CalendarEventWrapper wrapper = new CalendarEventWrapper();

            wrapper.setName(request.getApplicant().getScreenName());
            wrapper.setDate (DATE_FORMAT.format(start.getTime()));
            if (request.getStatus() == VacationRequestStatus.ACCEPTED) {
                wrapper.setValue(1);
            }
            else if (request.getStatus() == VacationRequestStatus.PENDING ) {
                wrapper.setValue(0.5);
            } else {
                wrapper.setValue(0);
            }
            wrapper.setType(1);

            results.add(wrapper);

            last = start.before(end);
            start.roll(Calendar.DAY_OF_YEAR, 1);
        }

        if (request.getStartHalfDay().equals(VacationRequest.HalfDay.AFTERNOON)) {
            results.getFirst().setType(2);
        }

        if (request.getEndHalfDay().equals(VacationRequest.HalfDay.MORNING)) {
            results.getLast().setType(0);
        }

        return results;
    }

    public static class CalendarEventWrapper {

        private String name;
        private String date;
        private double value;
        private int type;

        public CalendarEventWrapper() { }

        public CalendarEventWrapper(Imputation imputation) {
            this.date = DATE_FORMAT.format(imputation.getDay());
            this.value = imputation.getValue();
            this.type = 1;
            this.name = imputation.getAccount().getScreenName();
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDate() {
            return date;
        }

        public int getType() {
            return type;
        }

        public double getValue() {
            return value;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public void setValue(double value) {
            this.value = value;
        }

        public void setType(int type) {
            this.type = type;
        }


    }

}
