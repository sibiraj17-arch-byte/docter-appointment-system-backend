package com.healthcare.feature.availability.repository;

import com.healthcare.entity.AvailabilitySlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface AvailabilitySlotRepository extends JpaRepository<AvailabilitySlot, Long> {
    List<AvailabilitySlot> findByDoctorIdAndSlotDate(Long doctorId, LocalDate date);
    List<AvailabilitySlot> findByDoctorIdAndSlotDateAndIsAvailableTrue(Long doctorId, LocalDate date);
    
    Optional<AvailabilitySlot> findByDoctorIdAndSlotDateAndStartTimeAndIsAvailableTrue(
            Long doctorId, LocalDate date, LocalTime startTime);
    
    Optional<AvailabilitySlot> findByDoctorIdAndSlotDateAndStartTime(
            Long doctorId, LocalDate date, LocalTime startTime);
    
    boolean existsByDoctorIdAndSlotDateAndStartTime(Long doctorId, LocalDate date, LocalTime startTime);
    
    @Query("SELECT s FROM AvailabilitySlot s WHERE s.doctor.id = :doctorId AND s.slotDate >= :startDate AND s.slotDate <= :endDate ORDER BY s.slotDate, s.startTime")
    List<AvailabilitySlot> findSchedule(@Param("doctorId") Long doctorId, 
                                         @Param("startDate") LocalDate startDate, 
                                         @Param("endDate") LocalDate endDate);
    
    void deleteByDoctorIdAndSlotDateAndIsAvailableTrue(Long doctorId, LocalDate date);
}
