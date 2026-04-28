package com.healthcare.feature.search.controller;

import com.healthcare.feature.appointments.dto.AppointmentResponseDTO;
import com.healthcare.enums.AppointmentStatus;
import com.healthcare.feature.doctors.dto.DoctorResponseDTO;
import com.healthcare.feature.patientHistory.dto.MedicalHistoryResponseDTO;
import com.healthcare.feature.prescriptions.dto.PrescriptionResponseDTO;
import com.healthcare.feature.search.service.SearchService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/search")
public class SearchController {

    private final SearchService searchService;

    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping("/professionals")
    public ResponseEntity<List<DoctorResponseDTO>> searchProfessionals(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String specialization,
            @RequestParam(required = false) String role) {
        if (name != null && !name.isBlank()) {
            return ResponseEntity.ok(searchService.searchProfessionalsByName(name));
        } else if (specialization != null && !specialization.isBlank()) {
            return ResponseEntity.ok(searchService.searchProfessionalsBySpecialization(specialization));
        } else if (role != null && !role.isBlank()) {
            return ResponseEntity.ok(searchService.searchProfessionalsByRole(role));
        }
        return ResponseEntity.ok(searchService.searchProfessionalsByName(""));
    }

    @GetMapping("/appointments")
    public ResponseEntity<List<AppointmentResponseDTO>> searchAppointments(
            @RequestParam(required = false) Long patientId,
            @RequestParam(required = false) AppointmentStatus status) {
        return ResponseEntity.ok(searchService.searchAppointments(patientId, status));
    }

    @GetMapping("/prescriptions")
    public ResponseEntity<List<PrescriptionResponseDTO>> searchPrescriptions(
            @RequestParam(required = false) Long patientId,
            @RequestParam(required = false) String dateRange) {
        return ResponseEntity.ok(searchService.searchPrescriptions(patientId, dateRange));
    }

    @GetMapping("/medical-history")
    public ResponseEntity<List<MedicalHistoryResponseDTO>> searchMedicalHistory(
            @RequestParam(required = false) Long patientId,
            @RequestParam(required = false) String keyword) {
        return ResponseEntity.ok(searchService.searchMedicalHistory(patientId, keyword));
    }
}
