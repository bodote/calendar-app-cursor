package de.bas.bodo.woodle.model;

import java.time.LocalDate;
import java.time.LocalTime;

public record EventData(
        String name,
        String email,
        String title,
        String description,
        LocalDate date,
        LocalTime startTime,
        LocalTime endTime,
        LocalDate expiryDate) {
}