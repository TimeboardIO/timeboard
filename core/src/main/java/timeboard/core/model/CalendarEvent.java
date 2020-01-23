package timeboard.core.model;

/*-
 * #%L
 * core
 * %%
 * Copyright (C) 2019 - 2020 Timeboard
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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

public class CalendarEvent {

    private String name;
    private String date;
    private double value;
    private int type; // 0 MORNING - 1 FULL DAY - 2 AFTERNOON

    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    public CalendarEvent() { }

    public CalendarEvent(Imputation imputation) {
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

    public static List<CalendarEvent> requestToWrapperList(List<VacationRequest> requests) {
        final List<CalendarEvent> results = new ArrayList<>();

        for (VacationRequest r : requests) {
            results.addAll(requestToWrapper(r));
        }

        return results;
    }


    public static List<CalendarEvent> requestToWrapper(VacationRequest request) {
        final LinkedList<CalendarEvent> results = new LinkedList<>();

        final java.util.Calendar start = java.util.Calendar.getInstance();
        final java.util.Calendar end = java.util.Calendar.getInstance();

        start.setTime(request.getStartDate());
        end.setTime(request.getEndDate());
        boolean last = true;
        while(last) {
            final CalendarEvent wrapper = new CalendarEvent();

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
    

}
