package com.healthcare.feature.billing.service;

import com.healthcare.entity.*;
import com.healthcare.feature.appointments.dto.AppointmentRequestDTO;
import com.healthcare.feature.appointments.dto.AppointmentResponseDTO;
import com.healthcare.feature.appointments.service.AppointmentService;
import com.healthcare.enums.AppointmentStatus;
import com.healthcare.enums.PaymentStatus;
import com.healthcare.exception.DuplicateResourceException;
import com.healthcare.exception.ResourceNotFoundException;
import com.healthcare.exception.UnauthorizedException;
import com.healthcare.feature.billing.dto.*;
import com.healthcare.feature.billing.repository.PaymentRepository;
import com.healthcare.feature.appointments.repository.AppointmentRepository;
import com.healthcare.feature.availability.repository.AvailabilitySlotRepository;
import com.healthcare.feature.doctors.repository.DoctorRepository;
import com.healthcare.feature.patients.repository.PatientRepository;
import com.healthcare.security.CustomUserPrincipal;
import com.healthcare.utils.DateUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final AvailabilitySlotRepository availabilitySlotRepository;
    private final AppointmentService appointmentService;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;
    private final String razorpayKeyId;
    private final String razorpayKeySecret;
    private final String razorpayApiBaseUrl;

    public PaymentService(PaymentRepository paymentRepository,
                           AppointmentRepository appointmentRepository,
                           PatientRepository patientRepository,
                           DoctorRepository doctorRepository,
                           AvailabilitySlotRepository availabilitySlotRepository,
                           AppointmentService appointmentService,
                           ObjectMapper objectMapper,
                           @Value("${razorpay.key-id}") String razorpayKeyId,
                           @Value("${razorpay.key-secret}") String razorpayKeySecret,
                           @Value("${razorpay.api-base-url:https://api.razorpay.com}") String razorpayApiBaseUrl) {
        this.paymentRepository = paymentRepository;
        this.appointmentRepository = appointmentRepository;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
        this.availabilitySlotRepository = availabilitySlotRepository;
        this.appointmentService = appointmentService;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newHttpClient();
        this.razorpayKeyId = razorpayKeyId;
        this.razorpayKeySecret = razorpayKeySecret;
        this.razorpayApiBaseUrl = razorpayApiBaseUrl;
    }

    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomUserPrincipal) {
            return ((CustomUserPrincipal) auth.getPrincipal()).getUserId();
        }
        throw new UnauthorizedException("Not authenticated");
    }

    @Transactional
    public PaymentResponseDTO createPayment(PaymentRequestDTO request) {
        Patient patient = getCurrentPatient();
        Appointment appointment = getOwnedAppointment(patient, request.getAppointmentId());
        assertPayableAppointment(appointment);

        Payment payment = paymentRepository.findByAppointmentId(request.getAppointmentId()).orElseGet(Payment::new);
        if (payment.getId() != null && payment.getStatus() == PaymentStatus.COMPLETED) {
            throw new DuplicateResourceException("Payment already exists for this appointment");
        }

        Double amount = request.getAmount() != null ? request.getAmount() : appointment.getDoctor().getConsultationFee();

        payment.setAppointment(appointment);
        payment.setPatient(patient);
        payment.setAmount(amount);
        payment.setStatus(PaymentStatus.COMPLETED);
        payment.setTransactionId(request.getRazorpayPaymentId() != null ? request.getRazorpayPaymentId() : DateUtils.generateTransactionId());
        payment.setPaymentMethod(request.getPaymentMethod() != null ? request.getPaymentMethod() : "ONLINE");
        payment.setRazorpayOrderId(request.getRazorpayOrderId());
        payment.setRazorpayPaymentId(request.getRazorpayPaymentId());
        payment.setRazorpaySignature(request.getRazorpaySignature());

        return toDTO(paymentRepository.save(payment));
    }

    @Transactional
    public RazorpayOrderResponseDTO createRazorpayOrder(RazorpayOrderRequestDTO request) {
        Patient patient = getCurrentPatient();
        Appointment appointment = getOwnedAppointment(patient, request.getAppointmentId());
        assertPayableAppointment(appointment);
        Payment payment = paymentRepository.findByAppointmentId(request.getAppointmentId()).orElseGet(Payment::new);

        if (payment.getId() != null && payment.getStatus() == PaymentStatus.COMPLETED) {
            throw new DuplicateResourceException("Payment already completed for this appointment");
        }

        double baseAmount = request.getAmount() != null ? request.getAmount() : appointment.getDoctor().getConsultationFee();
        long amountInPaise = Math.round(baseAmount * 100);

        JsonNode orderResponse = createRemoteRazorpayOrder(
                amountInPaise,
                appointment,
                appointment.getDoctor().getId(),
                appointment.getAppointmentDate().toString(),
                appointment.getStartTime().toString()
        );

        payment.setAppointment(appointment);
        payment.setPatient(patient);
        payment.setAmount(baseAmount);
        payment.setStatus(PaymentStatus.PENDING);
        payment.setPaymentMethod("RAZORPAY");
        payment.setRazorpayOrderId(orderResponse.path("id").asText());
        payment.setTransactionId(null);
        payment.setRazorpayPaymentId(null);
        payment.setRazorpaySignature(null);
        payment = paymentRepository.save(payment);

        RazorpayOrderResponseDTO dto = new RazorpayOrderResponseDTO();
        dto.setPaymentId(payment.getId());
        dto.setAppointmentId(appointment.getId());
        dto.setKeyId(razorpayKeyId);
        dto.setOrderId(orderResponse.path("id").asText());
        dto.setCurrency(orderResponse.path("currency").asText("INR"));
        dto.setAmount(orderResponse.path("amount").asLong(amountInPaise));
        dto.setName("MediCore Healthcare");
        dto.setDescription("Appointment payment");
        dto.setPatientName(patient.getUser().getName());
        dto.setPatientEmail(patient.getUser().getEmail());
        dto.setPatientContact(patient.getUser().getMobileNumber());
        return dto;
    }

    @Transactional
    public PaymentResponseDTO verifyRazorpayPayment(RazorpayVerificationRequestDTO request) {
        Patient patient = getCurrentPatient();
        Appointment appointment = getOwnedAppointment(patient, request.getAppointmentId());
        assertPayableAppointment(appointment);
        Payment payment = paymentRepository.findByAppointmentId(appointment.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "appointmentId", appointment.getId()));

        if (!request.getRazorpayOrderId().equals(payment.getRazorpayOrderId())) {
            throw new UnauthorizedException("Razorpay order mismatch");
        }

        if (!isValidSignature(request.getRazorpayOrderId(), request.getRazorpayPaymentId(), request.getRazorpaySignature())) {
            payment.setStatus(PaymentStatus.FAILED);
            paymentRepository.save(payment);
            throw new UnauthorizedException("Payment signature verification failed");
        }

        payment.setStatus(PaymentStatus.COMPLETED);
        payment.setTransactionId(request.getRazorpayPaymentId());
        payment.setRazorpayPaymentId(request.getRazorpayPaymentId());
        payment.setRazorpaySignature(request.getRazorpaySignature());
        payment.setPaymentMethod("RAZORPAY");
        return toDTO(paymentRepository.save(payment));
    }

    public RazorpayOrderResponseDTO createBookingRazorpayOrder(BookingRazorpayOrderRequestDTO request) {
        throw new DuplicateResourceException(
                "Direct payment during booking is disabled. Book the appointment first and pay after the doctor marks it completed."
        );
    }

    @Transactional
    public BookingPaymentResultDTO verifyBookingRazorpayPayment(BookingRazorpayVerifyRequestDTO request) {
        throw new DuplicateResourceException(
                "Direct payment during booking is disabled. Book the appointment first and pay after the doctor marks it completed."
        );
    }

    public PaymentResponseDTO getById(Long id) {
        return toDTO(paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "id", id)));
    }

    public List<PaymentResponseDTO> getAll() {
        Long userId = getCurrentUserId();
        Patient patient = patientRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient profile", "userId", userId));
        return paymentRepository.findByPatientId(patient.getId()).stream()
                .sorted((left, right) -> {
                    var leftCreatedAt = left.getCreatedAt();
                    var rightCreatedAt = right.getCreatedAt();
                    if (leftCreatedAt == null && rightCreatedAt == null) return 0;
                    if (leftCreatedAt == null) return 1;
                    if (rightCreatedAt == null) return -1;
                    return rightCreatedAt.compareTo(leftCreatedAt);
                })
                .map(this::toDTO).collect(Collectors.toList());
    }

    public PaymentResponseDTO getByAppointment(Long appointmentId) {
        return toDTO(paymentRepository.findByAppointmentId(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "appointmentId", appointmentId)));
    }

    public List<PaymentResponseDTO> getByPatient(Long patientId) {
        return paymentRepository.findByPatientId(patientId).stream()
                .map(this::toDTO).collect(Collectors.toList());
    }

    public List<PaymentResponseDTO> getByStatus(PaymentStatus status) {
        return paymentRepository.findByStatus(status).stream()
                .map(this::toDTO).collect(Collectors.toList());
    }

    public PaymentStatusDTO checkPayment(Long appointmentId) {
        PaymentStatusDTO dto = new PaymentStatusDTO();
        dto.setAppointmentId(appointmentId);
        var payment = paymentRepository.findByAppointmentId(appointmentId);
        if (payment.isPresent()) {
            dto.setPaymentExists(true);
            dto.setStatus(payment.get().getStatus().name());
        } else {
            dto.setPaymentExists(false);
            dto.setStatus("NOT_INITIATED");
        }
        return dto;
    }

    private Patient getCurrentPatient() {
        Long userId = getCurrentUserId();
        return patientRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient profile", "userId", userId));
    }

    private Appointment getOwnedAppointment(Patient patient, Long appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment", "id", appointmentId));

        if (!appointment.getPatient().getId().equals(patient.getId())) {
            throw new UnauthorizedException("You can only pay for your own appointments");
        }
        return appointment;
    }

    private void assertPayableAppointment(Appointment appointment) {
        if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
            throw new UnauthorizedException("Cancelled appointments cannot be paid");
        }
        if (appointment.getStatus() != AppointmentStatus.COMPLETED) {
            throw new UnauthorizedException("Payment is available only after the doctor marks the appointment as completed");
        }
    }

    private JsonNode createRemoteRazorpayOrder(long amountInPaise, Appointment appointment, Long doctorId, String appointmentDate, String startTime) {
        try {
            String receipt = appointment != null
                    ? "apt_" + appointment.getId() + "_" + System.currentTimeMillis()
                    : "booking_" + doctorId + "_" + System.currentTimeMillis();
            String payload = objectMapper.writeValueAsString(java.util.Map.of(
                    "amount", amountInPaise,
                    "currency", "INR",
                    "receipt", receipt,
                    "notes", java.util.Map.of(
                            "appointmentId", appointment != null ? String.valueOf(appointment.getId()) : "",
                            "appointmentCode", appointment != null && appointment.getCode() != null ? appointment.getCode() : "",
                            "doctorId", doctorId != null ? String.valueOf(doctorId) : "",
                            "appointmentDate", appointmentDate != null ? appointmentDate : "",
                            "startTime", startTime != null ? startTime : ""
                    )
            ));

            String credentials = Base64.getEncoder().encodeToString((razorpayKeyId + ":" + razorpayKeySecret).getBytes(StandardCharsets.UTF_8));
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(razorpayApiBaseUrl + "/v1/orders"))
                    .header(HttpHeaders.AUTHORIZATION, "Basic " + credentials)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .POST(HttpRequest.BodyPublishers.ofString(payload))
                    .build();

            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException("Razorpay order creation failed: " + response.body());
            }
            return objectMapper.readTree(response.body());
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to create Razorpay order", exception);
        }
    }

    private Doctor validateBookingRequest(Long doctorId, java.time.LocalDate appointmentDate, java.time.LocalTime startTime) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor", "id", doctorId));
        if (doctor.getAvailabilityStatus() != com.healthcare.enums.AvailabilityStatus.AVAILABLE) {
            throw new UnauthorizedException("Doctor is not available currently");
        }
        availabilitySlotRepository.findByDoctorIdAndSlotDateAndStartTimeAndIsAvailableTrue(doctorId, appointmentDate, startTime)
                .orElseThrow(() -> new ResourceNotFoundException("Available slot", "startTime", startTime));
        boolean conflict = appointmentRepository.existsByDoctorIdAndAppointmentDateAndStartTimeAndStatusNot(
                doctorId, appointmentDate, startTime, com.healthcare.enums.AppointmentStatus.CANCELLED);
        if (conflict) {
            throw new DuplicateResourceException("This slot has already been booked");
        }
        return doctor;
    }

    private boolean isValidSignature(String orderId, String paymentId, String signature) {
        try {
            String payload = orderId + "|" + paymentId;
            Mac sha256Hmac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(razorpayKeySecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            sha256Hmac.init(secretKey);
            byte[] digest = sha256Hmac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            String generated = bytesToHex(digest);
            return generated.equals(signature);
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to verify Razorpay signature", exception);
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder builder = new StringBuilder(bytes.length * 2);
        for (byte value : bytes) {
            builder.append(String.format("%02x", value));
        }
        return builder.toString();
    }

    private PaymentResponseDTO toDTO(Payment p) {
        PaymentResponseDTO dto = new PaymentResponseDTO();
        dto.setId(p.getId());
        dto.setAppointmentId(p.getAppointment() != null ? p.getAppointment().getId() : null);
        dto.setAppointmentCode(p.getAppointment() != null ? p.getAppointment().getCode() : null);
        dto.setAppointmentStatus(p.getAppointment() != null ? p.getAppointment().getStatus() : null);
        dto.setAppointmentDate(p.getAppointment() != null ? p.getAppointment().getAppointmentDate() : null);
        dto.setAppointmentStartTime(p.getAppointment() != null ? p.getAppointment().getStartTime() : null);
        if (p.getPatient() != null) {
            dto.setPatientId(p.getPatient().getId());
            if (p.getPatient().getUser() != null) {
                dto.setPatientName(p.getPatient().getUser().getName());
            }
        }
        if (p.getAppointment() != null && p.getAppointment().getDoctor() != null) {
            dto.setDoctorId(p.getAppointment().getDoctor().getId());
            if (p.getAppointment().getDoctor().getUser() != null) {
                dto.setDoctorName(p.getAppointment().getDoctor().getUser().getName());
            }
            if (p.getAppointment().getDoctor().getSpecialization() != null) {
                dto.setSpecialization(p.getAppointment().getDoctor().getSpecialization().getName());
            }
            dto.setConsultationFee(p.getAppointment().getDoctor().getConsultationFee());
        }
        dto.setAmount(p.getAmount());
        dto.setStatus(p.getStatus());
        dto.setTransactionId(p.getTransactionId());
        dto.setPaymentMethod(p.getPaymentMethod());
        dto.setRazorpayOrderId(p.getRazorpayOrderId());
        dto.setRazorpayPaymentId(p.getRazorpayPaymentId());
        dto.setCreatedAt(p.getCreatedAt());
        return dto;
    }
}
