package com.healthcare.feature.admin.controller;

import com.healthcare.feature.admin.dto.*;
import com.healthcare.feature.admin.service.AdminService;
import com.healthcare.entity.User;
import com.healthcare.feature.doctors.dto.DoctorResponseDTO;
import com.healthcare.feature.patients.dto.PatientResponseDTO;
import com.healthcare.feature.specializations.dto.SpecializationRequestDTO;
import com.healthcare.feature.specializations.dto.SpecializationResponseDTO;
import com.healthcare.feature.specializations.service.SpecializationService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;
    private final SpecializationService specializationService;

    public AdminController(AdminService adminService, SpecializationService specializationService) {
        this.adminService = adminService;
        this.specializationService = specializationService;
    }

    @GetMapping("/professionals")
    public ResponseEntity<List<DoctorResponseDTO>> getAllProfessionals() {
        return ResponseEntity.ok(adminService.getAllDoctors());
    }

    @GetMapping("/patients")
    public ResponseEntity<List<PatientResponseDTO>> getAllPatients() {
        return ResponseEntity.ok(adminService.getAllPatients());
    }

    @PostMapping("/doctors")
    public ResponseEntity<DoctorResponseDTO> createDoctor(@RequestBody Map<String, Object> request) {
        DoctorResponseDTO dto = adminService.createDoctor(
                (String) request.get("name"),
                (String) request.get("mobileNumber"),
                (String) request.get("email"),
                request.get("specializationId") != null ? Long.valueOf(request.get("specializationId").toString()) : null,
                (String) request.get("qualification"),
                request.get("experienceYears") != null ? Integer.valueOf(request.get("experienceYears").toString()) : null,
                request.get("consultationFee") != null ? Double.valueOf(request.get("consultationFee").toString()) : null
        );
        return ResponseEntity.ok(dto);
    }

    @PutMapping("/doctors/{id}")
    public ResponseEntity<DoctorResponseDTO> updateDoctor(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        DoctorResponseDTO dto = adminService.updateDoctor(
                id,
                (String) request.get("name"),
                (String) request.get("email"),
                request.get("specializationId") != null ? Long.valueOf(request.get("specializationId").toString()) : null,
                (String) request.get("qualification"),
                request.get("experienceYears") != null ? Integer.valueOf(request.get("experienceYears").toString()) : null,
                request.get("consultationFee") != null ? Double.valueOf(request.get("consultationFee").toString()) : null,
                request.get("isVerified") != null ? Boolean.valueOf(request.get("isVerified").toString()) : null
        );
        return ResponseEntity.ok(dto);
    }

    @DeleteMapping("/doctors/{id}")
    public ResponseEntity<Map<String, String>> deleteDoctor(@PathVariable Long id) {
        adminService.deleteDoctor(id);
        return ResponseEntity.ok(Map.of("message", "Doctor deleted successfully"));
    }

    @PostMapping("/admins")
    public ResponseEntity<User> createAdmin(@RequestBody Map<String, String> request) {
        User user = adminService.createAdmin(request.get("name"), request.get("mobileNumber"), request.get("email"));
        return ResponseEntity.ok(user);
    }

    @PutMapping("/admins/{id}")
    public ResponseEntity<User> updateAdmin(@PathVariable Long id, @RequestBody Map<String, String> request) {
        User user = adminService.updateAdmin(id, request.get("name"), request.get("email"));
        return ResponseEntity.ok(user);
    }

    @DeleteMapping("/admins/{id}")
    public ResponseEntity<Map<String, String>> deleteAdmin(@PathVariable Long id) {
        adminService.deleteAdmin(id);
        return ResponseEntity.ok(Map.of("message", "Admin deleted successfully"));
    }

    @GetMapping("/dashboard/stats")
    public ResponseEntity<DashboardStatsDTO> getDashboardStats() {
        return ResponseEntity.ok(adminService.getDashboardStats());
    }

    @GetMapping("/reports/revenue")
    public ResponseEntity<RevenueReportDTO> getRevenueReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(adminService.getRevenueReport(startDate, endDate));
    }

    @GetMapping("/reports/appointments")
    public ResponseEntity<Object> getAppointmentsReport() {
        return ResponseEntity.ok(adminService.getAppointmentsReport());
    }

    @GetMapping("/analytics/users")
    public ResponseEntity<UserAnalyticsDTO> getUserAnalytics() {
        return ResponseEntity.ok(adminService.getUserAnalytics());
    }
}
