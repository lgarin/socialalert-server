package com.bravson.socialalert.common.domain;

public interface UserConstants {

	int MIN_PASSWORD_LENGTH = 3;
	int MAX_PASSWORD_LENGTH = 60;
	int MAX_USERNAME_LENGTH = 120;
	int MAX_NICKNAME_LENGTH = 20;
	
	int MAX_LOGIN_RETRY = 3;
	String NICKNAME_PATTERN = "^[a-zA-Z0-9_]*$";
}
