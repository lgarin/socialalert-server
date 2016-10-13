package com.bravson.socialalert.common.facade;

import java.io.IOException;
import java.net.URL;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.NotEmpty;

import com.bravson.socialalert.common.domain.UserConstants;
import com.bravson.socialalert.common.domain.UserInfo;
import com.googlecode.jsonrpc4j.JsonRpcParam;
import com.googlecode.jsonrpc4j.JsonRpcService;

@JsonRpcService("userFacade")
public interface UserFacade {

	UserInfo create(@JsonRpcParam("email") @NotBlank @Size(max=UserConstants.MAX_USERNAME_LENGTH) String username, @JsonRpcParam("nickname") @NotBlank @Size(max=UserConstants.MAX_NICKNAME_LENGTH) String nickname, @JsonRpcParam("password") @Length(min=UserConstants.MIN_PASSWORD_LENGTH, max=UserConstants.MAX_PASSWORD_LENGTH) String password) throws IOException;
	
	UserInfo login(@JsonRpcParam("email") @NotBlank String username, @JsonRpcParam("password") @NotBlank String password) throws IOException;

	UserInfo getCurrentUser() throws IOException;
	
	URL beginOAuthLogin(@JsonRpcParam("providerId") @NotEmpty String providerId, @JsonRpcParam("successUrl") @NotNull URL successUrl) throws IOException;

	public UserInfo completeOAuthLogin(@JsonRpcParam("receivingUrl") @NotNull URL receivingUrl) throws IOException;
	
	void logout() throws IOException;

	void changePassword(@JsonRpcParam("email") @NotBlank String username, @JsonRpcParam("oldPassword") @NotBlank String oldPassword, @JsonRpcParam("newPassword") @Length(min=UserConstants.MIN_PASSWORD_LENGTH, max=UserConstants.MAX_PASSWORD_LENGTH) String newPassword) throws IOException;
	
	void activateUser(@JsonRpcParam("token") @NotBlank String token) throws IOException;
	
	void initiatePasswordReset(@JsonRpcParam("email") @NotBlank String username) throws IOException;
	
	void resetPassword(@JsonRpcParam("email") @NotBlank String username, @JsonRpcParam("token") @NotBlank String token, @JsonRpcParam("newPassword") @Length(min=UserConstants.MIN_PASSWORD_LENGTH, max=UserConstants.MAX_PASSWORD_LENGTH) String newPassword) throws IOException;
	
	String generateUniqueNickname(@JsonRpcParam("baseNickname") @NotBlank String baseNickname) throws IOException;
}
