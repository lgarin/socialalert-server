package com.bravson.socialalert.services;

import java.lang.reflect.Field;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.security.access.AuthorizationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.util.ReflectionUtils;

import com.bravson.socialalert.app.infrastructure.JsonServerErrorResolver;
import com.bravson.socialalert.common.domain.ErrorCodes;
import com.googlecode.jsonrpc4j.ErrorResolver.JsonError;

public class JsonServerErrorResolverTest extends Assert implements ErrorCodes {

	private JsonServerErrorResolver errorResolver = new JsonServerErrorResolver();
	
	private void assertErrorCode(Throwable t, int errorCode) {
		JsonError error = errorResolver.resolveError(t, null, null);
		Field codeField = ReflectionUtils.findField(JsonError.class, "code");
		ReflectionUtils.makeAccessible(codeField);
		assertEquals(errorCode, ReflectionUtils.getField(codeField, error));
		Field messageField = ReflectionUtils.findField(JsonError.class, "message");
		ReflectionUtils.makeAccessible(messageField);
		assertEquals(t.getMessage(), ReflectionUtils.getField(messageField, error));
		Field dataField = ReflectionUtils.findField(JsonError.class, "data");
		ReflectionUtils.makeAccessible(dataField);
		assertEquals(t.getClass().getName(), ReflectionUtils.getField(dataField, error));
	}
	
	@Test
	public void testBadCredentialsException() {
		assertErrorCode(new BadCredentialsException("test"), BAD_CREDENTIALS);
	}
	
	@Test
	public void testAccessDeniedExceptionSubclass() {
		assertErrorCode(new AuthorizationServiceException("test"), ACCESS_DENIED);
	}
}
