package com.healthcare.feature.appointments.repository;

import com.healthcare.entity.Appointment;
import com.healthcare.enums.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    Optional<Appointment> findByCode(String code);
    List<Appointment> findByPatientId(Long patientId);
    List<Appointment> findByDoctorId(Long doctorId);
    List<Appointment> findByPatientIdAndStatus(Long patientId, AppointmentStatus status);
    List<Appointment> findByDoctorIdAndStatus(Long doctorId, AppointmentStatus status);

    @Query("SELECT a FROM Appointment a WHERE a.patient.id = :patientId AND a.appointmentDate >= :today ORDER BY a.appointmentDate, a.startTime")
    List<Appointment> findUpcomingByPatient(@Param("patientId") Long patientId, @Param("today") LocalDate today);
    
    @Query("SELECT a FROM Appointment a WHERE a.doctor.id = :doctorId AND a.appointmentDate >= :today ORDER BY a.appointmentDate, a.startTime")
    List<Appointment> findUpcomingByDoctor(@Param("doctorId") Long doctorId, @Param("today") LocalDate today);

    List<Appointment> findByDoctorIdAndAppointmentDate(Long doctorId, LocalDate date);
    
    @Query("SELECT a FROM Appointment a WHERE a.appointmentDate BETWEEN :startDate AND :endDate ORDER BY a.appointmentDate, a.startTime")
    List<Appointment> findByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    @Query("SELECT a FROM Appointment a WHERE a.doctor.id = :doctorId AND a.appointmentDate BETWEEN :startDate AND :endDate ORDER BY a.appointmentDate, a.startTime")
    List<Appointment> findByDoctorAndDateRange(@Param("doctorId") Long doctorId, 
                                                @Param("startDate") LocalDate startDate, 
                                                @Param("endDate") LocalDate endDate);

    boolean existsByDoctorIdAndAppointmentDateAndStartTimeAndStatusNot(
            Long doctorId, LocalDate date, LocalTime startTime, AppointmentStatus status);
    
    long countByStatus(AppointmentStatus status);
    
    @Query("SELECT a FROM Appointment a WHERE a.doctor.id = :doctorId AND a.appointmentDate = :today")
    List<Appointment> findTodayByDoctor(@Param("doctorId") Long doctorId, @Param("today") LocalDate today);
    
    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.appointmentDate = :today")
    long countToday(@Param("today") LocalDate today);   
}
