package com.healthcare.feature.billing.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class RazorpayOrderRequestDTO {
    @NotNull(message = "Appointment ID is required")
    private Long appointmentId;

    @Positive(message = "Amount must be positive")
    private Double amount;

    public Long getAppointmentId() { return appointmentId; }
    public void setAppointmentId(Long appointmentId) { this.appointmentId = appointmentId; }
    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }
}
