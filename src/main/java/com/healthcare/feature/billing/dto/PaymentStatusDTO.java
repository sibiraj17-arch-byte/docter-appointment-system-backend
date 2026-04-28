package com.healthcare.feature.billing.dto;

public class PaymentStatusDTO {
    private Long appointmentId;
    private Boolean paymentExists;
    private String status;

    public Long getAppointmentId() { return appointmentId; }
    public void setAppointmentId(Long appointmentId) { this.appointmentId = appointmentId; }
    public Boolean getPaymentExists() { return paymentExists; }
    public void setPaymentExists(Boolean paymentExists) { this.paymentExists = paymentExists; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
