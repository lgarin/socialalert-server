package com.bravson.socialalert.app.exceptions;

public class UnsafePasswordException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	
	public UnsafePasswordException() {
	}

	public UnsafePasswordException(String message) {
		super(message);
	}

	public UnsafePasswordException(Throwable cause) {
		super(cause);
	}

	public UnsafePasswordException(String message, Throwable cause) {
		super(message, cause);
	}

}
