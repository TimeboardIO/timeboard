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

import com.fasterxml.jackson.annotation.JsonFormat;

import java.io.Serializable;
import java.util.Calendar;
import java.util.*;
import java.util.stream.Collectors;


public class CalendarEvent implements Serializable {

    private String name;
    private String label;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private Date date;
    private double value;
    private int type; // 0 MORNING - 1 FULL DAY - 2 AFTERNOON

    public CalendarEvent() {
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
        while (last) {
            final CalendarEvent wrapper = new CalendarEvent();

            wrapper.setName(request.getApplicant().getScreenName());
            wrapper.setLabel(request.getLabel());
            wrapper.setDate(start.getTime());
            if (request.getStatus() == VacationRequestStatus.ACCEPTED) {
                wrapper.setValue(1);
            } else if (request.getStatus() == VacationRequestStatus.PENDING) {
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

    public static List<CalendarEvent> batchListToWrapperList(List<Batch> batches) {

        final List<CalendarEvent> wrapperList = new ArrayList<>();

        final Set<Batch> copy = new HashSet<>(batches);

        for (Batch batch : batches) {

            if(copy.contains(batch)) {
                copy.remove(batch);
                String label = batch.getName();
                final List<Batch> sameDayBatches = copy.stream().filter(other -> other.getDate().compareTo(batch.getDate()) == 0).collect(Collectors.toList());
                final CalendarEvent wrapper = new CalendarEvent();
                if(!sameDayBatches.isEmpty()) {
                    label += sameDayBatches.stream().map(Batch::getName).reduce("", (b, currentStr) -> b += " - " + currentStr);
                    copy.removeAll(sameDayBatches);
                }
                wrapper.setName("project");
                wrapper.setType(1); // FULL DAY
                wrapper.setValue(2); // BATCH
                wrapper.setLabel(label);
                wrapper.setDate(batch.getDate());

                wrapperList.add(wrapper);
            }
        }

        return wrapperList;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }


}
