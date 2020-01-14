package timeboard.vacations;

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
import org.springframework.web.bind.annotation.*;
import timeboard.core.api.ProjectService;
import timeboard.core.api.TimeboardSubjects;
import timeboard.core.api.UserService;
import timeboard.core.api.VacationService;
import timeboard.core.api.exceptions.BusinessException;
import timeboard.core.internal.events.TimeboardEventType;
import timeboard.core.internal.events.VacationEvent;
import timeboard.core.model.Account;
import timeboard.core.model.VacationRequest;
import timeboard.core.model.VacationRequestStatus;
import timeboard.core.security.TimeboardAuthentication;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Controller
@RequestMapping("/vacation")
public class VacationsController {

    @Autowired
    private UserService userService;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private VacationService vacationService;

    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    @GetMapping
    protected String handleGet(TimeboardAuthentication authentication, Model model) {
        return "vacations.html";
    }

    @GetMapping(value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<VacationRequestWrapper>> listRequests(TimeboardAuthentication authentication) {

        final Account actor = authentication.getDetails();

        List<VacationRequest> list =  this.vacationService.listUserVacations(actor);
        List<VacationRequestWrapper> returnList = new ArrayList<>();

        for (VacationRequest v : list) {
            returnList.add(new VacationRequestWrapper(v));
        }

        return ResponseEntity.ok(returnList);
    }

    @GetMapping(value = "/toValidate/list", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<VacationRequestWrapper>> listToValidateRequests(TimeboardAuthentication authentication) {

        final Account actor = authentication.getDetails();

        List<VacationRequest> list =  this.vacationService.listVacationsToValidateByUser(actor);
        List<VacationRequestWrapper> returnList = new ArrayList<>();

        for (VacationRequest v : list) {
            returnList.add(new VacationRequestWrapper(v));
        }

        return ResponseEntity.ok(returnList);
    }

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity createRequest(TimeboardAuthentication authentication,
            @ModelAttribute VacationRequestWrapper requestWrapper)
            throws BusinessException, ParseException {

        Account actor = authentication.getDetails();
        Account assignee = this.userService.findUserByID(requestWrapper.assigneeID);
        Date startDate = DATE_FORMAT.parse(requestWrapper.start);
        Date endDate = DATE_FORMAT.parse(requestWrapper.end);


        if(startDate.getTime() > endDate.getTime()){
            return ResponseEntity.badRequest().body("Start date must be before end date. ");
        }

        if(startDate.before(new Date())) {
            return ResponseEntity.badRequest().body("You can not submit vacation request in the past.");
        }

        if(assignee == null){
            return ResponseEntity.badRequest().body("Please enter user to notify. ");
        }

        VacationRequest request = new VacationRequest();
        request.setApplicant(actor);
        request.setAssignee(assignee);
        request.setLabel(requestWrapper.label);
        request.setStartDate(startDate);
        request.setEndDate(endDate);
        request.setStartHalfDay(requestWrapper.isHalfStart() ? VacationRequest.HalfDay.AFTERNOON : VacationRequest.HalfDay.MORNING);
        request.setEndHalfDay(requestWrapper.isHalfEnd() ? VacationRequest.HalfDay.MORNING : VacationRequest.HalfDay.AFTERNOON);
        request.setStatus(VacationRequestStatus.PENDING);

        if (!vacationService.listVacationRequestsByPeriod(actor,request).isEmpty()) {
            return ResponseEntity.badRequest().body("You already have a vacation request covering this period.");
        }

        this.vacationService.createVacationRequest(actor, request);

        TimeboardSubjects.VACATION_EVENTS.onNext(new VacationEvent(TimeboardEventType.CREATE, request));

        return this.listRequests(authentication) ;
    }

    @PatchMapping(value = "/approve/{requestID}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity approveRequest(TimeboardAuthentication authentication,
                                                            @PathVariable VacationRequest vacationRequest) throws BusinessException {
        Account actor = authentication.getDetails();
        this.vacationService.approveVacationRequest(actor, vacationRequest);

        return this.listToValidateRequests(authentication) ;
    }

    @PatchMapping(value = "/reject/{requestID}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity rejectRequest(TimeboardAuthentication authentication,
                                                                       @PathVariable VacationRequest vacationRequest) throws BusinessException {
        Account actor = authentication.getDetails();
        this.vacationService.rejectVacationRequest(actor, vacationRequest);

        return this.listToValidateRequests(authentication) ;
    }

    @DeleteMapping(value = "/{requestID}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity deleteRequest(TimeboardAuthentication authentication,
                                                                  @PathVariable VacationRequest vacationRequest) throws BusinessException {
        Account actor = authentication.getDetails();
        if(vacationRequest.getStatus() == VacationRequestStatus.ACCEPTED && vacationRequest.getStartDate().before(new Date())){
            return ResponseEntity.badRequest().body("Cannot cancel started vacation.");
        } else {
            this.vacationService.deleteVacationRequest(actor, vacationRequest);
        }

        return this.listRequests(authentication) ;
    }

    public static class VacationRequestWrapper {

        public long id;
        public String start;
        public String end;
        public boolean halfStart;
        public boolean halfEnd;
        public String label;
        public String status;
        public long assigneeID;
        public String assigneeName;
        public long applicantID;
        public String applicantName;

        public VacationRequestWrapper() { }

        public VacationRequestWrapper(VacationRequest r) {
            this.id = r.getId();
            this.start = DATE_FORMAT.format(r.getStartDate());
            this.end = DATE_FORMAT.format(r.getEndDate());
            this.halfStart = r.getStartHalfDay().equals(VacationRequest.HalfDay.AFTERNOON);
            this.halfEnd = r.getEndHalfDay().equals(VacationRequest.HalfDay.MORNING);
            this.status = r.getStatus().name();

            if(r.getAssignee() != null) {
                this.assigneeID = r.getAssignee().getId();
                this.assigneeName = r.getAssignee().getScreenName();
            } else {
                this.assigneeID = 0;
                this.assigneeName = "";
            }
            if(r.getApplicant() != null) {
                this.applicantID = r.getApplicant().getId();
                this.applicantName = r.getApplicant().getScreenName();
            } else {
                this.applicantID = 0;
                this.applicantName = "";
            }
        }

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }

        public String getStart() {
            return start;
        }

        public void setStart(String start) {
            this.start = start;
        }

        public String getEnd() {
            return end;
        }

        public void setEnd(String end) {
            this.end = end;
        }

        public boolean isHalfStart() {
            return halfStart;
        }

        public void setHalfStart(boolean halfStart) {
            this.halfStart = halfStart;
        }

        public boolean isHalfEnd() {
            return halfEnd;
        }

        public void setHalfEnd(boolean halfEnd) {
            this.halfEnd = halfEnd;
        }

        public long getAssigneeID() {
            return assigneeID;
        }

        public void setAssigneeID(long assigneeID) {
            this.assigneeID = assigneeID;
        }

        public String getAssigneeName() {
            return assigneeName;
        }

        public void setAssigneeName(String assigneeName) {
            this.assigneeName = assigneeName;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

    }



}
