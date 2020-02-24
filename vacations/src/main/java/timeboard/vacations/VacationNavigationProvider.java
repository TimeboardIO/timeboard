package timeboard.vacations;

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
import timeboard.core.api.AbacEntries;
import timeboard.core.api.NavigationExtPoint;


@Component
public class VacationNavigationProvider implements NavigationExtPoint {

    @Override
    public String getNavigationLabel() {
        return "nav.vacation";
    }

    @Override
    public String getNavigationPath() {
        return "/vacation";
    }

    @Override
    public String getNavigationAction() {
        return AbacEntries.VACATION_VIEW;
    }

    @Override
    public int getNavigationWeight() {
        return 6000;
    }

    @Override
    public String getNavigationLogo() {
        return "suitcase";
    }
}
