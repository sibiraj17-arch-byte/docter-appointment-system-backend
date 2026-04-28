package com.healthcare.feature.patientHistory.repository;

import com.healthcare.entity.MedicalHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface MedicalHistoryRepository extends JpaRepository<MedicalHistory, Long> {
    List<MedicalHistory> findByPatientId(Long patientId);
    List<MedicalHistory> findByPatientIdAndCreatedAtBetween(Long patientId, LocalDateTime startDate, LocalDateTime endDate);
    boolean existsByIdAndPatientId(Long id, Long patientId);
    
    @Query("SELECT mh FROM MedicalHistory mh WHERE mh.patient.id = :patientId AND LOWER(mh.condition) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<MedicalHistory> searchByPatientAndKeyword(@Param("patientId") Long patientId, @Param("keyword") String keyword);
}
