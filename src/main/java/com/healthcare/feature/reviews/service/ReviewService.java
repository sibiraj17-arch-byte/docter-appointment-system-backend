package com.healthcare.feature.reviews.service;

import com.healthcare.entity.*;
import com.healthcare.enums.AppointmentStatus;
import com.healthcare.exception.DuplicateResourceException;
import com.healthcare.exception.ResourceNotFoundException;
import com.healthcare.feature.reviews.dto.*;
import com.healthcare.feature.reviews.repository.ReviewRepository;
import com.healthcare.feature.appointments.repository.AppointmentRepository;
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
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;

    public ReviewService(ReviewRepository reviewRepository,
                          AppointmentRepository appointmentRepository,
                          PatientRepository patientRepository) {
        this.reviewRepository = reviewRepository;
        this.appointmentRepository = appointmentRepository;
        this.patientRepository = patientRepository;
    }

    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomUserPrincipal) {
            return ((CustomUserPrincipal) auth.getPrincipal()).getUserId();
        }
        throw new com.healthcare.exception.UnauthorizedException("Not authenticated");
    }

    @Transactional
    public ReviewResponseDTO create(ReviewRequestDTO request) {
        Long userId = getCurrentUserId();
        Patient patient = patientRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient profile", "userId", userId));

        Appointment appointment = appointmentRepository.findById(request.getAppointmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Appointment", "id", request.getAppointmentId()));

        if (!appointment.getPatient().getId().equals(patient.getId())) {
            throw new com.healthcare.exception.UnauthorizedException("You can only review your own appointments");
        }

        if (appointment.getStatus() != AppointmentStatus.COMPLETED) {
            throw new com.healthcare.exception.AppointmentConflictException("Can only review completed appointments");
        }

        if (reviewRepository.existsByAppointmentId(request.getAppointmentId())) {
            throw new DuplicateResourceException("Review already exists for this appointment");
        }

        Review review = new Review();
        review.setPatient(patient);
        review.setDoctor(appointment.getDoctor());
        review.setAppointment(appointment);
        review.setRating(request.getRating());
        review.setComment(request.getComment());

        return toDTO(reviewRepository.save(review));
    }

    public ReviewResponseDTO getById(Long id) {
        return toDTO(reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Review", "id", id)));
    }

    public List<ReviewResponseDTO> getAll() {
        Long userId = getCurrentUserId();
        Patient patient = patientRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient profile", "userId", userId));
        return reviewRepository.findByPatientId(patient.getId()).stream()
                .map(this::toDTO).collect(Collectors.toList());
    }

    public ReviewResponseDTO getByAppointment(Long appointmentId) {
        return toDTO(reviewRepository.findByAppointmentId(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Review", "appointmentId", appointmentId)));
    }

    public List<ReviewResponseDTO> getByProfessional(Long professionalId) {
        return reviewRepository.findByDoctorId(professionalId).stream()
                .map(this::toDTO).collect(Collectors.toList());
    }

    public Double getAverageRating(Long professionalId) {
        Double avg = reviewRepository.getAverageRating(professionalId);
        return avg != null ? avg : 0.0;
    }

    public List<ReviewResponseDTO> getByPatient(Long patientId) {
        return reviewRepository.findByPatientId(patientId).stream()
                .map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional
    public ReviewResponseDTO update(Long id, ReviewRequestDTO request) {
        Long userId = getCurrentUserId();
        Patient patient = patientRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient profile", "userId", userId));

        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Review", "id", id));

        if (!review.getPatient().getId().equals(patient.getId())) {
            throw new com.healthcare.exception.UnauthorizedException("You can only update your own reviews");
        }

        review.setRating(request.getRating());
        review.setComment(request.getComment());

        return toDTO(reviewRepository.save(review));
    }

    public Boolean checkReview(Long appointmentId) {
        return reviewRepository.existsByAppointmentId(appointmentId);
    }

    public RatingStatsDTO getRatingStats(Long professionalId) {
        Object[] stats = reviewRepository.getRatingStats(professionalId);
        if (stats == null || stats[0] == null) {
            RatingStatsDTO dto = new RatingStatsDTO();
            dto.setAverageRating(0.0);
            dto.setTotalReviews(0L);
            dto.setFiveStar(0L);
            dto.setFourStar(0L);
            dto.setThreeStar(0L);
            dto.setTwoStar(0L);
            dto.setOneStar(0L);
            return dto;
        }
        return new RatingStatsDTO(
                ((Number) stats[0]).doubleValue(),
                ((Number) stats[1]).longValue(),
                stats[2] != null ? ((Number) stats[2]).longValue() : 0L,
                stats[3] != null ? ((Number) stats[3]).longValue() : 0L,
                stats[4] != null ? ((Number) stats[4]).longValue() : 0L,
                stats[5] != null ? ((Number) stats[5]).longValue() : 0L,
                stats[6] != null ? ((Number) stats[6]).longValue() : 0L
        );
    }

    private ReviewResponseDTO toDTO(Review r) {
        ReviewResponseDTO dto = new ReviewResponseDTO();
        dto.setId(r.getId());
        dto.setPatientId(r.getPatient().getId());
        dto.setPatientName(r.getPatient().getUser().getName());
        dto.setDoctorId(r.getDoctor().getId());
        dto.setDoctorName(r.getDoctor().getUser().getName());
        dto.setAppointmentId(r.getAppointment() != null ? r.getAppointment().getId() : null);
        dto.setRating(r.getRating());
        dto.setComment(r.getComment());
        dto.setCreatedAt(r.getCreatedAt());
        return dto;
    }
}
