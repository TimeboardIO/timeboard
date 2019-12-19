package timeboard.reports;

/*-
 * #%L
 * reports
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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import timeboard.core.api.ReportService;
import timeboard.core.api.UserService;
import timeboard.core.model.Account;
import timeboard.core.ui.UserInfo;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Set;


@Component
@RestController
@RequestMapping(value = "/api/reports", produces = MediaType.APPLICATION_JSON_VALUE)
public class ReportsRestAPI {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Autowired
    private UserInfo userInfo;

    @Autowired
    private UserService userService;

    @Autowired
    private ReportService reportsService;


    @PostMapping(value = "/refreshProjectSelection", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity refreshProjectSelection(@RequestBody String filterProjects, HttpServletRequest request, Model model)
            throws JsonProcessingException {
        final Account actor = this.userInfo.getCurrentAccount();

        final String[] filters = filterProjects.split("\n");
        final Set<ReportService.ProjectWrapper> projects = this.reportsService.findProjects(actor, Arrays.asList(filters));

        return ResponseEntity.status(HttpStatus.OK).body(MAPPER.writeValueAsString(projects));
    }






}
