package com.bravson.socialalert.app.exceptions;

public class SystemExeption extends RuntimeException {

	private static final long serialVersionUID = 1L;
	
	public SystemExeption() {
	}

	public SystemExeption(String message) {
		super(message);
	}

	public SystemExeption(Throwable cause) {
		super(cause);
	}

	public SystemExeption(String message, Throwable cause) {
		super(message, cause);
	}
}
