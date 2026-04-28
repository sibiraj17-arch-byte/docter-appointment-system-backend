package com.healthcare.feature.appointments.controller;

import com.healthcare.feature.appointments.dto.*;
import com.healthcare.feature.appointments.service.AppointmentService;
import com.healthcare.enums.AppointmentStatus;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/bookings/appointments")
@PreAuthorize("isAuthenticated()")
public class AppointmentController {

    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @PostMapping
    public ResponseEntity<AppointmentResponseDTO> book(@Valid @RequestBody AppointmentRequestDTO request) {
        return ResponseEntity.ok(appointmentService.bookAppointment(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AppointmentResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(appointmentService.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<AppointmentResponseDTO>> getAll() {
        return ResponseEntity.ok(appointmentService.getAll());
    }

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<AppointmentResponseDTO>> getByPatient(@PathVariable Long patientId) {
        return ResponseEntity.ok(appointmentService.getByPatient(patientId));
    }

    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<List<AppointmentResponseDTO>> getByDoctor(@PathVariable Long doctorId) {
        return ResponseEntity.ok(appointmentService.getByDoctor(doctorId));
    }

    @GetMapping("/patient/{patientId}/upcoming")
    public ResponseEntity<List<AppointmentResponseDTO>> getUpcomingByPatient(@PathVariable Long patientId) {
        return ResponseEntity.ok(appointmentService.getUpcomingByPatient(patientId));
    }

    @GetMapping("/doctor/{doctorId}/upcoming")
    public ResponseEntity<List<AppointmentResponseDTO>> getUpcomingByDoctor(@PathVariable Long doctorId) {
        return ResponseEntity.ok(appointmentService.getUpcomingByDoctor(doctorId));
    }

    @GetMapping("/doctor/{doctorId}/today")
    public ResponseEntity<List<AppointmentResponseDTO>> getTodayByDoctor(@PathVariable Long doctorId) {
        return ResponseEntity.ok(appointmentService.getTodayByDoctor(doctorId));
    }

    @GetMapping("/range")
    public ResponseEntity<List<AppointmentResponseDTO>> getByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(appointmentService.getByDateRange(startDate, endDate));
    }

    @GetMapping("/doctor/{doctorId}/date/{date}")
    public ResponseEntity<List<AppointmentResponseDTO>> getByDoctorAndDate(
            @PathVariable Long doctorId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(appointmentService.getByDoctorAndDate(doctorId, date));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<AppointmentResponseDTO> updateStatus(@PathVariable Long id, @RequestParam AppointmentStatus status) {
        return ResponseEntity.ok(appointmentService.updateStatus(id, status));
    }

    @PutMapping("/{id}/reschedule")
    public ResponseEntity<AppointmentResponseDTO> reschedule(@PathVariable Long id, @Valid @RequestBody RescheduleRequestDTO request) {
        return ResponseEntity.ok(appointmentService.reschedule(id, request));
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<AppointmentResponseDTO> cancel(@PathVariable Long id, @RequestBody(required = false) CancelRequestDTO request) {
        if (request == null) request = new CancelRequestDTO();
        return ResponseEntity.ok(appointmentService.cancel(id, request));
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<AppointmentResponseDTO> getByCode(@PathVariable String code) {
        return ResponseEntity.ok(appointmentService.getByCode(code));
    }
}
