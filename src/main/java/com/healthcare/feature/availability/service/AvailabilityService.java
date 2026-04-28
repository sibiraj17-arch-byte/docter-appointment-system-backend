package com.healthcare.feature.availability.service;

import com.healthcare.entity.AvailabilitySlot;
import com.healthcare.entity.Doctor;
import com.healthcare.enums.AvailabilityStatus;
import com.healthcare.exception.ResourceNotFoundException;
import com.healthcare.exception.SlotNotAvailableException;
import com.healthcare.exception.UnauthorizedException;
import com.healthcare.feature.availability.repository.AvailabilitySlotRepository;
import com.healthcare.feature.doctors.repository.DoctorRepository;
import com.healthcare.feature.availability.dto.*;
import com.healthcare.security.CustomUserPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AvailabilityService {

    private final AvailabilitySlotRepository slotRepository;
    private final DoctorRepository doctorRepository;

    public AvailabilityService(AvailabilitySlotRepository slotRepository, DoctorRepository doctorRepository) {
        this.slotRepository = slotRepository;
        this.doctorRepository = doctorRepository;
    }

    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomUserPrincipal) {
            return ((CustomUserPrincipal) auth.getPrincipal()).getUserId();
        }
        throw new UnauthorizedException("Not authenticated");
    }

    public String toggleAvailability(Long doctorId, Boolean status) {
        Long userId = getCurrentUserId();
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor", "id", doctorId));

        if (!doctor.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("You can only manage your own availability");
        }

        doctor.setAvailabilityStatus(status ? AvailabilityStatus.AVAILABLE : AvailabilityStatus.ON_LEAVE);
        doctorRepository.save(doctor);
        return "Availability updated to " + doctor.getAvailabilityStatus();
    }

    public List<TimeSlotDTO> getSlots(Long doctorId, LocalDate date) {
        if (!doctorRepository.existsById(doctorId)) {
            throw new ResourceNotFoundException("Doctor", "id", doctorId);
        }
        return slotRepository.findByDoctorIdAndSlotDate(doctorId, date).stream()
                .map(this::toTimeSlotDTO).collect(Collectors.toList());
    }

    public Boolean checkSlot(Long doctorId, LocalDate date, LocalTime time) {
        if (!doctorRepository.existsById(doctorId)) {
            throw new ResourceNotFoundException("Doctor", "id", doctorId);
        }
        return slotRepository.findByDoctorIdAndSlotDateAndStartTimeAndIsAvailableTrue(doctorId, date, time).isPresent();
    }

    public List<AvailabilityResponseDTO> getSchedule(Long doctorId) {
        if (!doctorRepository.existsById(doctorId)) {
            throw new ResourceNotFoundException("Doctor", "id", doctorId);
        }
        LocalDate today = LocalDate.now();
        LocalDate weekLater = today.plusDays(7);
        List<AvailabilitySlot> slots = slotRepository.findSchedule(doctorId, today, weekLater);

        return slots.stream()
                .collect(Collectors.groupingBy(AvailabilitySlot::getSlotDate))
                .entrySet().stream()
                .map(entry -> {
                    AvailabilityResponseDTO dto = new AvailabilityResponseDTO();
                    dto.setDate(entry.getKey());
                    dto.setSlots(entry.getValue().stream().map(this::toTimeSlotDTO).collect(Collectors.toList()));
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public String blockSlots(Long doctorId, BlockSlotRequestDTO request) {
        Long userId = getCurrentUserId();
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor", "id", doctorId));

        if (!doctor.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("You can only manage your own availability");
        }

        if (request.getSlots() != null) {
            for (BlockSlotRequestDTO.TimeRange range : request.getSlots()) {
                List<AvailabilitySlot> slots = slotRepository.findByDoctorIdAndSlotDate(doctorId, request.getDate());
                for (AvailabilitySlot slot : slots) {
                    if (!slot.getStartTime().isBefore(range.getStartTime()) && !slot.getEndTime().isAfter(range.getEndTime())) {
                        slot.setIsAvailable(false);
                        slotRepository.save(slot);
                    }
                }
            }
        }
        return "Slots blocked successfully";
    }

    public void generateSlotsForDate(Long doctorId, LocalDate date) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor", "id", doctorId));

        DayOfWeek day = date.getDayOfWeek();
        if (day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY) return;

        LocalTime start = LocalTime.of(9, 0);
        LocalTime end = LocalTime.of(17, 0);

        while (start.isBefore(end)) {
                if (!slotRepository.existsByDoctorIdAndSlotDateAndStartTime(doctorId, date, start)) {
                    AvailabilitySlot slot = new AvailabilitySlot();
                    slot.setDoctor(doctor);
                slot.setSlotDate(date);
                slot.setStartTime(start);
                slot.setEndTime(start.plusHours(1));
                slot.setIsAvailable(true);
                slotRepository.save(slot);
            }
            start = start.plusHours(1);
        }
    }

    private TimeSlotDTO toTimeSlotDTO(AvailabilitySlot slot) {
        TimeSlotDTO dto = new TimeSlotDTO();
        dto.setId(slot.getId());
        dto.setSlotDate(slot.getSlotDate());
        dto.setStartTime(slot.getStartTime());
        dto.setEndTime(slot.getEndTime());
        dto.setIsAvailable(slot.getIsAvailable());
        return dto;
    }
}
