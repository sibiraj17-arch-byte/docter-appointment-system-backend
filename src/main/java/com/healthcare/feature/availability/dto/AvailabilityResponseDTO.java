package com.healthcare.feature.availability.dto;

import java.time.LocalDate;
import java.util.List;

public class AvailabilityResponseDTO {
    private LocalDate date;
    private List<TimeSlotDTO> slots;

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    public List<TimeSlotDTO> getSlots() { return slots; }
    public void setSlots(List<TimeSlotDTO> slots) { this.slots = slots; }
}
