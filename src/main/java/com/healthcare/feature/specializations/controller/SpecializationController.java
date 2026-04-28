package com.healthcare.feature.specializations.controller;

import com.healthcare.feature.specializations.dto.*;
import com.healthcare.feature.specializations.service.SpecializationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/specializations")
public class SpecializationController {

    private final SpecializationService specializationService;

    public SpecializationController(SpecializationService specializationService) {
        this.specializationService = specializationService;
    }

    @GetMapping
    public ResponseEntity<List<SpecializationResponseDTO>> getAll() {
        return ResponseEntity.ok(specializationService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SpecializationResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(specializationService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SpecializationResponseDTO> create(@Valid @RequestBody SpecializationRequestDTO request) {
        return ResponseEntity.ok(specializationService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SpecializationResponseDTO> update(@PathVariable Long id, @Valid @RequestBody SpecializationRequestDTO request) {
        return ResponseEntity.ok(specializationService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        specializationService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
