package com.bravson.socialalert.app.services;

import org.hibernate.validator.constraints.NotBlank;
import org.springframework.validation.annotation.Validated;

@Validated
public interface EmailService {

	public boolean isValidEmailAddress(@NotBlank String emailAddress);
	
	public void sendEmail(@NotBlank String senderAddress, @NotBlank String receiverAddress, @NotBlank String subject, @NotBlank String htmlContent);
}
