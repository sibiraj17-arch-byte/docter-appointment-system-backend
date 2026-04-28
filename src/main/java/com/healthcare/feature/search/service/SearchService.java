package com.healthcare.feature.search.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.healthcare.entity.Appointment;
import com.healthcare.entity.Doctor;
import com.healthcare.entity.MedicalHistory;
import com.healthcare.entity.Prescription;
import com.healthcare.enums.AppointmentStatus;
import com.healthcare.feature.appointments.dto.AppointmentResponseDTO;
import com.healthcare.feature.doctors.dto.DoctorResponseDTO;
import com.healthcare.feature.patientHistory.dto.MedicalHistoryResponseDTO;
import com.healthcare.feature.prescriptions.dto.PrescriptionItemDTO;
import com.healthcare.feature.prescriptions.dto.PrescriptionResponseDTO;
import com.healthcare.feature.appointments.repository.AppointmentRepository;
import com.healthcare.feature.doctors.repository.DoctorRepository;
import com.healthcare.feature.patientHistory.repository.MedicalHistoryRepository;
import com.healthcare.feature.prescriptions.repository.PrescriptionRepository;

@Service
@Transactional(readOnly = true)
public class SearchService {

    private final DoctorRepository doctorRepository;
    private final AppointmentRepository appointmentRepository;
    private final PrescriptionRepository prescriptionRepository;
    private final MedicalHistoryRepository medicalHistoryRepository;

    public SearchService(DoctorRepository doctorRepository,
                          AppointmentRepository appointmentRepository,
                          PrescriptionRepository prescriptionRepository,
                          MedicalHistoryRepository medicalHistoryRepository) {
        this.doctorRepository = doctorRepository;
        this.appointmentRepository = appointmentRepository;
        this.prescriptionRepository = prescriptionRepository;
        this.medicalHistoryRepository = medicalHistoryRepository;
    }

    public List<DoctorResponseDTO> searchProfessionalsByName(String name) {
        return doctorRepository.searchByName(name).stream()
                .filter(p -> p.getIsVerified())
                .map(this::toProfessionalDTO).collect(Collectors.toList());
    }

    public List<DoctorResponseDTO> searchProfessionalsBySpecialization(String specialization) {
        return doctorRepository.findAll().stream()
                .filter(p -> p.getIsVerified() && p.getSpecialization() != null &&
                        p.getSpecialization().getName().toLowerCase().contains(specialization.toLowerCase()))
                .map(this::toProfessionalDTO).collect(Collectors.toList());
    }

    public List<DoctorResponseDTO> searchProfessionalsByRole(String role) {
        return doctorRepository.findByIsVerifiedTrue().stream()
                .map(this::toProfessionalDTO).collect(Collectors.toList());
    }

    public List<AppointmentResponseDTO> searchAppointments(Long patientId, AppointmentStatus status) {
        List<Appointment> appointments;
        if (patientId != null && status != null) {
            appointments = appointmentRepository.findByPatientIdAndStatus(patientId, status);
        } else if (patientId != null) {
            appointments = appointmentRepository.findByPatientId(patientId);
        } else if (status != null) {
            appointments = appointmentRepository.findAll().stream()
                    .filter(a -> a.getStatus() == status)
                    .collect(Collectors.toList());
        } else {
            appointments = appointmentRepository.findAll();
        }
        return appointments.stream().map(this::toAppointmentDTO).collect(Collectors.toList());
    }

    public List<PrescriptionResponseDTO> searchPrescriptions(Long patientId, String dateRange) {
        if (patientId != null) {
            return prescriptionRepository.findByPatientId(patientId).stream()
                    .map(this::toPrescriptionDTO).collect(Collectors.toList());
        }
        return prescriptionRepository.findAll().stream()
                .map(this::toPrescriptionDTO).collect(Collectors.toList());
    }

    public List<MedicalHistoryResponseDTO> searchMedicalHistory(Long patientId, String keyword) {
        if (patientId != null && keyword != null && !keyword.isBlank()) {
            return medicalHistoryRepository.searchByPatientAndKeyword(patientId, keyword).stream()
                    .map(this::toMedicalHistoryDTO).collect(Collectors.toList());
        } else if (patientId != null) {
            return medicalHistoryRepository.findByPatientId(patientId).stream()
                    .map(this::toMedicalHistoryDTO).collect(Collectors.toList());
        }
        return medicalHistoryRepository.findAll().stream()
                .map(this::toMedicalHistoryDTO).collect(Collectors.toList());
    }

    private DoctorResponseDTO toProfessionalDTO(Doctor doctor) {
        return DoctorResponseDTO.from(doctor);
    }

    private AppointmentResponseDTO toAppointmentDTO(Appointment apt) {
        AppointmentResponseDTO dto = new AppointmentResponseDTO();
        dto.setId(apt.getId());
        dto.setCode(apt.getCode());
        dto.setPatientId(apt.getPatient().getId());
        dto.setPatientName(apt.getPatient().getUser().getName());
        dto.setDoctorId(apt.getDoctor().getId());
        dto.setDoctorName(apt.getDoctor().getUser().getName());
        if (apt.getDoctor().getSpecialization() != null) {
            dto.setSpecialization(apt.getDoctor().getSpecialization().getName());
        }
        dto.setAppointmentDate(apt.getAppointmentDate());
        dto.setStartTime(apt.getStartTime());
        dto.setEndTime(apt.getEndTime());
        dto.setStatus(apt.getStatus());
        dto.setReason(apt.getReason());
        dto.setNotes(apt.getNotes());
        dto.setSymptoms(apt.getSymptoms());
        dto.setCreatedAt(apt.getCreatedAt());
        return dto;
    }

    private PrescriptionResponseDTO toPrescriptionDTO(Prescription p) {
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
        dto.setItems(p.getItems().stream().map(item -> {
            PrescriptionItemDTO itemDTO = new PrescriptionItemDTO();
            itemDTO.setId(item.getId());
            itemDTO.setMedicineName(item.getMedicineName());
            itemDTO.setDosage(item.getDosage());
            itemDTO.setFrequency(item.getFrequency());
            itemDTO.setDuration(item.getDuration());
            itemDTO.setNotes(item.getNotes());
            return itemDTO;
        }).collect(Collectors.toList()));
        return dto;
    }

    private MedicalHistoryResponseDTO toMedicalHistoryDTO(MedicalHistory mh) {
        MedicalHistoryResponseDTO dto = new MedicalHistoryResponseDTO();
        dto.setId(mh.getId());
        dto.setPatientId(mh.getPatient().getId());
        dto.setCondition(mh.getCondition());
        dto.setDiagnosis(mh.getDiagnosis());
        dto.setDiagnosisDate(mh.getDiagnosisDate());
        dto.setStatus(mh.getStatus());
        dto.setNotes(mh.getNotes());
        dto.setTreatment(mh.getTreatment());
        dto.setCreatedAt(mh.getCreatedAt());
        return dto;
    }
}
