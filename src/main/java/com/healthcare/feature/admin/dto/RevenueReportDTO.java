package com.healthcare.feature.admin.dto;

import java.util.Map;

public class RevenueReportDTO {
    private double totalRevenue;
    private long totalPayments;
    private long completedPayments;
    private long pendingPayments;
    private long failedPayments;
    private Map<String, Double> monthlyBreakdown;

	public double getTotalRevenue() {
		return totalRevenue;
	}

	public void setTotalRevenue(double totalRevenue) {
		this.totalRevenue = totalRevenue;
	}

	public long getTotalPayments() {
		return totalPayments;
	}

	public void setTotalPayments(long totalPayments) {
		this.totalPayments = totalPayments;
	}

	public long getCompletedPayments() {
		return completedPayments;
	}

	public void setCompletedPayments(long completedPayments) {
		this.completedPayments = completedPayments;
	}

	public long getPendingPayments() {
		return pendingPayments;
	}

	public void setPendingPayments(long pendingPayments) {
		this.pendingPayments = pendingPayments;
	}

	public long getFailedPayments() {
		return failedPayments;
	}

	public void setFailedPayments(long failedPayments) {
		this.failedPayments = failedPayments;
	}

	public Map<String, Double> getMonthlyBreakdown() {
		return monthlyBreakdown;
	}

	public void setMonthlyBreakdown(Map<String, Double> monthlyBreakdown) {
		this.monthlyBreakdown = monthlyBreakdown;
	}
}
