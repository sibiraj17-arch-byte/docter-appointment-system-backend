package com.healthcare.feature.admin.dto;

public class DashboardTrendDTO {
    private String month;
    private double revenue;
    private long appointments;

    public DashboardTrendDTO() {
    }

    public DashboardTrendDTO(String month, double revenue, long appointments) {
        this.month = month;
        this.revenue = revenue;
        this.appointments = appointments;
    }

	public String getMonth() {
		return month;
	}

	public void setMonth(String month) {
		this.month = month;
	}

	public double getRevenue() {
		return revenue;
	}

	public void setRevenue(double revenue) {
		this.revenue = revenue;
	}

	public long getAppointments() {
		return appointments;
	}

	public void setAppointments(long appointments) {
		this.appointments = appointments;
	}
}
