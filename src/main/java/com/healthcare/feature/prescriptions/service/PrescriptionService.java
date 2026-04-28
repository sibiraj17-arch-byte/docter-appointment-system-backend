package com.healthcare.feature.prescriptions.service;

import com.healthcare.entity.*;
import com.healthcare.exception.ResourceNotFoundException;
import com.healthcare.feature.prescriptions.dto.*;
import com.healthcare.feature.prescriptions.repository.PrescriptionRepository;
import com.healthcare.feature.appointments.repository.AppointmentRepository;
import com.healthcare.feature.doctors.repository.DoctorRepository;
import com.healthcare.feature.patients.repository.PatientRepository;
import com.healthcare.security.CustomUserPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class PrescriptionService {

    private final PrescriptionRepository prescriptionRepository;
    private final AppointmentRepository appointmentRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;

    public PrescriptionService(PrescriptionRepository prescriptionRepository,
                                AppointmentRepository appointmentRepository,
                                DoctorRepository doctorRepository,
                                PatientRepository patientRepository) {
        this.prescriptionRepository = prescriptionRepository;
        this.appointmentRepository = appointmentRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
    }

    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomUserPrincipal) {
            return ((CustomUserPrincipal) auth.getPrincipal()).getUserId();
        }
        throw new com.healthcare.exception.UnauthorizedException("Not authenticated");
    }

    private String getCurrentUserRole() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomUserPrincipal) {
            return ((CustomUserPrincipal) auth.getPrincipal()).getRole().name();
        }
        throw new com.healthcare.exception.UnauthorizedException("Not authenticated");
    }

    private void assertCanAccess(Prescription prescription) {
        Long userId = getCurrentUserId();
        String role = getCurrentUserRole();

        if ("DOCTOR".equals(role) && prescription.getDoctor().getUser().getId().equals(userId)) {
            return;
        }

        if ("PATIENT".equals(role) && prescription.getPatient().getUser().getId().equals(userId)) {
            return;
        }

        throw new com.healthcare.exception.UnauthorizedException("You can only access your own prescriptions");
    }

    @Transactional
    public PrescriptionResponseDTO create(PrescriptionRequestDTO request) {
        Long userId = getCurrentUserId();
        Doctor doctor = doctorRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor profile", "userId", userId));

        Appointment appointment = null;
        Patient patient;

        if (request.getAppointmentId() != null) {
            appointment = appointmentRepository.findById(request.getAppointmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Appointment", "id", request.getAppointmentId()));
            if (!appointment.getDoctor().getId().equals(doctor.getId())) {
                throw new com.healthcare.exception.UnauthorizedException("You can only create prescriptions for your own appointments");
            }
            patient = appointment.getPatient();
        } else if (request.getPatientId() != null) {
            patient = patientRepository.findById(request.getPatientId())
                    .orElseThrow(() -> new ResourceNotFoundException("Patient", "id", request.getPatientId()));
        } else {
            throw new ResourceNotFoundException("Either appointmentId or patientId is required");
        }

        Prescription prescription = new Prescription();
        prescription.setPatient(patient);
        prescription.setDoctor(doctor);
        prescription.setAppointment(appointment);
        prescription.setDiagnosis(request.getDiagnosis());
        prescription.setInstructions(request.getInstructions());
        prescription.setFollowUp(request.getFollowUp());

        if (request.getItems() != null) {
            for (PrescriptionItemDTO itemDTO : request.getItems()) {
                PrescriptionItem item = new PrescriptionItem();
                item.setMedicineName(itemDTO.getMedicineName());
                item.setDosage(itemDTO.getDosage());
                item.setFrequency(itemDTO.getFrequency());
                item.setDuration(itemDTO.getDuration());
                item.setNotes(itemDTO.getNotes());
                prescription.addItem(item);
            }
        }

        return toDTO(prescriptionRepository.save(prescription));
    }

    public PrescriptionResponseDTO getById(Long id) {
        Prescription prescription = prescriptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Prescription", "id", id));
        assertCanAccess(prescription);
        return toDTO(prescription);
    }

    public List<PrescriptionResponseDTO> getAll() {
        Long userId = getCurrentUserId();
        Doctor doctor = doctorRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor profile", "userId", userId));
        return prescriptionRepository.findByDoctorId(doctor.getId()).stream()
                .map(this::toDTO).collect(Collectors.toList());
    }

    public PrescriptionResponseDTO getByAppointmentId(Long appointmentId) {
        Prescription prescription = prescriptionRepository.findByAppointmentId(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Prescription", "appointmentId", appointmentId));
        assertCanAccess(prescription);
        return toDTO(prescription);
    }

    public List<PrescriptionResponseDTO> getByPatient(Long patientId) {
        Long userId = getCurrentUserId();
        String role = getCurrentUserRole();

        if ("PATIENT".equals(role)) {
            Patient patient = patientRepository.findByUserId(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Patient profile", "userId", userId));
            if (!patient.getId().equals(patientId)) {
                throw new com.healthcare.exception.UnauthorizedException("You can only view your own prescriptions");
            }
        }

        Long doctorId = null;
        if ("DOCTOR".equals(role)) {
            doctorId = doctorRepository.findByUserId(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Doctor profile", "userId", userId))
                    .getId();
        }

        final Long scopedDoctorId = doctorId;
        return prescriptionRepository.findByPatientId(patientId).stream()
                .filter(prescription -> scopedDoctorId == null || prescription.getDoctor().getId().equals(scopedDoctorId))
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<PrescriptionResponseDTO> getByDoctor(Long doctorId) {
        Long userId = getCurrentUserId();
        Doctor doctor = doctorRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor profile", "userId", userId));
        if (!doctor.getId().equals(doctorId)) {
            throw new com.healthcare.exception.UnauthorizedException("You can only view your own prescriptions");
        }
        return prescriptionRepository.findByDoctorId(doctorId).stream()
                .map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional
    public PrescriptionResponseDTO update(Long id, PrescriptionRequestDTO request) {
        Long userId = getCurrentUserId();
        Doctor doctor = doctorRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor profile", "userId", userId));

        Prescription prescription = prescriptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Prescription", "id", id));

        if (!prescription.getDoctor().getId().equals(doctor.getId())) {
            throw new com.healthcare.exception.UnauthorizedException("You can only update your own prescriptions");
        }

        prescription.setDiagnosis(request.getDiagnosis());
        prescription.setInstructions(request.getInstructions());
        prescription.setFollowUp(request.getFollowUp());

        prescription.getItems().clear();
        if (request.getItems() != null) {
            for (PrescriptionItemDTO itemDTO : request.getItems()) {
                PrescriptionItem item = new PrescriptionItem();
                item.setMedicineName(itemDTO.getMedicineName());
                item.setDosage(itemDTO.getDosage());
                item.setFrequency(itemDTO.getFrequency());
                item.setDuration(itemDTO.getDuration());
                item.setNotes(itemDTO.getNotes());
                prescription.addItem(item);
            }
        }

        return toDTO(prescriptionRepository.save(prescription));
    }

    public String generatePdf(Long id) {
        Prescription prescription = prescriptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Prescription", "id", id));
        return "PDF generation for prescription " + id + " - Patient: " + prescription.getPatient().getUser().getName()
                + ", Diagnosis: " + prescription.getDiagnosis();
    }

    public String sendEmail(Long id) {
        Prescription prescription = prescriptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Prescription", "id", id));
        return "Prescription email sent to patient: " + prescription.getPatient().getUser().getName();
    }

    private PrescriptionResponseDTO toDTO(Prescription p) {
        PrescriptionResponseDTO dto = new PrescriptionResponseDTO();
        dto.setId(p.getId());
        dto.setPatientId(p.getPatient().getId());
        dto.setPatientName(p.getPatient().getUser().getName());
        dto.setDoctorId(p.getDoctor().getId());
        dto.setDoctorName(p.getDoctor().getUser().getName());
        dto.setAppointmentId(p.getAppointment() != null ? p.getAppointment().getId() : null);
        dto.setDiagnosis(p.getDiagnosis());
        dto.setInstructions(p.getInstructions());
        dto.setFollowUp(p.getFollowUp());
        dto.setCreatedAt(p.getCreatedAt());
        dto.setItems(p.getItems().stream().map(this::toItemDTO).collect(Collectors.toList()));
        return dto;
    }

    private PrescriptionItemDTO toItemDTO(PrescriptionItem item) {
        PrescriptionItemDTO dto = new PrescriptionItemDTO();
        dto.setId(item.getId());
        dto.setMedicineName(item.getMedicineName());
        dto.setDosage(item.getDosage());
        dto.setFrequency(item.getFrequency());
        dto.setDuration(item.getDuration());
        dto.setNotes(item.getNotes());
        return dto;
    }
}
