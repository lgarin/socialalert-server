package com.bravson.socialalert.app.exceptions;

public class NonUniqueException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public NonUniqueException() {
	}

	public NonUniqueException(String message) {
		super(message);
	}

	public NonUniqueException(Throwable cause) {
		super(cause);
	}

	public NonUniqueException(String message, Throwable cause) {
		super(message, cause);
	}

}
