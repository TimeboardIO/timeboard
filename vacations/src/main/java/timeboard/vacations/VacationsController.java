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
import timeboard.core.TimeboardAuthentication;
import timeboard.core.api.ProjectService;
import timeboard.core.api.UserService;
import timeboard.core.api.exceptions.BusinessException;
import timeboard.core.model.ProjectTag;
import timeboard.core.model.VacationRequest;

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


    @GetMapping
    protected String handleGet(TimeboardAuthentication authentication, Model model) {
        return "vacations.html";
    }


    @GetMapping(value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<VacationRequestWrapper>> listRequests(TimeboardAuthentication authentication) throws BusinessException {

        return ResponseEntity.ok(new ArrayList<VacationRequestWrapper>());
    }

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<VacationRequestWrapper>> createRequest(TimeboardAuthentication authentication,
                                                             @PathVariable Long requestID,
                                                             @ModelAttribute VacationRequestWrapper requestWrapper) throws BusinessException {
        return this.listRequests(authentication) ;
    }


    @PatchMapping(value = "/{requestID}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<VacationRequestWrapper>> patchRequest(TimeboardAuthentication authentication,
                                                            @PathVariable Long requestID,
                                                            @ModelAttribute VacationRequestWrapper requestWrapper) throws BusinessException {
        return this.listRequests(authentication) ;
    }

    @DeleteMapping(value = "/{requestID}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<VacationRequestWrapper>> deleteTag(TimeboardAuthentication authentication,
                                                                  @PathVariable Long requestID) throws BusinessException {
        return this.listRequests(authentication) ;
    }



    public static class VacationRequestWrapper {

        public long id;
        public Date start;
        public Date end;
        public boolean halfStart;
        public boolean halfEnd;
        public long assigneeID;
        public String assigneeName;

        public VacationRequestWrapper(VacationRequest r) {
            this.id = r.getId();
            this.start = r.getStartDate();
            this.end = r.getEndDate();
            this.halfStart = r.getStartHalfDay().equals(VacationRequest.HalfDay.AFTERNOON);
            this.halfEnd = r.getEndHalfDay().equals(VacationRequest.HalfDay.MORNING);
            this.assigneeID = r.getAssignee().getId();
            this.assigneeName = r.getAssignee().getName();
        }


        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }

        public Date getStart() {
            return start;
        }

        public void setStart(Date start) {
            this.start = start;
        }

        public Date getEnd() {
            return end;
        }

        public void setEnd(Date end) {
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
    }



}
