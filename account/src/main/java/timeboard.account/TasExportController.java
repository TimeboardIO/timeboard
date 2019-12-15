package timeboard.account;

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
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import timeboard.core.api.ProjectService;
import timeboard.core.model.Account;
import timeboard.core.model.Project;
import timeboard.core.model.TASData;
import timeboard.core.ui.UserInfo;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

//TIME ATTACHMENT SHEET
@Controller
@RequestMapping("/account/exportTAS")
public class TasExportController {

    @Autowired
    private ProjectService projectService;

    @Autowired
    private UserInfo userInfo;

    @PostMapping
    protected String handlePost(HttpServletRequest request, HttpServletResponse response, Model model) throws ServletException, IOException {

        try {
            Account actor = this.userInfo.getCurrentAccount();
            int month = Integer.parseInt(request.getParameter("month"));
            int year = Integer.parseInt(request.getParameter("year"));
            Long projectID = Long.parseLong(request.getParameter("projectID"));

            Calendar cal = Calendar.getInstance();
            cal.set(year, month-1, 1, 2, 0);

            try (ByteArrayOutputStream buf = new ByteArrayOutputStream()) {
                final Project project = projectService.getProjectByID(actor, projectID);
                final TASData data = projectService.generateTasData(actor, project, month, year);
                final ExcelTASReport tasReport = new ExcelTASReport(buf);;
                tasReport.generateFAT(data);

                final String mimeType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
                final String filename = "TAS_"+year+"_"+month+"_"+actor.getScreenName().replaceAll("'| |", "")+"_"+new Date().getTime();
                response.setContentLengthLong(buf.toByteArray().length);
                response.setHeader("Expires:", "0");
                response.setHeader("Content-Disposition", "attachment; filename=" + filename + ".xls");
                response.setContentType(mimeType);
                response.getOutputStream().write(buf.toByteArray());
                response.getOutputStream().flush();
                response.setStatus(201);

            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        return ("/account");

    }


}

