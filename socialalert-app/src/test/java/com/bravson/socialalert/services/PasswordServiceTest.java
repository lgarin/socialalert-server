package com.bravson.socialalert.services;

import javax.annotation.Resource;
import javax.validation.ValidationException;

import org.junit.Test;

import com.bravson.socialalert.app.entities.ApplicationUser;
import com.bravson.socialalert.app.services.PasswordService;
import com.bravson.socialalert.infrastructure.SimpleServiceTest;

public class PasswordServiceTest extends SimpleServiceTest {

	@Resource
	private PasswordService service;
	
	@Test
	public void encodeSimplePassword() {
		ApplicationUser user = new ApplicationUser("lucien@test.com", null, null);
		String password = service.encodePassword(user, "123");
		assertEquals("178879f9ceb4af92183e5cd84cb5416097d41f386941316fbf1f2428474c1c78", password);
	}
	
	@Test
	public void encodeStrongPassword() {
		ApplicationUser user = new ApplicationUser("webapp@test.com", null, null);
		String password = service.encodePassword(user, "Rsp#zD6!eCbd");
		assertEquals("decdf9f4b933670412e35f6f96bd0ea8eed88bf1dcafaa4d29f7aee17d039dff", password);
	}
	
	@Test
	public void checkValidPassword() {
		ApplicationUser user = new ApplicationUser("lucien@test.com", null, "178879f9ceb4af92183e5cd84cb5416097d41f386941316fbf1f2428474c1c78");
		assertTrue(service.isPasswordValid(user, "123"));
	}
	
	@Test
	public void checkInvalidPassword() {
		ApplicationUser user = new ApplicationUser("lucien@test.com", null, "178879f9ceb4af92183e5cd84cb5416097d41f386941316fbf1f2428474c1c78");
		assertFalse(service.isPasswordValid(user, "abc"));
	}
	
	@Test(expected=ValidationException.class)
	public void checkNullPassword() {
		ApplicationUser user = new ApplicationUser("lucien@test.com", null, "178879f9ceb4af92183e5cd84cb5416097d41f386941316fbf1f2428474c1c78");
		service.isPasswordValid(user, null);
	}
}
