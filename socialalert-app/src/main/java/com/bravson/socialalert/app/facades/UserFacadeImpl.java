package com.bravson.socialalert.app.facades;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.UUID;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.ValidationException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.openid.OpenIDAuthenticationToken;
import org.springframework.security.web.session.HttpSessionCreatedEvent;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import com.bravson.socialalert.app.domain.ActivationEmailTaskPayload;
import com.bravson.socialalert.app.domain.ExternalProfileInfo;
import com.bravson.socialalert.app.domain.PasswordResetEmailTaskPayload;
import com.bravson.socialalert.app.domain.StoreProfilePictureTaskPayload;
import com.bravson.socialalert.app.entities.ApplicationUser;
import com.bravson.socialalert.app.entities.UserProfile;
import com.bravson.socialalert.app.services.ApplicationEventService;
import com.bravson.socialalert.app.services.ApplicationUserService;
import com.bravson.socialalert.app.services.EmailService;
import com.bravson.socialalert.app.services.OAuthAuthenicationService;
import com.bravson.socialalert.app.services.PasswordService;
import com.bravson.socialalert.app.services.UserProfileService;
import com.bravson.socialalert.app.tasks.QueuedTaskScheduler;
import com.bravson.socialalert.app.utilities.SecurityUtils;
import com.bravson.socialalert.common.domain.UserInfo;
import com.bravson.socialalert.common.domain.UserRole;
import com.bravson.socialalert.common.domain.UserState;
import com.bravson.socialalert.common.facade.UserFacade;

@Service
@Validated
public class UserFacadeImpl implements UserFacade, ApplicationListener<HttpSessionCreatedEvent>  {

	@Resource
	private ApplicationUserService userService;
	
	@Resource(name="authenticationManager")
	private AuthenticationManager authManager;
	
	@Resource
	private PasswordService passwordService;
	
	@Resource
	private EmailService emailService;
	
	@Resource
	private QueuedTaskScheduler queuedTaskScheduler;
	
	@Resource
	private OAuthAuthenicationService oAuthService;
	
	@Resource
	private UserProfileService profileService;
	
	@Resource
	private ApplicationEventService eventService;
	
	@Resource
	private QueuedTaskScheduler taskScheduler;
	
	@Resource
	private SessionRegistry sessionRegistry;

	@Autowired(required=false)
	private HttpServletRequest httpRequest;
	
	@Value("${profile.picture.retryCount}")
	private int downloadRetryCount;
	
	@Transactional(rollbackFor={Throwable.class})
	public UserInfo create(String username, String nickname, String password) {
		if (!emailService.isValidEmailAddress(username)) {
			throw new ValidationException("Invalid email domain for email " + username);
		}
		
		ApplicationUser user = userService.createUser(username, nickname, password);
		queuedTaskScheduler.scheduleTask(new ActivationEmailTaskPayload(username));
		return user.toUserInfo();
	}
	
	@Override
	@Transactional(noRollbackFor={BadCredentialsException.class},rollbackFor={Throwable.class})
	public UserInfo login(String username, String password) {
		
		Authentication auth;
		try {
			auth = authManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
		} catch (BadCredentialsException e) {
			userService.updateLastLoginFailure(username);
			throw e;
		}
		ApplicationUser user = SecurityUtils.findPrincipal(auth);
		if (user != null && user.getProfileId() != null) {
			eventService.createEvent(user.getProfileId(), "login", username);	
		}
		
		if (user != null) {
			user = userService.updateLastLoginSuccess(username);
			SecurityContextHolder.getContext().setAuthentication(auth);
			return user.toUserInfo();
		} else {
			throw new AuthenticationServiceException("No user detail found");
		}
	}
	
	@Override
	public UserInfo getCurrentUser() {
		ApplicationUser user = SecurityUtils.findAuthenticatedPrincipal();
		return user != null ? user.toUserInfo() : null;
	}

