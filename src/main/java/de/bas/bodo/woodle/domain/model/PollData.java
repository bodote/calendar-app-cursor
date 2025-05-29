package de.bas.bodo.woodle.domain.model;

import java.time.LocalDate;
import java.time.LocalTime;

public record PollData(
        String name,
        String email,
        String title,
        String description,
        LocalDate date,
        LocalTime startTime,
        LocalTime endTime,
        LocalDate expiryDate) {
}