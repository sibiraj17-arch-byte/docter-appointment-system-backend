package com.healthcare.feature.medicalReports.controller;

import com.healthcare.feature.medicalReports.dto.*;
import com.healthcare.feature.medicalReports.service.MedicalReportService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/medical-reports")
@PreAuthorize("hasRole('DOCTOR')")
public class MedicalReportController {

    private final MedicalReportService medicalReportService;

    public MedicalReportController(MedicalReportService medicalReportService) {
        this.medicalReportService = medicalReportService;
    }

    @PostMapping
    public ResponseEntity<MedicalReportResponseDTO> create(@Valid @RequestBody MedicalReportRequestDTO request) {
        return ResponseEntity.ok(medicalReportService.create(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MedicalReportResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(medicalReportService.getById(id));
    }

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<MedicalReportResponseDTO>> getByPatient(@PathVariable Long patientId) {
        return ResponseEntity.ok(medicalReportService.getByPatient(patientId));
    }

    @GetMapping("/patient/{patientId}/range")
    public ResponseEntity<List<MedicalReportResponseDTO>> getByPatientAndDateRange(
            @PathVariable Long patientId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return ResponseEntity.ok(medicalReportService.getByPatientAndDateRange(patientId, start, end));
    }

    @GetMapping("/{id}/export-pdf")
    public ResponseEntity<Map<String, String>> exportPdf(@PathVariable Long id) {
        String result = medicalReportService.exportPdf(id);
        return ResponseEntity.ok(Map.of("message", result));
    }

    @GetMapping("/{id}/export-excel")
    public ResponseEntity<Map<String, String>> exportExcel(@PathVariable Long id) {
        String result = medicalReportService.exportExcel(id);
        return ResponseEntity.ok(Map.of("message", result));
    }
}
