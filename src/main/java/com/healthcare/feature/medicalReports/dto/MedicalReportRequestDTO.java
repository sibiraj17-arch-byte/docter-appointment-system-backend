package com.healthcare.feature.medicalReports.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class MedicalReportRequestDTO {
    private Long appointmentId;
    private Long patientId;

    @NotBlank(message = "Report type is required")
    @Size(max = 200)
    private String reportType;

    @NotBlank(message = "Title is required")
    @Size(max = 300)
    private String title;

    @Size(max = 5000)
    private String description;

    @Size(max = 5000)
    private String findings;

    @Size(max = 2000)
    private String recommendations;

    public Long getAppointmentId() { return appointmentId; }
    public void setAppointmentId(Long appointmentId) { this.appointmentId = appointmentId; }
    public Long getPatientId() { return patientId; }
    public void setPatientId(Long patientId) { this.patientId = patientId; }
    public String getReportType() { return reportType; }
    public void setReportType(String reportType) { this.reportType = reportType; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getFindings() { return findings; }
    public void setFindings(String findings) { this.findings = findings; }
    public String getRecommendations() { return recommendations; }
    public void setRecommendations(String recommendations) { this.recommendations = recommendations; }
}
