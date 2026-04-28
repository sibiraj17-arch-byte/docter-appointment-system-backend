package com.healthcare.feature.billing.dto;

import com.healthcare.feature.appointments.dto.AppointmentResponseDTO;

public class BookingPaymentResultDTO {
    private AppointmentResponseDTO appointment;
    private PaymentResponseDTO payment;

    public AppointmentResponseDTO getAppointment() { return appointment; }
    public void setAppointment(AppointmentResponseDTO appointment) { this.appointment = appointment; }
    public PaymentResponseDTO getPayment() { return payment; }
    public void setPayment(PaymentResponseDTO payment) { this.payment = payment; }
}
