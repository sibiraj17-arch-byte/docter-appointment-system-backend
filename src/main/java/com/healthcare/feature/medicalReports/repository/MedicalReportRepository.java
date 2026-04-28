package com.healthcare.feature.medicalReports.repository;

import com.healthcare.entity.MedicalReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface MedicalReportRepository extends JpaRepository<MedicalReport, Long> {
    List<MedicalReport> findByPatientId(Long patientId);
    
    @Query("SELECT r FROM MedicalReport r WHERE r.patient.id = :patientId AND r.createdAt BETWEEN :start AND :end ORDER BY r.createdAt DESC")
    List<MedicalReport> findByPatientIdAndDateRange(@Param("patientId") Long patientId,
                                                      @Param("start") LocalDateTime start,
                                                      @Param("end") LocalDateTime end);
}
