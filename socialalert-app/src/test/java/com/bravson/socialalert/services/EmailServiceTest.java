package com.bravson.socialalert.services;

import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;

import com.bravson.socialalert.app.services.EmailService;
import com.bravson.socialalert.infrastructure.SimpleServiceTest;

public class EmailServiceTest extends SimpleServiceTest {

	@Autowired
	@Qualifier("realEmailService")
	private EmailService service;
	
	@Value("${user.activation.sender}")
	private String senderAddress;
	
	@Test
	public void checkValidEmail() {
		assertTrue(service.isValidEmailAddress("lucien@test.com"));
	}
	
	@Test
	public void checkNoDomain() {
		assertFalse(service.isValidEmailAddress("lucien"));
	}
	
	@Test
	public void checkDomainOnly() {
		assertFalse(service.isValidEmailAddress("@test.com"));
	}
	
	@Test
	public void checkInvalidDomain() {
		assertFalse(service.isValidEmailAddress("lucien@a345n.iop"));
	}
	
	@Test
	public void checkIpAddress() {
		assertFalse(service.isValidEmailAddress("lucien@8.8.8.8"));
	}
	
	@Ignore
	@Test
	public void sendEmail() {
		service.sendEmail(senderAddress, "lgarin@gmx.ch", "test", "<h1>hello2</h1>");
	}
}
