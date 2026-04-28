package com.healthcare.feature.patients.controller;

import com.healthcare.feature.patients.dto.*;
import com.healthcare.feature.patients.service.PatientService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/patient-profiles")
@PreAuthorize("hasRole('PATIENT')")
public class PatientController {

    private final PatientService patientService;

    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    @PostMapping("/profile")
    public ResponseEntity<PatientResponseDTO> createProfile(@Valid @RequestBody PatientRequestDTO request) {
        return ResponseEntity.ok(patientService.createProfile(request));
    }

    @GetMapping("/profile")
    public ResponseEntity<PatientResponseDTO> getMyProfile() {
        return ResponseEntity.ok(patientService.getMyProfile());
    }

    @PutMapping("/profile")
    public ResponseEntity<PatientResponseDTO> updateProfile(@Valid @RequestBody PatientUpdateDTO request) {
        return ResponseEntity.ok(patientService.updateProfile(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PatientResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(patientService.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<PatientResponseDTO>> getAll() {
        return ResponseEntity.ok(patientService.getAll());
    }
}
