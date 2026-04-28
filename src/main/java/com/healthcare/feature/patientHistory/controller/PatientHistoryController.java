package com.healthcare.feature.patientHistory.controller;

import com.healthcare.entity.MedicalDocument;
import com.healthcare.feature.patientHistory.dto.*;
import com.healthcare.feature.patientHistory.service.PatientHistoryService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/patient-history")
@PreAuthorize("hasRole('PATIENT')")
public class PatientHistoryController {

    private final PatientHistoryService patientHistoryService;

    public PatientHistoryController(PatientHistoryService patientHistoryService) {
        this.patientHistoryService = patientHistoryService;
    }

    @GetMapping("/{patientId}")
    public ResponseEntity<List<MedicalHistoryResponseDTO>> getByPatient(@PathVariable Long patientId) {
        return ResponseEntity.ok(patientHistoryService.getByPatient(patientId));
    }

    @GetMapping("/{patientId}/range")
    public ResponseEntity<List<MedicalHistoryResponseDTO>> getByPatientAndDateRange(
            @PathVariable Long patientId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        return ResponseEntity.ok(patientHistoryService.getByPatientAndDateRange(patientId, startDate, endDate));
    }

    @GetMapping("/{patientId}/summary")
    public ResponseEntity<MedicalSummaryDTO> getSummary(@PathVariable Long patientId) {
        return ResponseEntity.ok(patientHistoryService.getSummary(patientId));
    }

    @PostMapping
    public ResponseEntity<MedicalHistoryResponseDTO> create(
            @RequestParam Long patientId,
            @Valid @RequestBody MedicalHistoryRequestDTO request) {
        return ResponseEntity.ok(patientHistoryService.create(patientId, request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MedicalHistoryResponseDTO> update(@PathVariable Long id, @Valid @RequestBody MedicalHistoryRequestDTO request) {
        return ResponseEntity.ok(patientHistoryService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> delete(@PathVariable Long id) {
        patientHistoryService.delete(id);
        return ResponseEntity.ok(Map.of("message", "Medical history deleted successfully"));
    }

    @PostMapping("/{patientId}/documents")
    public ResponseEntity<MedicalDocument> uploadDocument(
            @PathVariable Long patientId,
            @RequestParam(required = false) Long historyId,
            @Valid @RequestBody DocumentUploadDTO request) {
        return ResponseEntity.ok(patientHistoryService.uploadDocument(patientId, historyId, request));
    }

    @GetMapping("/{patientId}/documents")
    public ResponseEntity<List<MedicalDocument>> getDocuments(@PathVariable Long patientId) {
        return ResponseEntity.ok(patientHistoryService.getDocuments(patientId));
    }

    @GetMapping("/documents/{documentId}/download")
    public ResponseEntity<Map<String, String>> downloadDocument(@PathVariable Long documentId) {
        String url = patientHistoryService.downloadDocument(documentId);
        return ResponseEntity.ok(Map.of("downloadUrl", url));
    }
}
