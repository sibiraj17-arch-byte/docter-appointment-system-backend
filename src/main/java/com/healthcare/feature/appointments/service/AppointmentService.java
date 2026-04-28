package com.healthcare.feature.appointments.service;

import com.healthcare.feature.appointments.dto.*;
import com.healthcare.entity.*;
import com.healthcare.enums.AppointmentStatus;
import com.healthcare.enums.AvailabilityStatus;
import com.healthcare.exception.*;
import com.healthcare.feature.appointments.repository.AppointmentRepository;
import com.healthcare.feature.doctors.repository.DoctorRepository;
import com.healthcare.feature.billing.repository.PaymentRepository;
import com.healthcare.feature.patients.repository.PatientRepository;
import com.healthcare.feature.availability.repository.AvailabilitySlotRepository;
import com.healthcare.security.CustomUserPrincipal;
import com.healthcare.utils.DateUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final AvailabilitySlotRepository slotRepository;
    private final PaymentRepository paymentRepository;

    public AppointmentService(AppointmentRepository appointmentRepository,
                               DoctorRepository doctorRepository,
                               PatientRepository patientRepository,
                               AvailabilitySlotRepository slotRepository,
                               PaymentRepository paymentRepository) {
        this.appointmentRepository = appointmentRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
        this.slotRepository = slotRepository;
        this.paymentRepository = paymentRepository;
    }

    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomUserPrincipal) {
            return ((CustomUserPrincipal) auth.getPrincipal()).getUserId();
        }
        throw new UnauthorizedException("Not authenticated");
    }

    private String getCurrentUserRole() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomUserPrincipal) {
            return ((CustomUserPrincipal) auth.getPrincipal()).getRole().name();
        }
        throw new UnauthorizedException("Not authenticated");
    }

    @Transactional
    public AppointmentResponseDTO bookAppointment(AppointmentRequestDTO request) {
        Long userId = getCurrentUserId();
        Patient patient = patientRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient profile not found. Please create your profile."));

        Doctor doctor = doctorRepository.findById(request.getDoctorId())
                .orElseThrow(() -> new ResourceNotFoundException("Doctor", "id", request.getDoctorId()));

        if (doctor.getAvailabilityStatus() != AvailabilityStatus.AVAILABLE) {
            throw new SlotNotAvailableException("Doctor is not available currently");
        }

        LocalDate appointmentDate = request.getAppointmentDate();
        LocalTime startTime = request.getStartTime();
        LocalTime endTime = startTime.plusHours(1);

        if (appointmentDate.isBefore(LocalDate.now())) {
            throw new SlotNotAvailableException("Cannot book appointment in the past");
        }

        if (!DateUtils.isWithinWorkingHours(startTime)) {
            throw new SlotNotAvailableException("Appointment time must be within working hours (9AM - 5PM)");
        }

        if (DateUtils.isWorkingDay(appointmentDate) && !DateUtils.isWithinWorkingHours(endTime.minusMinutes(1))) {
            throw new SlotNotAvailableException("Appointment end time exceeds working hours");
        }

        AvailabilitySlot slot = slotRepository
                .findByDoctorIdAndSlotDateAndStartTimeAndIsAvailableTrue(
                        request.getDoctorId(), appointmentDate, startTime)
                .orElseThrow(() -> new SlotNotAvailableException("Slot not available for the selected time"));

        boolean conflict = appointmentRepository.existsByDoctorIdAndAppointmentDateAndStartTimeAndStatusNot(
                request.getDoctorId(), appointmentDate, startTime, AppointmentStatus.CANCELLED);
        if (conflict) {
            throw new AppointmentConflictException("This time slot is already booked");
        }

        // Only mark slot as unavailable AFTER all validations pass
        slot.setIsAvailable(false);
        slotRepository.save(slot);

        Appointment appointment = new Appointment();
        appointment.setCode(DateUtils.generateAppointmentCode());
        appointment.setPatient(patient);
        appointment.setDoctor(doctor);
        appointment.setAppointmentDate(appointmentDate);
        appointment.setStartTime(startTime);
        appointment.setEndTime(endTime);
        appointment.setStatus(AppointmentStatus.SCHEDULED);
        appointment.setReason(request.getReason());
        appointment.setSymptoms(request.getSymptoms());

        return toDTO(appointmentRepository.save(appointment));
    }

    public AppointmentResponseDTO getById(Long id) {
        return toDTO(appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment", "id", id)));
    }

    public AppointmentResponseDTO getByCode(String code) {
        return toDTO(appointmentRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment", "code", code)));
    }

    public List<AppointmentResponseDTO> getAll() {
        String role = getCurrentUserRole();
        Long userId = getCurrentUserId();
        if (role.equals("DOCTOR")) {
            Doctor doctor = doctorRepository.findByUserId(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Doctor profile", "userId", userId));
            return appointmentRepository.findByDoctorId(doctor.getId()).stream()
                    .map(this::toDTO).collect(Collectors.toList());
        } else {
            Patient patient = patientRepository.findByUserId(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Patient profile", "userId", userId));
            return appointmentRepository.findByPatientId(patient.getId()).stream()
                    .map(this::toDTO).collect(Collectors.toList());
        }
    }

    public List<AppointmentResponseDTO> getByPatient(Long patientId) {
        return appointmentRepository.findByPatientId(patientId).stream()
                .map(this::toDTO).collect(Collectors.toList());
    }

    public List<AppointmentResponseDTO> getByDoctor(Long doctorId) {
        return appointmentRepository.findByDoctorId(doctorId).stream()
                .map(this::toDTO).collect(Collectors.toList());
    }

    public List<AppointmentResponseDTO> getUpcomingByPatient(Long patientId) {
        return appointmentRepository.findUpcomingByPatient(patientId, LocalDate.now()).stream()
                .map(this::toDTO).collect(Collectors.toList());
    }

    public List<AppointmentResponseDTO> getUpcomingByDoctor(Long doctorId) {
        return appointmentRepository.findUpcomingByDoctor(doctorId, LocalDate.now()).stream()
                .map(this::toDTO).collect(Collectors.toList());
    }

    public List<AppointmentResponseDTO> getTodayByDoctor(Long doctorId) {
        return appointmentRepository.findTodayByDoctor(doctorId, LocalDate.now()).stream()
                .map(this::toDTO).collect(Collectors.toList());
    }

    public List<AppointmentResponseDTO> getByDateRange(LocalDate startDate, LocalDate endDate) {
        return appointmentRepository.findByDateRange(startDate, endDate).stream()
                .map(this::toDTO).collect(Collectors.toList());
    }

    public List<AppointmentResponseDTO> getByDoctorAndDate(Long doctorId, LocalDate date) {
        return appointmentRepository.findByDoctorIdAndAppointmentDate(doctorId, date).stream()
                .map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional
    public AppointmentResponseDTO updateStatus(Long id, AppointmentStatus status) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment", "id", id));
        authorizeStatusUpdate(appointment, status);
        appointment.setStatus(status);
        Appointment savedAppointment = appointmentRepository.save(appointment);
        if (status == AppointmentStatus.COMPLETED) {
            ensurePendingPayment(savedAppointment);
        }
        return toDTO(savedAppointment);
    }

    @Transactional
    public AppointmentResponseDTO reschedule(Long id, RescheduleRequestDTO request) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment", "id", id));

        if (appointment.getStatus() == AppointmentStatus.COMPLETED || appointment.getStatus() == AppointmentStatus.CANCELLED) {
            throw new AppointmentConflictException("Cannot reschedule a " + appointment.getStatus() + " appointment");
        }

        LocalDate newDate = request.getNewDate();
        LocalTime newStart = request.getNewStartTime();

        if (newDate.isBefore(LocalDate.now())) {
            throw new SlotNotAvailableException("Cannot reschedule to past date");
        }

        if (!DateUtils.isWithinWorkingHours(newStart)) {
            throw new SlotNotAvailableException("New time must be within working hours (9AM - 5PM)");
        }

        boolean conflict = appointmentRepository.existsByDoctorIdAndAppointmentDateAndStartTimeAndStatusNot(
                appointment.getDoctor().getId(), newDate, newStart, AppointmentStatus.CANCELLED);
        if (conflict) {
            throw new AppointmentConflictException("New time slot is already booked");
        }

        AvailabilitySlot newSlot = slotRepository
                .findByDoctorIdAndSlotDateAndStartTimeAndIsAvailableTrue(
                        appointment.getDoctor().getId(), newDate, newStart)
                .orElseThrow(() -> new SlotNotAvailableException("New slot not available"));

        AvailabilitySlot oldSlot = slotRepository
                .findByDoctorIdAndSlotDateAndStartTime(
                        appointment.getDoctor().getId(), appointment.getAppointmentDate(), appointment.getStartTime())
                .orElse(null);
        if (oldSlot != null) {
            oldSlot.setIsAvailable(true);
            slotRepository.save(oldSlot);
        }

        newSlot.setIsAvailable(false);
        slotRepository.save(newSlot);

        appointment.setAppointmentDate(newDate);
        appointment.setStartTime(newStart);
        appointment.setEndTime(newStart.plusHours(1));
        appointment.setStatus(AppointmentStatus.RESCHEDULED);

        return toDTO(appointmentRepository.save(appointment));
    }

    @Transactional
    public AppointmentResponseDTO cancel(Long id, CancelRequestDTO request) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment", "id", id));

        if (appointment.getStatus() == AppointmentStatus.CANCELLED || appointment.getStatus() == AppointmentStatus.COMPLETED) {
            throw new AppointmentConflictException("Cannot cancel a " + appointment.getStatus() + " appointment");
        }

        appointment.setStatus(AppointmentStatus.CANCELLED);
        if (request.getReason() != null) {
            appointment.setNotes("Cancel reason: " + request.getReason());
        }

        AvailabilitySlot slot = slotRepository
                .findByDoctorIdAndSlotDateAndStartTime(
                        appointment.getDoctor().getId(), appointment.getAppointmentDate(), appointment.getStartTime())
                .orElse(null);
        if (slot != null) {
            slot.setIsAvailable(true);
            slotRepository.save(slot);
        }

        return toDTO(appointmentRepository.save(appointment));
    }

    private AppointmentResponseDTO toDTO(Appointment apt) {
        if (apt == null) {
            return null;
        }
        
        AppointmentResponseDTO dto = new AppointmentResponseDTO();
        dto.setId(apt.getId());
        dto.setCode(apt.getCode());
        
        // Safely handle patient information with null checks
        Patient patient = apt.getPatient();
        if (patient != null) {
            dto.setPatientId(patient.getId());
            User patientUser = patient.getUser();
            if (patientUser != null) {
                dto.setPatientName(patientUser.getName());
                dto.setPatientMobileNumber(patientUser.getMobileNumber());
                dto.setPatientEmail(patientUser.getEmail());
            }
        }
        
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
            if (doctor.getConsultationFee() != null) {
                dto.setConsultationFee(doctor.getConsultationFee());
            }
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

    private void authorizeStatusUpdate(Appointment appointment, AppointmentStatus status) {
        String role = getCurrentUserRole();
        Long userId = getCurrentUserId();

        if ("ADMIN".equals(role)) {
            return;
        }

        if (!"DOCTOR".equals(role)) {
            throw new UnauthorizedException("Only doctors can update appointment status");
        }

        if (appointment.getDoctor() == null || appointment.getDoctor().getUser() == null
                || !userId.equals(appointment.getDoctor().getUser().getId())) {
            throw new UnauthorizedException("You can only update your own appointments");
        }

        if (status != AppointmentStatus.COMPLETED && status != AppointmentStatus.CONFIRMED) {
            throw new UnauthorizedException("Doctors can only confirm or complete appointments");
        }
    }

    private void ensurePendingPayment(Appointment appointment) {
        Payment payment = paymentRepository.findByAppointmentId(appointment.getId()).orElseGet(Payment::new);
        if (payment.getId() != null && payment.getStatus() == com.healthcare.enums.PaymentStatus.COMPLETED) {
            return;
        }

        payment.setAppointment(appointment);
        payment.setPatient(appointment.getPatient());
        payment.setAmount(appointment.getDoctor() != null && appointment.getDoctor().getConsultationFee() != null
                ? appointment.getDoctor().getConsultationFee()
                : 0.0);
        payment.setStatus(com.healthcare.enums.PaymentStatus.PENDING);
        if (payment.getPaymentMethod() == null || payment.getPaymentMethod().isBlank()) {
            payment.setPaymentMethod("RAZORPAY");
        }
        paymentRepository.save(payment);
    }
}
