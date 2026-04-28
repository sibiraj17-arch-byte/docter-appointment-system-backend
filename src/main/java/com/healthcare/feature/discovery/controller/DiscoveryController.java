package com.healthcare.feature.discovery.controller;

import com.healthcare.feature.availability.dto.TimeSlotDTO;
import com.healthcare.feature.discovery.service.DiscoveryService;
import com.healthcare.feature.doctors.dto.DoctorResponseDTO;
import com.healthcare.feature.reviews.dto.ReviewResponseDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/discovery")
public class DiscoveryController {

    private final DiscoveryService discoveryService;

    public DiscoveryController(DiscoveryService discoveryService) {
        this.discoveryService = discoveryService;
    }

    @GetMapping("/professionals")
    public ResponseEntity<List<DoctorResponseDTO>> getAllProfessionals() {
        return ResponseEntity.ok(discoveryService.getAllDoctors());
    }

    @GetMapping("/professionals/{id}")
    public ResponseEntity<DoctorResponseDTO> getProfessionalById(@PathVariable Long id) {
        return ResponseEntity.ok(discoveryService.getDoctorById(id));
    }

    @GetMapping("/professionals/specialization/{specializationId}")
    public ResponseEntity<List<DoctorResponseDTO>> getBySpecialization(@PathVariable Long specializationId) {
        return ResponseEntity.ok(discoveryService.getBySpecialization(specializationId));
    }

    @GetMapping("/professionals/available/all")
    public ResponseEntity<List<DoctorResponseDTO>> getAvailableProfessionals() {
        return ResponseEntity.ok(discoveryService.getAvailableDoctors());
    }

    @GetMapping("/professionals/available/specialization/{specializationId}")
    public ResponseEntity<List<DoctorResponseDTO>> getAvailableBySpecialization(@PathVariable Long specializationId) {
        return ResponseEntity.ok(discoveryService.getAvailableBySpecialization(specializationId));
    }

    @GetMapping("/professionals/{id}/availability")
    public ResponseEntity<List<TimeSlotDTO>> getProfessionalAvailability(@PathVariable Long id) {
        return ResponseEntity.ok(discoveryService.getDoctorAvailability(id));
    }

    @GetMapping("/professionals/{id}/rating")
    public ResponseEntity<Double> getProfessionalRating(@PathVariable Long id) {
        return ResponseEntity.ok(discoveryService.getProfessionalRating(id));
    }

    @GetMapping("/professionals/{id}/reviews")
    public ResponseEntity<List<ReviewResponseDTO>> getProfessionalReviews(@PathVariable Long id) {
        return ResponseEntity.ok(discoveryService.getProfessionalReviews(id));
    }
}
