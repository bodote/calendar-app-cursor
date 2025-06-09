package de.bas.bodo.woodle.view;

import java.util.ArrayList;
import java.util.List;

public record ScheduleEventStep2Form(List<TimeSlot> timeSlots) {
    public ScheduleEventStep2Form {
        if (timeSlots == null) {
            timeSlots = new ArrayList<>();
        }
    }

    public ScheduleEventStep2Form(String date, String startTime, String endTime) {
        this(new ArrayList<>());
        timeSlots.add(new TimeSlot(date, startTime, endTime));
    }

    public record TimeSlot(String date, String startTime, String endTime) {
    }
}