package com.healthcare.feature.prescriptions.controller;

import com.healthcare.feature.prescriptions.dto.*;
import com.healthcare.feature.prescriptions.service.PrescriptionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/prescriptions")
@PreAuthorize("isAuthenticated()")
public class PrescriptionController {

    private final PrescriptionService prescriptionService;

    public PrescriptionController(PrescriptionService prescriptionService) {
        this.prescriptionService = prescriptionService;
    }

    @PostMapping
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<PrescriptionResponseDTO> create(@Valid @RequestBody PrescriptionRequestDTO request) {
        return ResponseEntity.ok(prescriptionService.create(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PrescriptionResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(prescriptionService.getById(id));
    }

    @GetMapping
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<List<PrescriptionResponseDTO>> getAll() {
        return ResponseEntity.ok(prescriptionService.getAll());
    }

    @GetMapping("/appointment/{appointmentId}")
    public ResponseEntity<PrescriptionResponseDTO> getByAppointment(@PathVariable Long appointmentId) {
        return ResponseEntity.ok(prescriptionService.getByAppointmentId(appointmentId));
    }

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<PrescriptionResponseDTO>> getByPatient(@PathVariable Long patientId) {
        return ResponseEntity.ok(prescriptionService.getByPatient(patientId));
    }

    @GetMapping("/doctor/{doctorId}")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<List<PrescriptionResponseDTO>> getByDoctor(@PathVariable Long doctorId) {
        return ResponseEntity.ok(prescriptionService.getByDoctor(doctorId));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<PrescriptionResponseDTO> update(@PathVariable Long id, @Valid @RequestBody PrescriptionRequestDTO request) {
        return ResponseEntity.ok(prescriptionService.update(id, request));
    }

    @GetMapping("/{id}/pdf")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<Map<String, String>> generatePdf(@PathVariable Long id) {
        String result = prescriptionService.generatePdf(id);
        return ResponseEntity.ok(Map.of("message", result));
    }

    @PostMapping("/{id}/send-email")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<Map<String, String>> sendEmail(@PathVariable Long id) {
        String result = prescriptionService.sendEmail(id);
        return ResponseEntity.ok(Map.of("message", result));
    }
}
