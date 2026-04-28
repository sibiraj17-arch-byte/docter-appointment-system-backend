package com.healthcare.feature.billing.repository;

import com.healthcare.entity.Payment;
import com.healthcare.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByAppointmentId(Long appointmentId);
    Optional<Payment> findByRazorpayOrderId(String razorpayOrderId);
    List<Payment> findByPatientId(Long patientId);
    List<Payment> findByStatus(PaymentStatus status);
    boolean existsByAppointmentId(Long appointmentId);
    
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.status = 'COMPLETED' AND p.createdAt BETWEEN :start AND :end")
    Double sumCompletedPaymentsBetween(@Param("start") java.time.LocalDateTime start, @Param("end") java.time.LocalDateTime end);
}
