package com.healthcare.feature.appointments.dto;

import jakarta.validation.constraints.Size;

public class CancelRequestDTO {
    @Size(max = 500)
    private String reason;

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
