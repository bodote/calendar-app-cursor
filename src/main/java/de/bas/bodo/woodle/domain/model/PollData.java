package de.bas.bodo.woodle.domain.model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record PollData(
        String name,
        String email,
        String title,
        String description,
        List<EventTimeSlot> timeSlots,
        LocalDate expiryDate,
        List<Participant> participants) {

    public record EventTimeSlot(LocalDate date, LocalTime startTime, LocalTime endTime) {
    }

    public record Participant(String name, List<Integer> selectedTimeSlotIndexes) {
    }
}