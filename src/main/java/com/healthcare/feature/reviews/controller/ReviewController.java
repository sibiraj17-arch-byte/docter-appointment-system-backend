package com.healthcare.feature.reviews.controller;

import com.healthcare.feature.reviews.dto.*;
import com.healthcare.feature.reviews.service.ReviewService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reviews")
@PreAuthorize("hasRole('PATIENT')")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping
    public ResponseEntity<ReviewResponseDTO> create(@Valid @RequestBody ReviewRequestDTO request) {
        return ResponseEntity.ok(reviewService.create(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReviewResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(reviewService.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<ReviewResponseDTO>> getAll() {
        return ResponseEntity.ok(reviewService.getAll());
    }

    @GetMapping("/appointment/{appointmentId}")
    public ResponseEntity<ReviewResponseDTO> getByAppointment(@PathVariable Long appointmentId) {
        return ResponseEntity.ok(reviewService.getByAppointment(appointmentId));
    }

    @GetMapping("/professional/{professionalId}")
    public ResponseEntity<List<ReviewResponseDTO>> getByProfessional(@PathVariable Long professionalId) {
        return ResponseEntity.ok(reviewService.getByProfessional(professionalId));
    }

    @GetMapping("/professional/{professionalId}/rating")
    public ResponseEntity<Double> getAverageRating(@PathVariable Long professionalId) {
        return ResponseEntity.ok(reviewService.getAverageRating(professionalId));
    }

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<ReviewResponseDTO>> getByPatient(@PathVariable Long patientId) {
        return ResponseEntity.ok(reviewService.getByPatient(patientId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ReviewResponseDTO> update(@PathVariable Long id, @Valid @RequestBody ReviewRequestDTO request) {
        return ResponseEntity.ok(reviewService.update(id, request));
    }

    @GetMapping("/check/{appointmentId}")
    public ResponseEntity<Map<String, Boolean>> checkReview(@PathVariable Long appointmentId) {
        Boolean exists = reviewService.checkReview(appointmentId);
        return ResponseEntity.ok(Map.of("reviewExists", exists));
    }

    @GetMapping("/ratings/stats/professional/{professionalId}")
    public ResponseEntity<RatingStatsDTO> getRatingStats(@PathVariable Long professionalId) {
        return ResponseEntity.ok(reviewService.getRatingStats(professionalId));
    }
}
