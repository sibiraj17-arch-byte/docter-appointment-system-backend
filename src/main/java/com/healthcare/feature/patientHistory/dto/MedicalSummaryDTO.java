package com.healthcare.feature.patientHistory.dto;

import java.util.List;

public class MedicalSummaryDTO {
    private Long patientId;
    private String patientName;
    private long totalConditions;
    private long activeConditions;
    private long totalAppointments;
    private long completedAppointments;
    private long totalPrescriptions;
    private long totalReports;
    private List<MedicalHistoryResponseDTO> conditions;

    public Long getPatientId() { return patientId; }
    public void setPatientId(Long patientId) { this.patientId = patientId; }
    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }
    public long getTotalConditions() { return totalConditions; }
    public void setTotalConditions(long totalConditions) { this.totalConditions = totalConditions; }
    public long getActiveConditions() { return activeConditions; }
    public void setActiveConditions(long activeConditions) { this.activeConditions = activeConditions; }
    public long getTotalAppointments() { return totalAppointments; }
    public void setTotalAppointments(long totalAppointments) { this.totalAppointments = totalAppointments; }
    public long getCompletedAppointments() { return completedAppointments; }
    public void setCompletedAppointments(long completedAppointments) { this.completedAppointments = completedAppointments; }
    public long getTotalPrescriptions() { return totalPrescriptions; }
    public void setTotalPrescriptions(long totalPrescriptions) { this.totalPrescriptions = totalPrescriptions; }
    public long getTotalReports() { return totalReports; }
    public void setTotalReports(long totalReports) { this.totalReports = totalReports; }
    public List<MedicalHistoryResponseDTO> getConditions() { return conditions; }
    public void setConditions(List<MedicalHistoryResponseDTO> conditions) { this.conditions = conditions; }
}
