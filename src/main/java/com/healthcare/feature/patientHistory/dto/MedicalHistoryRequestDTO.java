package com.healthcare.feature.patientHistory.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public class MedicalHistoryRequestDTO {
    @NotBlank(message = "Condition is required")
    @Size(max = 300)
    private String condition;

    @Size(max = 1000)
    private String diagnosis;

    private LocalDate diagnosisDate;

    @Size(max = 100)
    private String status;

    @Size(max = 2000)
    private String notes;

    @Size(max = 200)
    private String treatment;

    public String getCondition() { return condition; }
    public void setCondition(String condition) { this.condition = condition; }
    public String getDiagnosis() { return diagnosis; }
    public void setDiagnosis(String diagnosis) { this.diagnosis = diagnosis; }
    public LocalDate getDiagnosisDate() { return diagnosisDate; }
    public void setDiagnosisDate(LocalDate diagnosisDate) { this.diagnosisDate = diagnosisDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public String getTreatment() { return treatment; }
    public void setTreatment(String treatment) { this.treatment = treatment; }
}
