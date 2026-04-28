package com.healthcare.feature.admin.service;

import com.healthcare.feature.admin.dto.*;
import com.healthcare.entity.*;
import com.healthcare.enums.AppointmentStatus;
import com.healthcare.enums.AvailabilityStatus;
import com.healthcare.enums.PaymentStatus;
import com.healthcare.enums.UserRole;
import com.healthcare.exception.DuplicateResourceException;
import com.healthcare.exception.ResourceNotFoundException;
import com.healthcare.exception.UnauthorizedException;
import com.healthcare.feature.doctors.dto.DoctorResponseDTO;
import com.healthcare.feature.auth.repository.UserRepository;
import com.healthcare.feature.doctors.repository.DoctorRepository;
import com.healthcare.feature.patients.dto.PatientResponseDTO;
import com.healthcare.feature.patients.repository.PatientRepository;
import com.healthcare.feature.specializations.repository.SpecializationRepository;
import com.healthcare.feature.appointments.repository.AppointmentRepository;
import com.healthcare.feature.billing.repository.PaymentRepository;
import com.healthcare.feature.reviews.repository.ReviewRepository;
import com.healthcare.security.CustomUserPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AdminService {
    private static final DateTimeFormatter MONTH_FORMAT = DateTimeFormatter.ofPattern("MMM");
    private static final DateTimeFormatter RECENT_USER_FORMAT = DateTimeFormatter.ofPattern("dd MMM yyyy");

    private final UserRepository userRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final SpecializationRepository specializationRepository;
    private final AppointmentRepository appointmentRepository;
    private final PaymentRepository paymentRepository;
    private final ReviewRepository reviewRepository;

    public AdminService(UserRepository userRepository,
                         DoctorRepository doctorRepository,
                         PatientRepository patientRepository,
                         SpecializationRepository specializationRepository,
                         AppointmentRepository appointmentRepository,
                         PaymentRepository paymentRepository,
                         ReviewRepository reviewRepository) {
        this.userRepository = userRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
        this.specializationRepository = specializationRepository;
        this.appointmentRepository = appointmentRepository;
        this.paymentRepository = paymentRepository;
        this.reviewRepository = reviewRepository;
    }

    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomUserPrincipal) {
            return ((CustomUserPrincipal) auth.getPrincipal()).getUserId();
        }
        throw new UnauthorizedException("Not authenticated");
    }

    @Transactional(readOnly = true)
    public List<DoctorResponseDTO> getAllDoctors() {
        return doctorRepository
        		.findAll()
        		.stream()
        		.map(DoctorResponseDTO::from)
        		.collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PatientResponseDTO> getAllPatients() {
        Map<Long, Patient> patientsByUserId = patientRepository.findAll().stream()
                .filter(patient -> patient.getUser() != null && patient.getUser().getId() != null)
                .collect(Collectors.toMap(patient -> patient.getUser().getId(), patient -> patient, (left, right) -> left));

        return userRepository.findByRole(UserRole.PATIENT).stream()
                .sorted(Comparator.comparing(User::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(user -> toPatientDTO(user, patientsByUserId.get(user.getId())))
                .collect(Collectors.toList());
    }

    @Transactional
    public DoctorResponseDTO createDoctor(String name, String mobileNumber, String email,
                                                  Long specializationId, String qualification,
                                                  Integer experienceYears, Double consultationFee) {
        if (userRepository.existsByMobileNumber(mobileNumber)) {
            throw new DuplicateResourceException("Mobile number already registered");
        }

        User user = new User(name, mobileNumber, UserRole.DOCTOR);
        user.setEmail(email);
        user = userRepository.save(user);

        Doctor doctor = new Doctor();
        doctor.setUser(user);
        doctor.setQualification(qualification);
        doctor.setExperienceYears(experienceYears);
        doctor.setConsultationFee(consultationFee != null ? consultationFee : 500.0);
        doctor.setIsVerified(true);
        doctor.setAvailabilityStatus(AvailabilityStatus.AVAILABLE);
        doctor.setExperience(experienceYears != null ? experienceYears + " years" : null);

        if (specializationId != null) {
            specializationRepository.findById(specializationId).ifPresent(doctor::setSpecialization);
        }

        return DoctorResponseDTO.from(doctorRepository.save(doctor));
    }

    @Transactional
    public DoctorResponseDTO updateDoctor(Long id, String name, String email,
                                                  Long specializationId, String qualification,
                                                  Integer experienceYears, Double consultationFee, Boolean isVerified) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor", "id", id));

        User user = doctor.getUser();
        if (name != null) user.setName(name);
        if (email != null) user.setEmail(email);
        userRepository.save(user);

        if (qualification != null) doctor.setQualification(qualification);
        if (experienceYears != null) doctor.setExperienceYears(experienceYears);
        if (consultationFee != null) doctor.setConsultationFee(consultationFee);
        if (isVerified != null) doctor.setIsVerified(isVerified);
        if (experienceYears != null) doctor.setExperience(experienceYears + " years");
        if (specializationId != null) {
            specializationRepository.findById(specializationId).ifPresent(doctor::setSpecialization);
        }

        return DoctorResponseDTO.from(doctorRepository.save(doctor));
    }

    @Transactional
    public void deleteDoctor(Long id) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor", "id", id));
        userRepository.delete(doctor.getUser());
    }

    @Transactional
    public User createAdmin(String name, String mobileNumber, String email) {
        if (userRepository.existsByMobileNumber(mobileNumber)) {
            throw new DuplicateResourceException("Mobile number already registered");
        }
        User user = new User(name, mobileNumber, UserRole.ADMIN);
        user.setEmail(email);
        return userRepository.save(user);
    }

    public User updateAdmin(Long id, String name, String email) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        if (name != null) user.setName(name);
        if (email != null) user.setEmail(email);
        return userRepository.save(user);
    }

    public void deleteAdmin(Long id) {
        User currentUser = userRepository.findById(getCurrentUserId()).orElseThrow();
        if (currentUser.getId().equals(id)) {
            throw new com.healthcare.exception.UnauthorizedException("Cannot delete your own admin account");
        }
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        userRepository.delete(user);
    }

    public DashboardStatsDTO getDashboardStats() {
        DashboardStatsDTO stats = new DashboardStatsDTO();
        List<User> allUsers = userRepository.findAll();
        List<Doctor> allDoctors = doctorRepository.findAll();
        List<Appointment> allAppointments = appointmentRepository.findAll();
        List<Payment> allPayments = paymentRepository.findAll();

        stats.setTotalUsers(allUsers.size());
        stats.setTotalPatients(userRepository.findByRole(UserRole.PATIENT).size());
        stats.setTotalDoctors(allDoctors.size());
        stats.setVerifiedDoctors(allDoctors.stream().filter(doctor -> Boolean.TRUE.equals(doctor.getIsVerified())).count());
        stats.setPendingDoctors(allDoctors.stream().filter(doctor -> !Boolean.TRUE.equals(doctor.getIsVerified())).count());
        stats.setTotalAdmins(userRepository.findByRole(UserRole.ADMIN).size());
        stats.setActiveUsers(userRepository.findByIsActiveTrue().size());
        stats.setTotalAppointments(allAppointments.size());
        stats.setCompletedAppointments(appointmentRepository.countByStatus(AppointmentStatus.COMPLETED));
        stats.setCancelledAppointments(appointmentRepository.countByStatus(AppointmentStatus.CANCELLED));
        stats.setTodayAppointments(appointmentRepository.countToday(LocalDate.now()));

        Double revenue = paymentRepository.sumCompletedPaymentsBetween(
                LocalDateTime.of(LocalDate.of(2020, 1, 1), LocalTime.MIDNIGHT),
                LocalDateTime.now());
        stats.setTotalRevenue(revenue != null ? revenue : 0.0);
        stats.setTotalReviews(reviewRepository.count());
        stats.setTotalSpecializations(specializationRepository.findAll().size());
        stats.setChartData(buildDashboardChartData(allAppointments, allPayments));
        stats.setRecentUsers(allUsers.stream()
                .sorted(Comparator.comparing(User::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(6)
                .map(this::toRecentUserDTO)
                .collect(Collectors.toList()));
        return stats;
    }

    public RevenueReportDTO getRevenueReport(LocalDate startDate, LocalDate endDate) {
        RevenueReportDTO report = new RevenueReportDTO();
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(23, 59, 59);

        Double revenue = paymentRepository.sumCompletedPaymentsBetween(start, end);
        report.setTotalRevenue(revenue != null ? revenue : 0.0);

        List<Payment> allPayments = paymentRepository.findAll().stream()
                .filter(p -> p.getCreatedAt() != null && !p.getCreatedAt().isBefore(start) && !p.getCreatedAt().isAfter(end))
                .collect(Collectors.toList());

        report.setTotalPayments((long) allPayments.size());
        report.setCompletedPayments(allPayments.stream().filter(p -> p.getStatus() == PaymentStatus.COMPLETED).count());
        report.setPendingPayments(allPayments.stream().filter(p -> p.getStatus() == PaymentStatus.PENDING).count());
        report.setFailedPayments(allPayments.stream().filter(p -> p.getStatus() == PaymentStatus.FAILED).count());

        Map<String, Double> monthly = new LinkedHashMap<>();
        allPayments.stream()
                .filter(p -> p.getStatus() == PaymentStatus.COMPLETED)
                .collect(Collectors.groupingBy(p -> p.getCreatedAt().getYear() + "-" + String.format("%02d", p.getCreatedAt().getMonthValue()),
                        Collectors.summingDouble(Payment::getAmount)))
                .forEach(monthly::put);
        report.setMonthlyBreakdown(monthly);

        return report;
    }

    public Object getAppointmentsReport() {
        List<Appointment> all = appointmentRepository.findAll();
        Map<String, Long> byStatus = all.stream()
                .collect(Collectors.groupingBy(a -> a.getStatus().name(), Collectors.counting()));
        Map<String, Long> byMonth = all.stream()
                .collect(Collectors.groupingBy(a -> a.getAppointmentDate().getYear() + "-" + String.format("%02d", a.getAppointmentDate().getMonthValue()),
                        Collectors.counting()));
        return Map.of("totalAppointments", all.size(), "byStatus", byStatus, "byMonth", byMonth);
    }

    public UserAnalyticsDTO getUserAnalytics() {
        UserAnalyticsDTO analytics = new UserAnalyticsDTO();
        List<User> allUsers = userRepository.findAll();
        analytics.setTotalUsers(allUsers.size());
        analytics.setActiveUsers(userRepository.findByIsActiveTrue().size());
        analytics.setPatientCount(userRepository.findByRole(UserRole.PATIENT).size());
        analytics.setDoctorCount(doctorRepository.findAll().size());
        analytics.setAdminCount(userRepository.findByRole(UserRole.ADMIN).size());

        Map<String, Long> roleDist = new LinkedHashMap<>();
        roleDist.put("PATIENT", analytics.getPatientCount());
        roleDist.put("DOCTOR", analytics.getDoctorCount());
        roleDist.put("ADMIN", analytics.getAdminCount());
        analytics.setRoleDistribution(roleDist);

        Map<String, Long> monthlyRegs = allUsers.stream()
                .filter(u -> u.getCreatedAt() != null)
                .collect(Collectors.groupingBy(u -> u.getCreatedAt().getYear() + "-" + String.format("%02d", u.getCreatedAt().getMonthValue()),
                        Collectors.counting()));
        analytics.setMonthlyRegistrations(monthlyRegs);

        return analytics;
    }

    private List<DashboardTrendDTO> buildDashboardChartData(List<Appointment> appointments, List<Payment> payments) {
        Map<YearMonth, Long> appointmentsByMonth = appointments.stream()
                .filter(appointment -> appointment.getAppointmentDate() != null)
                .collect(Collectors.groupingBy(
                        appointment -> YearMonth.from(appointment.getAppointmentDate()),
                        Collectors.counting()
                ));

        Map<YearMonth, Double> revenueByMonth = payments.stream()
                .filter(payment -> payment.getStatus() == PaymentStatus.COMPLETED && payment.getCreatedAt() != null)
                .collect(Collectors.groupingBy(
                        payment -> YearMonth.from(payment.getCreatedAt()),
                        Collectors.summingDouble(Payment::getAmount)
                ));

        List<DashboardTrendDTO> chartData = new ArrayList<>();
        YearMonth currentMonth = YearMonth.now();
        for (int i = 5; i >= 0; i--) {
            YearMonth month = currentMonth.minusMonths(i);
            chartData.add(new DashboardTrendDTO(
                    month.format(MONTH_FORMAT),
                    revenueByMonth.getOrDefault(month, 0.0),
                    appointmentsByMonth.getOrDefault(month, 0L)
            ));
        }
        return chartData;
    }

    private RecentUserDTO toRecentUserDTO(User user) {
        RecentUserDTO dto = new RecentUserDTO();
        dto.setName(user.getName());
        dto.setRole(user.getRole().name());
        dto.setMobileNumber(user.getMobileNumber());
        dto.setCreatedAt(user.getCreatedAt() != null ? user.getCreatedAt().format(RECENT_USER_FORMAT) : "");
        dto.setStatus(Boolean.TRUE.equals(user.getIsActive()) ? "Active" : "Inactive");
        return dto;
    }

    private PatientResponseDTO toPatientDTO(User user, Patient patient) {
        PatientResponseDTO dto = new PatientResponseDTO();
        dto.setId(patient != null ? patient.getId() : null);
        dto.setUserId(user.getId());
        dto.setName(user.getName());
        dto.setMobileNumber(user.getMobileNumber());
        dto.setEmail(user.getEmail());
        dto.setGender(patient != null ? patient.getGender() : null);
        dto.setDateOfBirth(patient != null ? patient.getDateOfBirth() : null);
        dto.setAddress(patient != null ? patient.getAddress() : null);
        dto.setBloodGroup(patient != null ? patient.getBloodGroup() : null);
        dto.setAllergies(patient != null ? patient.getAllergies() : null);
        dto.setEmergencyContact(patient != null ? patient.getEmergencyContact() : null);
        dto.setCreatedAt(patient != null && patient.getCreatedAt() != null ? patient.getCreatedAt() : user.getCreatedAt());
        return dto;
    }
}
