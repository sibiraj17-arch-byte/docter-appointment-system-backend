package com.healthcare.feature.myAppointments.controller;

import com.healthcare.entity.*;
import com.healthcare.enums.AppointmentStatus;
import com.healthcare.exception.ResourceNotFoundException;
import com.healthcare.feature.myAppointments.dto.MyAppointmentResponseDTO;
import com.healthcare.feature.appointments.repository.AppointmentRepository;
import com.healthcare.feature.patients.repository.PatientRepository;
import com.healthcare.feature.billing.repository.PaymentRepository;
import com.healthcare.security.CustomUserPrincipal;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/my-appointments")
@PreAuthorize("hasRole('PATIENT')")
@Transactional(readOnly = true)
public class MyAppointmentsController {

    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final PaymentRepository paymentRepository;

    public MyAppointmentsController(AppointmentRepository appointmentRepository,
                                     PatientRepository patientRepository,
                                     PaymentRepository paymentRepository) {
        this.appointmentRepository = appointmentRepository;
        this.patientRepository = patientRepository;
        this.paymentRepository = paymentRepository;
    }

    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomUserPrincipal) {
            return ((CustomUserPrincipal) auth.getPrincipal()).getUserId();
        }
        throw new com.healthcare.exception.UnauthorizedException("Not authenticated");
    }

    private Patient getCurrentPatient() {
        Long userId = getCurrentUserId();
        return patientRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient profile", "userId", userId));
    }

    private MyAppointmentResponseDTO toDTO(Appointment apt) {
        if (apt == null) {
            return null;
        }
        
        MyAppointmentResponseDTO dto = new MyAppointmentResponseDTO();
        dto.setId(apt.getId());
        dto.setCode(apt.getCode());
        
        // Safely handle doctor information with null checks
        Doctor doctor = apt.getDoctor();
        if (doctor != null) {
            dto.setDoctorId(doctor.getId());
            
            User doctorUser = doctor.getUser();
            if (doctorUser != null) {
                dto.setDoctorName(doctorUser.getName());
            }
            
            Specialization specialization = doctor.getSpecialization();
            if (specialization != null) {
                dto.setSpecialization(specialization.getName());
            }
            
            if (doctor.getQualification() != null) {
                dto.setDoctorQualification(doctor.getQualification());
            }
            if (doctor.getConsultationFee() != null) {
                dto.setConsultationFee(doctor.getConsultationFee());
            }
        }
        
        dto.setAppointmentDate(apt.getAppointmentDate());
        dto.setStartTime(apt.getStartTime());
        dto.setEndTime(apt.getEndTime());
        dto.setStatus(apt.getStatus());
        dto.setReason(apt.getReason());
        dto.setSymptoms(apt.getSymptoms());
        dto.setNotes(apt.getNotes());
        
        // Safely handle related objects
        Prescription prescription = apt.getPrescription();
        dto.setHasPrescription(prescription != null);
        
        Review review = apt.getReview();
        dto.setHasReview(review != null);
        
        Payment payment = apt.getPayment();
        dto.setHasPayment(payment != null);
        dto.setPaymentStatus(payment != null ? payment.getStatus().name() : "NOT_INITIATED");
        
        dto.setCreatedAt(apt.getCreatedAt());
        return dto;
    }

    @GetMapping
    public ResponseEntity<List<MyAppointmentResponseDTO>> getAll() {
        Patient patient = getCurrentPatient();
        List<Appointment> appointments = appointmentRepository.findByPatientId(patient.getId());
        return ResponseEntity.ok(appointments.stream().map(this::toDTO).collect(Collectors.toList()));
    }

    @GetMapping("/upcoming")
    public ResponseEntity<List<MyAppointmentResponseDTO>> getUpcoming() {
        Patient patient = getCurrentPatient();
        List<Appointment> appointments = appointmentRepository.findUpcomingByPatient(patient.getId(), LocalDate.now());
        return ResponseEntity.ok(appointments.stream().map(this::toDTO).collect(Collectors.toList()));
    }

    @GetMapping("/past")
    public ResponseEntity<List<MyAppointmentResponseDTO>> getPast() {
        Patient patient = getCurrentPatient();
        List<Appointment> all = appointmentRepository.findByPatientId(patient.getId());
        List<Appointment> past = all.stream()
                .filter(a -> a.getAppointmentDate().isBefore(LocalDate.now()) || 
                             (a.getAppointmentDate().isEqual(LocalDate.now()) && a.getEndTime().isBefore(java.time.LocalTime.now())))
                .collect(Collectors.toList());
        return ResponseEntity.ok(past.stream().map(this::toDTO).collect(Collectors.toList()));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<MyAppointmentResponseDTO>> getByStatus(@PathVariable AppointmentStatus status) {
        Patient patient = getCurrentPatient();
        List<Appointment> appointments = appointmentRepository.findByPatientIdAndStatus(patient.getId(), status);
        return ResponseEntity.ok(appointments.stream().map(this::toDTO).collect(Collectors.toList()));
    }

    @GetMapping("/date/{date}")
    public ResponseEntity<List<MyAppointmentResponseDTO>> getByDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        Patient patient = getCurrentPatient();
        List<Appointment> all = appointmentRepository.findByPatientId(patient.getId());
        List<Appointment> filtered = all.stream()
                .filter(a -> a.getAppointmentDate().equals(date))
                .collect(Collectors.toList());
        return ResponseEntity.ok(filtered.stream().map(this::toDTO).collect(Collectors.toList()));
    }

    @GetMapping("/{appointmentId}")
    public ResponseEntity<MyAppointmentResponseDTO> getById(@PathVariable Long appointmentId) {
        Patient patient = getCurrentPatient();
        Appointment apt = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment", "id", appointmentId));
        if (!apt.getPatient().getId().equals(patient.getId())) {
            throw new com.healthcare.exception.UnauthorizedException("You can only view your own appointments");
        }
        return ResponseEntity.ok(toDTO(apt));
    }

    @GetMapping("/history")
    public ResponseEntity<List<MyAppointmentResponseDTO>> getHistory() {
        Patient patient = getCurrentPatient();
        List<Appointment> all = appointmentRepository.findByPatientId(patient.getId());
        List<Appointment> history = all.stream()
                .filter(a -> a.getStatus() == AppointmentStatus.COMPLETED || a.getStatus() == AppointmentStatus.CANCELLED)
                .collect(Collectors.toList());
        return ResponseEntity.ok(history.stream().map(this::toDTO).collect(Collectors.toList()));
    }

    @GetMapping("/count")
    public ResponseEntity<Map<String, Long>> getCount() {
        Patient patient = getCurrentPatient();
        List<Appointment> all = appointmentRepository.findByPatientId(patient.getId());
        long total = all.size();
        long upcoming = all.stream()
                .filter(a -> a.getAppointmentDate().isAfter(LocalDate.now()) || 
                             (a.getAppointmentDate().isEqual(LocalDate.now()) && a.getStartTime().isAfter(java.time.LocalTime.now())))
                .filter(a -> a.getStatus() != AppointmentStatus.CANCELLED && a.getStatus() != AppointmentStatus.COMPLETED)
                .count();
        long completed = all.stream().filter(a -> a.getStatus() == AppointmentStatus.COMPLETED).count();
        long cancelled = all.stream().filter(a -> a.getStatus() == AppointmentStatus.CANCELLED).count();
        return ResponseEntity.ok(Map.of("total", total, "upcoming", upcoming, "completed", completed, "cancelled", cancelled));
    }

    @GetMapping("/timeline")
    public ResponseEntity<List<MyAppointmentResponseDTO>> getTimeline() {
        Patient patient = getCurrentPatient();
        List<Appointment> all = appointmentRepository.findByPatientId(patient.getId());
        List<Appointment> sorted = all.stream()
                .sorted((a, b) -> {
                    int dateCompare = b.getAppointmentDate().compareTo(a.getAppointmentDate());
                    if (dateCompare != 0) return dateCompare;
                    return b.getStartTime().compareTo(a.getStartTime());
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(sorted.stream().map(this::toDTO).collect(Collectors.toList()));
    }

    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<List<MyAppointmentResponseDTO>> getByDoctor(@PathVariable Long doctorId) {
        Patient patient = getCurrentPatient();
        List<Appointment> all = appointmentRepository.findByPatientId(patient.getId());
        List<Appointment> filtered = all.stream()
                .filter(a -> a.getDoctor().getId().equals(doctorId))
                .collect(Collectors.toList());
        return ResponseEntity.ok(filtered.stream().map(this::toDTO).collect(Collectors.toList()));
    }
}
