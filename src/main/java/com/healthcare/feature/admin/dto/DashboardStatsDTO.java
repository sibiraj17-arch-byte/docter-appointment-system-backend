package com.healthcare.feature.admin.dto;

import java.util.List;

public class DashboardStatsDTO {
    private long totalUsers;
    private long totalPatients;
    private long totalDoctors;
    private long verifiedDoctors;
    private long pendingDoctors;
    private long totalAdmins;
    private long activeUsers;
    private long totalAppointments;
    private long completedAppointments;
    private long cancelledAppointments;
    private long todayAppointments;
    private double totalRevenue;
    private long totalReviews;
    private long totalSpecializations;
    private List<DashboardTrendDTO> chartData;
    private List<RecentUserDTO> recentUsers;

	public long getTotalUsers() {
		return totalUsers;
	}

	public void setTotalUsers(long totalUsers) {
		this.totalUsers = totalUsers;
	}

	public long getTotalPatients() {
		return totalPatients;
	}

	public void setTotalPatients(long totalPatients) {
		this.totalPatients = totalPatients;
	}

	public long getTotalDoctors() {
		return totalDoctors;
	}

	public void setTotalDoctors(long totalDoctors) {
		this.totalDoctors = totalDoctors;
	}

	public long getVerifiedDoctors() {
		return verifiedDoctors;
	}

	public void setVerifiedDoctors(long verifiedDoctors) {
		this.verifiedDoctors = verifiedDoctors;
	}

	public long getPendingDoctors() {
		return pendingDoctors;
	}

	public void setPendingDoctors(long pendingDoctors) {
		this.pendingDoctors = pendingDoctors;
	}

	public long getTotalAdmins() {
		return totalAdmins;
	}

	public void setTotalAdmins(long totalAdmins) {
		this.totalAdmins = totalAdmins;
	}

	public long getActiveUsers() {
		return activeUsers;
	}

	public void setActiveUsers(long activeUsers) {
		this.activeUsers = activeUsers;
	}

	public long getTotalAppointments() {
		return totalAppointments;
	}

	public void setTotalAppointments(long totalAppointments) {
		this.totalAppointments = totalAppointments;
	}

	public long getCompletedAppointments() {
		return completedAppointments;
	}

	public void setCompletedAppointments(long completedAppointments) {
		this.completedAppointments = completedAppointments;
	}

	public long getCancelledAppointments() {
		return cancelledAppointments;
	}

	public void setCancelledAppointments(long cancelledAppointments) {
		this.cancelledAppointments = cancelledAppointments;
	}

	public long getTodayAppointments() {
		return todayAppointments;
	}

	public void setTodayAppointments(long todayAppointments) {
		this.todayAppointments = todayAppointments;
	}

	public double getTotalRevenue() {
		return totalRevenue;
	}

	public void setTotalRevenue(double totalRevenue) {
		this.totalRevenue = totalRevenue;
	}

	public long getTotalReviews() {
		return totalReviews;
	}

	public void setTotalReviews(long totalReviews) {
		this.totalReviews = totalReviews;
	}

	public long getTotalSpecializations() {
		return totalSpecializations;
	}

	public void setTotalSpecializations(long totalSpecializations) {
		this.totalSpecializations = totalSpecializations;
	}

	public List<DashboardTrendDTO> getChartData() {
		return chartData;
	}

	public void setChartData(List<DashboardTrendDTO> chartData) {
		this.chartData = chartData;
	}

	public List<RecentUserDTO> getRecentUsers() {
		return recentUsers;
	}

	public void setRecentUsers(List<RecentUserDTO> recentUsers) {
		this.recentUsers = recentUsers;
	}
}
