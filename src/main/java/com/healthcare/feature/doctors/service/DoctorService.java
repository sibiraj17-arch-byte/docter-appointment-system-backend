package com.healthcare.feature.doctors.service;

import com.healthcare.entity.Doctor;
import com.healthcare.entity.Specialization;
import com.healthcare.entity.User;
import com.healthcare.exception.ResourceNotFoundException;
import com.healthcare.feature.doctors.dto.*;
import com.healthcare.feature.doctors.repository.DoctorRepository;
import com.healthcare.feature.specializations.repository.SpecializationRepository;
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
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final UserRepository userRepository;
    private final SpecializationRepository specializationRepository;

    public DoctorService(DoctorRepository doctorRepository,
                         UserRepository userRepository,
                         SpecializationRepository specializationRepository) {
        this.doctorRepository = doctorRepository;
        this.userRepository = userRepository;
        this.specializationRepository = specializationRepository;
    }

    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomUserPrincipal) {
            return ((CustomUserPrincipal) auth.getPrincipal()).getUserId();
        }
        throw new com.healthcare.exception.UnauthorizedException("Not authenticated");
    }

    @Transactional
    public DoctorResponseDTO createProfile(DoctorRequestDTO request) {
        Long userId = getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        if (doctorRepository.findByUserId(userId).isPresent()) {
            throw new com.healthcare.exception.DuplicateResourceException("Profile already exists");
        }

        Doctor doctor = new Doctor();
        doctor.setUser(user);
        doctor.setQualification(request.getQualification());
        doctor.setExperience(request.getExperience());
        doctor.setBio(request.getBio());
        doctor.setExperienceYears(request.getExperienceYears());
        doctor.setConsultationFee(request.getConsultationFee() != null ? request.getConsultationFee() : 500.0);
        doctor.setTotalExperience(request.getTotalExperience());

        if (request.getSpecializationId() != null) {
            Specialization spec = specializationRepository.findById(request.getSpecializationId())
                    .orElseThrow(() -> new ResourceNotFoundException("Specialization", "id", request.getSpecializationId()));
            doctor.setSpecialization(spec);
        }

        return DoctorResponseDTO.from(doctorRepository.save(doctor));
    }

    public DoctorResponseDTO getMyProfile() {
        Long userId = getCurrentUserId();
        Doctor doctor = doctorRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor profile", "userId", userId));
        return DoctorResponseDTO.from(doctor);
    }

    @Transactional
    public DoctorResponseDTO updateProfile(DoctorUpdateDTO request) {
        Long userId = getCurrentUserId();
        Doctor doctor = doctorRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor profile", "userId", userId));

        if (request.getQualification() != null) doctor.setQualification(request.getQualification());
        if (request.getExperience() != null) doctor.setExperience(request.getExperience());
        if (request.getBio() != null) doctor.setBio(request.getBio());
        if (request.getExperienceYears() != null) doctor.setExperienceYears(request.getExperienceYears());
        if (request.getConsultationFee() != null) doctor.setConsultationFee(request.getConsultationFee());
        if (request.getTotalExperience() != null) doctor.setTotalExperience(request.getTotalExperience());
        if (request.getSpecializationId() != null) {
            Specialization spec = specializationRepository.findById(request.getSpecializationId())
                    .orElseThrow(() -> new ResourceNotFoundException("Specialization", "id", request.getSpecializationId()));
            doctor.setSpecialization(spec);
        }

        return DoctorResponseDTO.from(doctorRepository.save(doctor));
    }

    public DoctorResponseDTO getById(Long id) {
        return DoctorResponseDTO.from(doctorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor", "id", id)));
    }

    public List<DoctorResponseDTO> getAll() {
        return doctorRepository.findAll().stream().map(DoctorResponseDTO::from).collect(Collectors.toList());
    }

    public DoctorResponseDTO getByUserId(Long userId) {
        return DoctorResponseDTO.from(doctorRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor", "userId", userId)));
    }

    public DoctorResponseDTO getByMobileNumber(String mobileNumber) {
        return DoctorResponseDTO.from(doctorRepository.findByUserMobileNumber(mobileNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor", "mobileNumber", mobileNumber)));
    }
}
