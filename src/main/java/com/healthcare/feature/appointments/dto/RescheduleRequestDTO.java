package com.healthcare.feature.appointments.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;

public class RescheduleRequestDTO {

    @NotNull
    @FutureOrPresent(message = "New date must be today or in the future")
    private LocalDate newDate;

    @NotNull
    private LocalTime newStartTime;

    public LocalDate getNewDate() { return newDate; }
    public void setNewDate(LocalDate newDate) { this.newDate = newDate; }

    public LocalTime getNewStartTime() { return newStartTime; }
    public void setNewStartTime(LocalTime newStartTime) { this.newStartTime = newStartTime; }
}
