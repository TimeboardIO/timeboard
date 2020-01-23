package timeboard.organization;

/*-
 * #%L
 * timesheet
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

import org.springframework.stereotype.Component;
import timeboard.core.api.NavigationExtPoint;

import java.util.Calendar;
import java.util.Date;


@Component
public class TimesheetNavigationProvider implements NavigationExtPoint {

    @Override
    public String getNavigationLabel() {
        return "nav.timesheet";
    }

    @Override
    public String getNavigationParams() {
        final Calendar c = Calendar.getInstance();
        c.setTime(new Date());

        return String.format("week=%s&year=%s", c.get(Calendar.WEEK_OF_YEAR), c.get(Calendar.YEAR));
    }

    @Override
    public String getNavigationPath() {
        return "/timesheet";
    }

    @Override
    public int getNavigationWeight() {
        return 100;
    }

    @Override
    public String getNavigationLogo() {
        return "calendar alternate outline";
    }
}
