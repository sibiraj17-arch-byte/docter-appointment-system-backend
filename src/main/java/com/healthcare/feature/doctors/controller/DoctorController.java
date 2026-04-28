package com.healthcare.feature.doctors.controller;

import com.healthcare.feature.doctors.dto.*;
import com.healthcare.feature.doctors.service.DoctorService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/doctors")
@PreAuthorize("hasRole('DOCTOR')")
public class DoctorController {

    private final DoctorService doctorService;

    public DoctorController(DoctorService doctorService) {
        this.doctorService = doctorService;
    }

    @PostMapping("/profile")
    public ResponseEntity<DoctorResponseDTO> createProfile(@Valid @RequestBody DoctorRequestDTO request) {
        return ResponseEntity.ok(doctorService.createProfile(request));
    }

    @GetMapping("/profile")
    public ResponseEntity<DoctorResponseDTO> getMyProfile() {
        return ResponseEntity.ok(doctorService.getMyProfile());
    }

    @PutMapping("/profile")
    public ResponseEntity<DoctorResponseDTO> updateProfile(@Valid @RequestBody DoctorUpdateDTO request) {
        return ResponseEntity.ok(doctorService.updateProfile(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DoctorResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(doctorService.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<DoctorResponseDTO>> getAll() {
        return ResponseEntity.ok(doctorService.getAll());
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<DoctorResponseDTO> getByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(doctorService.getByUserId(userId));
    }

    @GetMapping("/mobile/{mobileNumber}")
    public ResponseEntity<DoctorResponseDTO> getByMobileNumber(@PathVariable String mobileNumber) {
        return ResponseEntity.ok(doctorService.getByMobileNumber(mobileNumber));
    }
}
