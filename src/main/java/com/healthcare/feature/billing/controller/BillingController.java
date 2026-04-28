package com.healthcare.feature.billing.controller;

import com.healthcare.feature.billing.dto.*;
import com.healthcare.feature.billing.service.PaymentService;
import com.healthcare.enums.PaymentStatus;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/billing")
@PreAuthorize("hasRole('PATIENT')")
public class BillingController {

    private final PaymentService paymentService;

    public BillingController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/payments")
    public ResponseEntity<PaymentResponseDTO> createPayment(@Valid @RequestBody PaymentRequestDTO request) {
        return ResponseEntity.ok(paymentService.createPayment(request));
    }

    @PostMapping("/payments/razorpay/order")
    public ResponseEntity<RazorpayOrderResponseDTO> createRazorpayOrder(@Valid @RequestBody RazorpayOrderRequestDTO request) {
        return ResponseEntity.ok(paymentService.createRazorpayOrder(request));
    }

    @PostMapping("/payments/razorpay/verify")
    public ResponseEntity<PaymentResponseDTO> verifyRazorpayPayment(@Valid @RequestBody RazorpayVerificationRequestDTO request) {
        return ResponseEntity.ok(paymentService.verifyRazorpayPayment(request));
    }

    @PostMapping("/payments/razorpay/booking/order")
    public ResponseEntity<RazorpayOrderResponseDTO> createBookingRazorpayOrder(@Valid @RequestBody BookingRazorpayOrderRequestDTO request) {
        return ResponseEntity.ok(paymentService.createBookingRazorpayOrder(request));
    }

    @PostMapping("/payments/razorpay/booking/verify")
    public ResponseEntity<BookingPaymentResultDTO> verifyBookingRazorpayPayment(@Valid @RequestBody BookingRazorpayVerifyRequestDTO request) {
        return ResponseEntity.ok(paymentService.verifyBookingRazorpayPayment(request));
    }

    @GetMapping("/payments/{id}")
    public ResponseEntity<PaymentResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(paymentService.getById(id));
    }

    @GetMapping("/payments")
    public ResponseEntity<List<PaymentResponseDTO>> getAll() {
        return ResponseEntity.ok(paymentService.getAll());
    }

    @GetMapping("/payments/appointment/{appointmentId}")
    public ResponseEntity<PaymentResponseDTO> getByAppointment(@PathVariable Long appointmentId) {
        return ResponseEntity.ok(paymentService.getByAppointment(appointmentId));
    }

    @GetMapping("/payments/patient/{patientId}")
    public ResponseEntity<List<PaymentResponseDTO>> getByPatient(@PathVariable Long patientId) {
        return ResponseEntity.ok(paymentService.getByPatient(patientId));
    }

    @GetMapping("/payments/status/{status}")
    public ResponseEntity<List<PaymentResponseDTO>> getByStatus(@PathVariable PaymentStatus status) {
        return ResponseEntity.ok(paymentService.getByStatus(status));
    }

    @GetMapping("/payments/check/{appointmentId}")
    public ResponseEntity<PaymentStatusDTO> checkPayment(@PathVariable Long appointmentId) {
        return ResponseEntity.ok(paymentService.checkPayment(appointmentId));
    }
}
