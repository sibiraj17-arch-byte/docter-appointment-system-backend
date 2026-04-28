package com.healthcare.feature.availability.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class BlockSlotRequestDTO {
    @NotNull
    private LocalDate date;
    
    private List<TimeRange> slots;

    public static class TimeRange {
        @NotNull
        private LocalTime startTime;
        @NotNull
        private LocalTime endTime;

        public LocalTime getStartTime() { return startTime; }
        public void setStartTime(LocalTime startTime) { this.startTime = startTime; }
        public LocalTime getEndTime() { return endTime; }
        public void setEndTime(LocalTime endTime) { this.endTime = endTime; }
    }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    public List<TimeRange> getSlots() { return slots; }
    public void setSlots(List<TimeRange> slots) { this.slots = slots; }
}
