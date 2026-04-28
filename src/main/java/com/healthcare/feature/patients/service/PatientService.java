package com.healthcare.feature.patients.service;

import com.healthcare.entity.Patient;
import com.healthcare.entity.User;
import com.healthcare.exception.DuplicateResourceException;
import com.healthcare.exception.ResourceNotFoundException;
import com.healthcare.feature.patients.dto.*;
import com.healthcare.feature.patients.repository.PatientRepository;
import com.healthcare.feature.auth.repository.UserRepository;
import com.healthcare.security.CustomUserPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class PatientService {

    private final PatientRepository patientRepository;
    private final UserRepository userRepository;

    public PatientService(PatientRepository patientRepository, UserRepository userRepository) {
        this.patientRepository = patientRepository;
        this.userRepository = userRepository;
    }

    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomUserPrincipal) {
            return ((CustomUserPrincipal) auth.getPrincipal()).getUserId();
        }
        throw new com.healthcare.exception.UnauthorizedException("Not authenticated");
    }

    @Transactional
    public PatientResponseDTO createProfile(PatientRequestDTO request) {
        Long userId = getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        if (patientRepository.findByUserId(userId).isPresent()) {
            throw new DuplicateResourceException("Patient profile already exists");
        }

        Patient patient = new Patient();
        patient.setUser(user);
        patient.setGender(request.getGender());
        patient.setDateOfBirth(request.getDateOfBirth());
        patient.setAddress(request.getAddress());
        patient.setBloodGroup(request.getBloodGroup());
        patient.setAllergies(request.getAllergies());
        patient.setEmergencyContact(request.getEmergencyContact());

        return toDTO(patientRepository.save(patient));
    }

    public PatientResponseDTO getMyProfile() {
        Long userId = getCurrentUserId();
        Patient patient = patientRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient profile", "userId", userId));
        return toDTO(patient);
    }

    @Transactional
    public PatientResponseDTO updateProfile(PatientUpdateDTO request) {
        Long userId = getCurrentUserId();
        Patient patient = patientRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient profile", "userId", userId));

        if (request.getGender() != null) patient.setGender(request.getGender());
        if (request.getDateOfBirth() != null) patient.setDateOfBirth(request.getDateOfBirth());
        if (request.getAddress() != null) patient.setAddress(request.getAddress());
        if (request.getBloodGroup() != null) patient.setBloodGroup(request.getBloodGroup());
        if (request.getAllergies() != null) patient.setAllergies(request.getAllergies());
        if (request.getEmergencyContact() != null) patient.setEmergencyContact(request.getEmergencyContact());

        return toDTO(patientRepository.save(patient));
    }

    public PatientResponseDTO getById(Long id) {
        return toDTO(patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patient", "id", id)));
    }

    public List<PatientResponseDTO> getAll() {
        return patientRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    private PatientResponseDTO toDTO(Patient patient) {
        PatientResponseDTO dto = new PatientResponseDTO();
        dto.setId(patient.getId());
        dto.setUserId(patient.getUser().getId());
        dto.setName(patient.getUser().getName());
        dto.setMobileNumber(patient.getUser().getMobileNumber());
        dto.setEmail(patient.getUser().getEmail());
        dto.setGender(patient.getGender());
        dto.setDateOfBirth(patient.getDateOfBirth());
        dto.setAddress(patient.getAddress());
        dto.setBloodGroup(patient.getBloodGroup());
        dto.setAllergies(patient.getAllergies());
        dto.setEmergencyContact(patient.getEmergencyContact());
        dto.setCreatedAt(patient.getCreatedAt());
        return dto;
    }
}
