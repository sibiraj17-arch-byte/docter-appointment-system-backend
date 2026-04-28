
package com.healthcare.feature.availability.controller;

import com.healthcare.feature.availability.dto.*;
import com.healthcare.feature.availability.service.AvailabilityService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/availability")
@PreAuthorize("hasRole('DOCTOR')")
public class AvailabilityController {

    private final AvailabilityService availabilityService;

    public AvailabilityController(AvailabilityService availabilityService) {
        this.availabilityService = availabilityService;
    }

    @PutMapping("/{professionalId}")
    public ResponseEntity<Map<String, String>> toggleAvailability(
            @PathVariable Long professionalId, @RequestParam Boolean status) {
        String message = availabilityService.toggleAvailability(professionalId, status);
        return ResponseEntity.ok(Map.of("message", message));
    }

    @GetMapping("/{professionalId}/slots")
    public ResponseEntity<List<TimeSlotDTO>> getSlots(
            @PathVariable Long professionalId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(availabilityService.getSlots(professionalId, date));
    }

    @GetMapping("/{professionalId}/check-slot")
    public ResponseEntity<Map<String, Boolean>> checkSlot(
            @PathVariable Long professionalId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime time) {
        Boolean available = availabilityService.checkSlot(professionalId, date, time);
        return ResponseEntity.ok(Map.of("available", available));
    }

    @GetMapping("/{professionalId}/schedule")
    public ResponseEntity<List<AvailabilityResponseDTO>> getSchedule(@PathVariable Long professionalId) {
        return ResponseEntity.ok(availabilityService.getSchedule(professionalId));
    }

    @PostMapping("/{professionalId}/block-slots")
    public ResponseEntity<Map<String, String>> blockSlots(
            @PathVariable Long professionalId, @RequestBody BlockSlotRequestDTO request) {
        String message = availabilityService.blockSlots(professionalId, request);
        return ResponseEntity.ok(Map.of("message", message));
    }
}
