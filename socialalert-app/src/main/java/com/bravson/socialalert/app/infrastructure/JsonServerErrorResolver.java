package com.bravson.socialalert.app.infrastructure;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.validation.ValidationException;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.bravson.socialalert.app.exceptions.NonUniqueException;
import com.bravson.socialalert.app.exceptions.SystemExeption;
import com.bravson.socialalert.app.exceptions.UnsafePasswordException;
import com.bravson.socialalert.common.domain.ErrorCodes;
import com.fasterxml.jackson.databind.JsonNode;
import com.googlecode.jsonrpc4j.ErrorResolver;

public class JsonServerErrorResolver implements ErrorResolver, ErrorCodes {
	
	private Map<Class<? extends Throwable>, Integer> errorMap = new ConcurrentHashMap<>();
	

	public JsonServerErrorResolver() {
		errorMap.put(BadCredentialsException.class, BAD_CREDENTIALS);
		errorMap.put(CredentialsExpiredException.class, CREDENTIAL_EXPIRED);
		errorMap.put(LockedException.class, LOCKED_ACCOUNT);
		errorMap.put(AccessDeniedException.class, ACCESS_DENIED);
		errorMap.put(ValidationException.class, INVALID_INPUT);
		errorMap.put(AuthenticationCredentialsNotFoundException.class, NO_CREDENTIALS_FOUND);
		errorMap.put(UsernameNotFoundException.class, USER_NOT_FOUND);
		errorMap.put(DuplicateKeyException.class, DUPLICATE_KEY);
		errorMap.put(NonUniqueException.class, NON_UNIQUE_INPUT);
		errorMap.put(SystemExeption.class, SYSTEM_ERROR);
		errorMap.put(UnsafePasswordException.class, UNSAFE_PASSWORD);
	}
	
	public void setErrorMap(Map<Class<? extends Throwable>, Integer> entries) {
		errorMap.putAll(entries);
	}
	
	@Override
	public JsonError resolveError(Throwable t, Method method, List<JsonNode> arguments) {
		
		Class<? extends Throwable> errorClass = t.getClass();
		Integer errorCode = errorMap.get(errorClass);
		if (errorCode == null) {
			errorCode = findSuperclassErrorCode(errorClass);
			errorMap.put(errorClass, errorCode);
		}
		return new JsonError(errorCode, t.getMessage(), t.getClass().getName());
	}

	@SuppressWarnings("unchecked")
	private Integer findSuperclassErrorCode(Class<? extends Throwable> currentClass) {
		if (currentClass == Throwable.class) {
			return UNSPECIFIED;
		}
		Class<? extends Throwable> superclass = (Class<? extends Throwable>) currentClass.getSuperclass();
		Integer errorCode = errorMap.get(superclass);
		if (errorCode != null) {
			return errorCode;
		}
		return findSuperclassErrorCode(superclass);
	}
}
