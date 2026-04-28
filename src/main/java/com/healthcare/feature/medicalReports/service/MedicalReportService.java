package com.healthcare.feature.medicalReports.service;

import com.healthcare.entity.Appointment;
import com.healthcare.entity.Doctor;
import com.healthcare.entity.MedicalReport;
import com.healthcare.entity.Patient;
import com.healthcare.exception.ResourceNotFoundException;
import com.healthcare.feature.medicalReports.dto.MedicalReportRequestDTO;
import com.healthcare.feature.medicalReports.dto.MedicalReportResponseDTO;
import com.healthcare.feature.appointments.repository.AppointmentRepository;
import com.healthcare.feature.medicalReports.repository.MedicalReportRepository;
import com.healthcare.feature.patients.repository.PatientRepository;
import com.healthcare.feature.doctors.repository.DoctorRepository;
import com.healthcare.security.CustomUserPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MedicalReportService {

    private final MedicalReportRepository medicalReportRepository;
    private final AppointmentRepository appointmentRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;

    public MedicalReportService(MedicalReportRepository medicalReportRepository,
                                AppointmentRepository appointmentRepository,
                                DoctorRepository doctorRepository,
                                PatientRepository patientRepository) {
        this.medicalReportRepository = medicalReportRepository;
        this.appointmentRepository = appointmentRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
    }

    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomUserPrincipal principal) {
            return principal.getUserId();
        }
        throw new com.healthcare.exception.UnauthorizedException("Not authenticated");
    }

    public MedicalReportResponseDTO create(MedicalReportRequestDTO request) {
        Long userId = getCurrentUserId();
        Doctor doctor = doctorRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor profile", "userId", userId));

        Appointment appointment = null;
        Patient patient;

        if (request.getAppointmentId() != null) {
            appointment = appointmentRepository.findById(request.getAppointmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Appointment", "id", request.getAppointmentId()));
            if (!appointment.getDoctor().getId().equals(doctor.getId())) {
                throw new com.healthcare.exception.UnauthorizedException(
                        "You can only create reports for your own appointments");
            }
            patient = appointment.getPatient();
        } else if (request.getPatientId() != null) {
            patient = patientRepository.findById(request.getPatientId())
                    .orElseThrow(() -> new ResourceNotFoundException("Patient", "id", request.getPatientId()));
        } else {
            throw new ResourceNotFoundException("Either appointmentId or patientId is required");
        }

        MedicalReport report = new MedicalReport();
        report.setPatient(patient);
        report.setDoctor(doctor);
        report.setAppointment(appointment);
        report.setReportType(request.getReportType());
        report.setTitle(request.getTitle());
        report.setDescription(request.getDescription());
        report.setFindings(request.getFindings());
        report.setRecommendations(request.getRecommendations());

        return toDTO(medicalReportRepository.save(report));
    }

    public MedicalReportResponseDTO getById(Long id) {
        return toDTO(medicalReportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MedicalReport", "id", id)));
    }

    public List<MedicalReportResponseDTO> getByPatient(Long patientId) {
        return medicalReportRepository.findByPatientId(patientId).stream()
                .map(this::toDTO).collect(Collectors.toList());
    }

    public List<MedicalReportResponseDTO> getByPatientAndDateRange(Long patientId, LocalDateTime start, LocalDateTime end) {
        return medicalReportRepository.findByPatientIdAndDateRange(patientId, start, end).stream()
                .map(this::toDTO).collect(Collectors.toList());
    }

    public String exportPdf(Long id) {
        MedicalReport report = medicalReportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MedicalReport", "id", id));
        return "PDF export for report: " + report.getTitle() + " - Patient: " + report.getPatient().getUser().getName();
    }

    public String exportExcel(Long id) {
        MedicalReport report = medicalReportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MedicalReport", "id", id));
        return "Excel export for report: " + report.getTitle() + " - Patient: " + report.getPatient().getUser().getName();
    }

    private MedicalReportResponseDTO toDTO(MedicalReport r) {
        MedicalReportResponseDTO dto = new MedicalReportResponseDTO();
        dto.setId(r.getId());
        dto.setPatientId(r.getPatient().getId());
        dto.setPatientName(r.getPatient().getUser().getName());
        dto.setDoctorId(r.getDoctor().getId());
        dto.setDoctorName(r.getDoctor().getUser().getName());
        dto.setAppointmentId(r.getAppointment() != null ? r.getAppointment().getId() : null);
        dto.setReportType(r.getReportType());
        dto.setTitle(r.getTitle());
        dto.setDescription(r.getDescription());
        dto.setFindings(r.getFindings());
        dto.setRecommendations(r.getRecommendations());
        dto.setCreatedAt(r.getCreatedAt());
        return dto;
    }
}
