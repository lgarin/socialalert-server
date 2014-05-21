package com.bravson.socialalert.app.exceptions;

public class DataMissingException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public DataMissingException() {
	}

	public DataMissingException(String message) {
		super(message);
	}

	public DataMissingException(Throwable cause) {
		super(cause);
	}

	public DataMissingException(String message, Throwable cause) {
		super(message, cause);
	}

}
