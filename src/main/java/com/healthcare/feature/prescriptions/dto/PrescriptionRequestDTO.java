package com.healthcare.feature.prescriptions.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

public class PrescriptionRequestDTO {
    private Long appointmentId;
    private Long patientId;

    @NotBlank(message = "Diagnosis is required")
    @Size(max = 1000)
    private String diagnosis;

    @Size(max = 2000)
    private String instructions;

    @Size(max = 500)
    private String followUp;

    @NotEmpty(message = "At least one medicine item is required")
    @Valid
    private List<PrescriptionItemDTO> items;

    public Long getAppointmentId() { return appointmentId; }
    public void setAppointmentId(Long appointmentId) { this.appointmentId = appointmentId; }
    public Long getPatientId() { return patientId; }
    public void setPatientId(Long patientId) { this.patientId = patientId; }
    public String getDiagnosis() { return diagnosis; }
    public void setDiagnosis(String diagnosis) { this.diagnosis = diagnosis; }
    public String getInstructions() { return instructions; }
    public void setInstructions(String instructions) { this.instructions = instructions; }
    public String getFollowUp() { return followUp; }
    public void setFollowUp(String followUp) { this.followUp = followUp; }
    public List<PrescriptionItemDTO> getItems() { return items; }
    public void setItems(List<PrescriptionItemDTO> items) { this.items = items; }
}
