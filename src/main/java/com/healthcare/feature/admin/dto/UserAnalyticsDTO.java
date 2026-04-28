package com.healthcare.feature.admin.dto;

import java.util.Map;

public class UserAnalyticsDTO {
    private long totalUsers;
    private long activeUsers;
    private long patientCount;
    private long doctorCount;
    private long adminCount;
    private Map<String, Long> roleDistribution;
    private Map<String, Long> monthlyRegistrations;

	public long getTotalUsers() {
		return totalUsers;
	}

	public void setTotalUsers(long totalUsers) {
		this.totalUsers = totalUsers;
	}

	public long getActiveUsers() {
		return activeUsers;
	}

	public void setActiveUsers(long activeUsers) {
		this.activeUsers = activeUsers;
	}

	public long getPatientCount() {
		return patientCount;
	}

	public void setPatientCount(long patientCount) {
		this.patientCount = patientCount;
	}

	public long getDoctorCount() {
		return doctorCount;
	}

	public void setDoctorCount(long doctorCount) {
		this.doctorCount = doctorCount;
	}

	public long getAdminCount() {
		return adminCount;
	}

	public void setAdminCount(long adminCount) {
		this.adminCount = adminCount;
	}

	public Map<String, Long> getRoleDistribution() {
		return roleDistribution;
	}

	public void setRoleDistribution(Map<String, Long> roleDistribution) {
		this.roleDistribution = roleDistribution;
	}

	public Map<String, Long> getMonthlyRegistrations() {
		return monthlyRegistrations;
	}

	public void setMonthlyRegistrations(Map<String, Long> monthlyRegistrations) {
		this.monthlyRegistrations = monthlyRegistrations;
	}
}
