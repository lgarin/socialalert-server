package com.bravson.socialalert.common.domain;


public interface ErrorCodes {

	int BAD_CREDENTIALS = -1;
	int CREDENTIAL_EXPIRED = -2;
	int LOCKED_ACCOUNT = -3;
	int ACCESS_DENIED = -4;
	int INVALID_INPUT = -5;
	int NO_CREDENTIALS_FOUND = -6;
	int USER_NOT_FOUND = -7;
	int DUPLICATE_KEY = -8;
	int NON_UNIQUE_INPUT = -9;
	int UNSAFE_PASSWORD = -10;
	int SYSTEM_ERROR = -500;
	int UNSPECIFIED = -999;
}
