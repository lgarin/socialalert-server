package com.bravson.socialalert.app.services;

import java.util.Collection;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.validation.annotation.Validated;

import com.bravson.socialalert.app.entities.ApplicationUser;
import com.bravson.socialalert.common.domain.UserContent;

@Validated
public interface ApplicationUserService {

	int unlockPageOfUsers(int pageSize);
	
	String generateActivationToken(@NotEmpty String username);
	
	ApplicationUser activateUser(@NotEmpty String username, @NotEmpty String token);

	ApplicationUser findUserByEmail(@NotEmpty String email);
	
	ApplicationUser getUserByEmail(@NotEmpty String email);
	
	ApplicationUser changePassword(@NotEmpty String username, @NotEmpty String newPassword);
	
	ApplicationUser updateLastLoginFailure(@NotEmpty String username);
	
	ApplicationUser updateLastLoginSuccess(@NotEmpty String username);
	
	ApplicationUser clearLoginFailures(@NotEmpty String username);

	String generatePasswordResetToken(@NotEmpty String username);
	
	ApplicationUser resetPassword(@NotEmpty String username, @NotEmpty String token, @NotEmpty String newPassword);

	ApplicationUser createUser(@NotBlank String username, @NotBlank String nickname, @NotBlank String password);
	
	String generateUniqueNickname(@NotEmpty String baseNickname);

	<T extends UserContent> void populateCreators(List<T> items);
	
	void updateOnlineStatus(@NotNull Collection<? extends UserContent> profiles);
}
