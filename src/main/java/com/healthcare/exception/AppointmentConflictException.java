package com.healthcare.exception;

public class AppointmentConflictException extends RuntimeException {
    /**
	 * 
	 */
	private static final long serialVersionUID = 2263351237454913130L;

	public AppointmentConflictException(String message) {
        super(message);
    }
}
