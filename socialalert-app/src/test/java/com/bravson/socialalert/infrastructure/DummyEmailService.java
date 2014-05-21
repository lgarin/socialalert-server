package com.bravson.socialalert.infrastructure;

import org.hibernate.validator.internal.constraintvalidators.EmailValidator;
import org.junit.Ignore;

import com.bravson.socialalert.app.services.EmailService;

@Ignore
public class DummyEmailService implements EmailService {

	private EmailValidator validator = new EmailValidator();
	
	@Override
	public boolean isValidEmailAddress(String emailAddress) {
		return validator.isValid(emailAddress, null);
	}
	
	@Override
	public void sendEmail(String senderAddress,String receiverAddress, String subject, String htmlContent) {
		System.out.println(htmlContent);
	}
}
