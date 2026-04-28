package com.healthcare.exception;

public class DuplicateResourceException extends RuntimeException {
    /**
	 * 
	 */
	private static final long serialVersionUID = -7923055340690517095L;

	public DuplicateResourceException(String message) {
        super(message);
    }
}
