package com.bravson.socialalert.app.services;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotBlank;
import org.springframework.validation.annotation.Validated;

import com.bravson.socialalert.app.entities.ApplicationUser;

@Validated
public interface PasswordService {

	public boolean isPasswordValid(@NotNull ApplicationUser user, @NotBlank String rawPassword);
	
	public String encodePassword(@NotNull ApplicationUser user, @NotBlank String rawPassword);
}
