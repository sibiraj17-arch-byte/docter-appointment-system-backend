package com.healthcare.feature.patientHistory.repository;

import com.healthcare.entity.MedicalDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MedicalDocumentRepository extends JpaRepository<MedicalDocument, Long> {
    List<MedicalDocument> findByMedicalHistoryPatientId(Long patientId);
    List<MedicalDocument> findByMedicalHistoryId(Long medicalHistoryId);
}
