package com.healthcare.exception;

public class InvalidOTPException extends RuntimeException {
    /**
	 * 
	 */
	private static final long serialVersionUID = 3931781056632103660L;

	public InvalidOTPException(String message) {
        super(message);
    }
}
