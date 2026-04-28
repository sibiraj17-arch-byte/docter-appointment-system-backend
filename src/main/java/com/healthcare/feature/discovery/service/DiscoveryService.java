package com.healthcare.feature.discovery.service;

import com.healthcare.entity.Doctor;
import com.healthcare.entity.Review;
import com.healthcare.exception.ResourceNotFoundException;
import com.healthcare.feature.availability.dto.TimeSlotDTO;
import com.healthcare.feature.availability.service.AvailabilityService;
import com.healthcare.feature.doctors.dto.DoctorResponseDTO;
import com.healthcare.feature.reviews.dto.ReviewResponseDTO;
import com.healthcare.feature.availability.repository.AvailabilitySlotRepository;
import com.healthcare.feature.doctors.repository.DoctorRepository;
import com.healthcare.feature.reviews.repository.ReviewRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class DiscoveryService {

    private final DoctorRepository doctorRepository;
    private final AvailabilitySlotRepository availabilitySlotRepository;
    private final ReviewRepository reviewRepository;
    private final AvailabilityService availabilityService;

    public DiscoveryService(DoctorRepository doctorRepository,
                            AvailabilitySlotRepository availabilitySlotRepository,
                            ReviewRepository reviewRepository,
                            AvailabilityService availabilityService) {
        this.doctorRepository = doctorRepository;
        this.availabilitySlotRepository = availabilitySlotRepository;
        this.reviewRepository = reviewRepository;
        this.availabilityService = availabilityService;
    }

    public List<DoctorResponseDTO> getAllDoctors() {
        return doctorRepository.findByIsVerifiedTrue().stream().map(DoctorResponseDTO::from).collect(Collectors.toList());
    }

    public DoctorResponseDTO getDoctorById(Long id) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor", "id", id));
        return DoctorResponseDTO.from(doctor);
    }

    public List<DoctorResponseDTO> getBySpecialization(Long specializationId) {
        return doctorRepository.findBySpecializationId(specializationId).stream()
                .filter(d -> d.getIsVerified())
                .map(DoctorResponseDTO::from).collect(Collectors.toList());
    }

    public List<DoctorResponseDTO> getAvailableDoctors() {
        return doctorRepository.findAvailableDoctors().stream().map(DoctorResponseDTO::from).collect(Collectors.toList());
    }

    public List<DoctorResponseDTO> getAvailableBySpecialization(Long specializationId) {
        return doctorRepository.findAvailableBySpecialization(specializationId).stream().map(DoctorResponseDTO::from).collect(Collectors.toList());
    }

    @Transactional
    public List<TimeSlotDTO> getDoctorAvailability(Long id) {
        if (!doctorRepository.existsById(id)) {
            throw new ResourceNotFoundException("Doctor", "id", id);
        }
        LocalDate today = LocalDate.now();
        LocalDate weekLater = today.plusDays(7);
        for (LocalDate date = today; !date.isAfter(weekLater); date = date.plusDays(1)) {
            availabilityService.generateSlotsForDate(id, date);
        }
        return availabilitySlotRepository.findSchedule(id, today, weekLater).stream()
                .filter(s -> s.getIsAvailable())
                .map(this::toTimeSlotDTO)
                .collect(Collectors.toList());
    }

    public Double getProfessionalRating(Long id) {
        if (!doctorRepository.existsById(id)) {
            throw new ResourceNotFoundException("Doctor", "id", id);
        }
        Double avg = reviewRepository.getAverageRating(id);
        return avg != null ? avg : 0.0;
    }

    public List<ReviewResponseDTO> getProfessionalReviews(Long id) {
        if (!doctorRepository.existsById(id)) {
            throw new ResourceNotFoundException("Doctor", "id", id);
        }
        return reviewRepository.findByDoctorId(id).stream().map(this::toReviewDTO).collect(Collectors.toList());
    }

    private ReviewResponseDTO toReviewDTO(Review review) {
        ReviewResponseDTO dto = new ReviewResponseDTO();
        dto.setId(review.getId());
        dto.setPatientId(review.getPatient().getId());
        dto.setPatientName(review.getPatient().getUser().getName());
        dto.setDoctorId(review.getDoctor().getId());
        dto.setDoctorName(review.getDoctor().getUser().getName());
        dto.setAppointmentId(review.getAppointment() != null ? review.getAppointment().getId() : null);
        dto.setRating(review.getRating());
        dto.setComment(review.getComment());
        dto.setCreatedAt(review.getCreatedAt());
        return dto;
    }

    private TimeSlotDTO toTimeSlotDTO(com.healthcare.entity.AvailabilitySlot slot) {
        TimeSlotDTO dto = new TimeSlotDTO();
        dto.setId(slot.getId());
        dto.setSlotDate(slot.getSlotDate());
        dto.setStartTime(slot.getStartTime());
        dto.setEndTime(slot.getEndTime());
        dto.setIsAvailable(slot.getIsAvailable());
        return dto;
    }
}