	private UserInfo completeExternalLogin(ExternalProfileInfo info) {
		ApplicationUser user = userService.findUserByEmail(info.getEmail());
		if (user == null) {
			user = userService.createUser(info.getEmail(), info.getEmail(), UUID.randomUUID().toString());
			String activationToken = userService.generateActivationToken(info.getEmail());
			user = userService.activateUser(info.getEmail(), activationToken);
		} else if (user != null && user.getState() == UserState.UNVERIFIED) {
			// TODO should we warn that password will be changed?
			userService.changePassword(info.getEmail(), UUID.randomUUID().toString());
			String activationToken = userService.generateActivationToken(info.getEmail());
			user = userService.activateUser(info.getEmail(), activationToken);
		}
	
		UserProfile profile = profileService.completeProfile(user.getProfileId(), info);
		if (profile.getImage() == null && info.getImage() != null) {
			taskScheduler.scheduleTask(new StoreProfilePictureTaskPayload(user.getProfileId(), info.getImage(), downloadRetryCount));
		}
		
		SimpleGrantedAuthority authority = new SimpleGrantedAuthority(UserRole.USER.name());
		OpenIDAuthenticationToken auth = new OpenIDAuthenticationToken(user, Collections.singletonList(authority), info.getIdentifier(), null);
		if (user != null && user.getProfileId() != null) {
			eventService.createEvent(user.getProfileId(), "externalLogin", info.getIdentifier());	
		}
		user = userService.updateLastLoginSuccess(user.getUsername());
		SecurityContextHolder.getContext().setAuthentication(auth);
		return user.toUserInfo();
	}
	
	@Override
	public URL beginOAuthLogin(String providerId, URL successUrl) {
		return oAuthService.beginOAuthConsumption(providerId, successUrl);
	}
	
	@Override
	public UserInfo completeOAuthLogin(URL receivingUrl) {
		ExternalProfileInfo oAuthInfo = oAuthService.endOAuthConsumption(receivingUrl);
		return completeExternalLogin(oAuthInfo);
	}
	
	@Override
	@Transactional(noRollbackFor={BadCredentialsException.class},rollbackFor={Throwable.class})
	public void changePassword(String username, String oldPassword, String newPassword) {
		ApplicationUser user = userService.getUserByEmail(username);
		if (!passwordService.isPasswordValid(user, oldPassword)) {
			userService.updateLastLoginFailure(username);
			throw new BadCredentialsException("Invalid credential");
		}
		if (passwordService.isPasswordValid(user, newPassword)) {
			throw new CredentialsExpiredException("New password must differ from previous one");
		}
		userService.changePassword(username, newPassword);
		
		if (user.getProfileId() != null) {
			eventService.createEvent(user.getProfileId(), "changePassword", username);	
		}
	}
	
	@Override
	@PreAuthorize("isAuthenticated()")
	@Transactional(rollbackFor={Throwable.class})
	public void logout() {
		ApplicationUser user = SecurityUtils.findAuthenticatedPrincipal();
		if (user != null && user.getProfileId() != null) {
			eventService.createEvent(user.getProfileId(), "logout", user.getUsername());	
		}
		if (user != null) {
			userService.clearLoginFailures(user.getUsername());
		}
		SecurityContextHolder.clearContext();
		if (httpRequest != null && httpRequest.getSession(false) != null) {
			httpRequest.getSession().invalidate();
		}
	}
	
	@Override
	@PreAuthorize("isAuthenticated()")
	@Transactional(rollbackFor={Throwable.class})
	public void activateUser(String token) {
		ApplicationUser user = SecurityUtils.findAuthenticatedPrincipal();
		userService.activateUser(user.getUsername(), token);
		if (user != null && user.getProfileId() != null) {
			eventService.createEvent(user.getProfileId(), "activateUser", user.getUsername());	
		}
	}
	
	@Override
	@Transactional(rollbackFor={Throwable.class})
	public void initiatePasswordReset(String username) throws IOException {
		ApplicationUser user = userService.getUserByEmail(username);
		queuedTaskScheduler.scheduleTask(new PasswordResetEmailTaskPayload(user.getUsername()));
	}
	
	@Override
	@Transactional(rollbackFor={Throwable.class})
	public void resetPassword(String username, String token, String newPassword) throws IOException {
		userService.resetPassword(username, token, newPassword);
		/* TODO
		if (user != null && user.getProfileId() != null) {
			eventService.createEvent(user.getProfileId(), "resetPassword", user.getUsername());	
		}
		*/
	}
	
	@Override
	public String generateUniqueNickname(String baseNickname) throws IOException {
		return userService.generateUniqueNickname(baseNickname);
	}
	
	@Override
	public void onApplicationEvent(HttpSessionCreatedEvent event) {
		if (SecurityContextHolder.getContext().getAuthentication() != null) {
			sessionRegistry.registerNewSession(event.getSession().getId(), SecurityContextHolder.getContext().getAuthentication().getPrincipal());
		}
	}
}
