package timeboard.home;

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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import timeboard.core.TimeboardAuthentication;
import timeboard.core.api.ProjectService;
import timeboard.core.api.TimesheetService;
import timeboard.core.model.Account;
import timeboard.home.model.Week;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


@Controller
@RequestMapping(HomeController.URI)
public class HomeController {

    public static final String URI = "/home";

    @Autowired
    private ProjectService projectService;

    @Autowired
    private TimesheetService timesheetService;

    @PostMapping
    protected String handlePost() {
        return "home.html";
    }

    @GetMapping
    protected String handleGet(TimeboardAuthentication authentication, Model model) {


        //load previous weeks data
        Date d = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(d);
        List<Week> weeks = new ArrayList<>();
        final Account account = authentication.getDetails();
        int weeksToDisplay = 3; // actual week and the two previous ones
        if (this.timesheetService != null) {
            for (int i = 0; i < weeksToDisplay; i++) {
                boolean weekIsValidated = timesheetService.isTimesheetValidated(
                        account,
                        calendar.get(Calendar.YEAR), calendar.get(Calendar.WEEK_OF_YEAR));

                calendar.set(Calendar.DAY_OF_WEEK, 2); // Monday
                Date firstDayOfWeek = calendar.getTime();
                calendar.set(Calendar.DAY_OF_WEEK, 1); // Sunday
                Date lastDayOfWeek = calendar.getTime();
                Double weekSum = this.timesheetService.getSumImputationForWeek(firstDayOfWeek, lastDayOfWeek, account);

                Week week = new Week(calendar.get(Calendar.WEEK_OF_YEAR), calendar.get(Calendar.YEAR), weekSum, weekIsValidated);
                weeks.add(week);
                calendar.roll(Calendar.WEEK_OF_YEAR, -1);
            }
        }

        model.addAttribute("nb_projects", this.projectService.listProjects(account, authentication.getCurrentOrganization()).size());
        model.addAttribute("nb_tasks", this.projectService.listUserTasks(account).size());
        model.addAttribute("weeks", weeks);

        return "home.html";
    }


}
