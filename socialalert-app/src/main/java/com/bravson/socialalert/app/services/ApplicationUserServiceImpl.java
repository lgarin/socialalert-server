package com.bravson.socialalert.app.services;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

import javax.annotation.Resource;
import javax.validation.ValidationException;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.encoding.PasswordEncoder;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bravson.socialalert.app.entities.ApplicationUser;
import com.bravson.socialalert.app.entities.UserProfile;
import com.bravson.socialalert.app.exceptions.NonUniqueException;
import com.bravson.socialalert.app.exceptions.UnsafePasswordException;
import com.bravson.socialalert.app.repositories.ApplicationUserRepository;
import com.bravson.socialalert.common.domain.UserConstants;
import com.bravson.socialalert.common.domain.UserContent;
import com.bravson.socialalert.common.domain.UserState;

@SuppressWarnings("deprecation")
@Service
public class ApplicationUserServiceImpl implements ApplicationUserService {

	private static final Pattern NICKNAME_PATTERN = Pattern.compile(UserConstants.NICKNAME_PATTERN);
	
	@Resource
	private ApplicationUserRepository userRepository;
	
	@Resource
	private PasswordEncoder passwordEncoder;
	
	@Resource
	private PasswordService passwordService;
	
	@Resource
	private UserProfileService profileService;
	
	@Resource
	private SessionRegistry sessionRegistry;
	
	@Value("${user.unlock.delay}")
	private long unlockDelay;
	
	@Value("${nickname.page.size}")
	private int pageSize;
	
	@Override
	@Transactional(rollbackFor={Throwable.class})
	public int unlockPageOfUsers(int pageSize) {
		int count = 0;
		Collection<ApplicationUser> users = userRepository.findByState(UserState.LOCKED, unlockDelay, new PageRequest(0, pageSize));
		users = userRepository.lockAll(users);
		for (ApplicationUser user : users) {
			count++;
			user.unlock();
		}
		
		if (count > 0) {
			userRepository.save(users);
		}
		
		return count;
	}
	
	@Override
	public String generateActivationToken(String username) {
		ApplicationUser user = userRepository.findById(username);
		if (user == null) {
			return null;
		}
		if (user.getState() != UserState.UNVERIFIED) {
			return null;
		}
		return passwordEncoder.encodePassword(user.getUsername(), user.getCreation().toString());
	}
	
	@Transactional(rollbackFor={Throwable.class})
	@Override
	public ApplicationUser activateUser(String username, String token) {
		ApplicationUser user = userRepository.lockById(username);
		if (user == null) {
			throw new UsernameNotFoundException("Cannot find user "  + username);
		}
		if (!passwordEncoder.isPasswordValid(token, user.getUsername(), user.getCreation().toString())) {
			throw new BadCredentialsException("Invalid token for user " + username);
		}
		UserProfile profile = profileService.createEmptyProfile(user.getNickname());
		user.activate(profile);
		return userRepository.save(user);
	}
	
	@Override
	public ApplicationUser getUserByEmail(String email) {
		ApplicationUser user = userRepository.findById(email);
		if (user == null) {
			throw new UsernameNotFoundException("Cannot find user "  + email);
		}
		return user;
	}
	
	@Override
	public ApplicationUser findUserByEmail(String email) {
		return userRepository.findById(email);
	}
	
	@Transactional(rollbackFor={Throwable.class})
	@Override
	public ApplicationUser changePassword(String username, String newPassword) {
		ApplicationUser user = userRepository.lockById(username);
		if (user == null) {
			return null;
		}
		if (user.isUnsafePassword(newPassword)) {
			throw new UnsafePasswordException("The given password is not safe enough");
		}
		String encodedPassword = passwordService.encodePassword(user, newPassword);
		user.changePassword(encodedPassword);
		return userRepository.save(user);
	}
	
	@Transactional(rollbackFor={Throwable.class})
	@Override
	public ApplicationUser updateLastLoginFailure(String username) {
		ApplicationUser user = userRepository.lockById(username);
		if (user == null) {
			return null;
		}
		user.updateLastLoginFailure();
		return userRepository.save(user);
	}
	
	@Transactional(rollbackFor={Throwable.class})
	@Override
	public ApplicationUser updateLastLoginSuccess(String username) {
		ApplicationUser user = userRepository.lockById(username);
		if (user == null) {
			return null;
		}
		user.updateLastLoginSuccess();
		return userRepository.save(user);
	}
	
	@Transactional(rollbackFor={Throwable.class})
	@Override
	public ApplicationUser clearLoginFailures(String username) {
		ApplicationUser user = userRepository.lockById(username);
		if (user == null) {
			return null;
		}
		if (user.hasLoginFailure()) {
			user.clearLoginFailures();
			userRepository.save(user);
		}
		return user;
	}
	
	@Override
	public String generatePasswordResetToken(String username) {
		ApplicationUser user = userRepository.findById(username);
		if (user == null) {
			return null;
		}
		if (user.getLastLoginFailure() == null || user.getState() != UserState.LOCKED) {
			return null;
		}
		return passwordEncoder.encodePassword(user.getUsername(), user.getLastLoginFailure().toString());
	}

	@Transactional(rollbackFor={Throwable.class})
	@Override
	public ApplicationUser resetPassword(String username, String token, String newPassword) {
		ApplicationUser user = userRepository.lockById(username);
		if (user == null) {
			throw new UsernameNotFoundException("Cannot find user "  + username);
		}
		if (!passwordEncoder.isPasswordValid(token, user.getUsername(), user.getLastLoginFailure().toString())) {
			throw new BadCredentialsException("Invalid token for user " + username);
		}
		user.changePassword(newPassword);
		return userRepository.save(user);
	}
	
	@Override
	public ApplicationUser createUser(String username, String nickname, String password) {
		if (findUserByEmail(username) != null) {
			throw new DuplicateKeyException("The user " + username + " already exists");
		}
		
		ApplicationUser user = new ApplicationUser(username, nickname, password);
		if (user.isUnsafePassword(password)) {
			throw new UnsafePasswordException("The given password is not safe enough");
		}
		String uniqueNickname = generateUniqueNickname(nickname);
		if (!ObjectUtils.equals(nickname, uniqueNickname)) {
			throw new NonUniqueException("The given nickname is not unique. Try with '" + uniqueNickname + "'.");
		}
		
		String encodedPassword = passwordService.encodePassword(user, password);
		user.changePassword(encodedPassword);
		return userRepository.save(user);
	}
	
	@Override
	public <T extends UserContent> void populateCreators(List<T> items) {
		HashMap<UUID, ApplicationUser> profileIdMap = new HashMap<>(items.size());
		for (T item : items) {
			profileIdMap.put(item.getProfileId(), null);
		}
		List<ApplicationUser> users = getUsersByProfileIds(profileIdMap.keySet());
		for (ApplicationUser user : users) {
			profileIdMap.put(user.getProfileId(), user);
		}
		for (T item : items) {
			ApplicationUser user = profileIdMap.get(item.getProfileId());
			if (user != null) {
				item.setCreator(user.getNickname());
			}
		}
	}
	
	private List<ApplicationUser> getUsersByProfileIds(Collection<UUID> profileIds) {
		if (profileIds.isEmpty()) {
			return Collections.emptyList();
		}
		return userRepository.findByProfileIds(profileIds, new PageRequest(0, profileIds.size()));
	}
	
	@Override
	public String generateUniqueNickname(String baseNickname) {
		if (!NICKNAME_PATTERN.matcher(baseNickname).matches()) {
			throw new ValidationException("The nickname must be alphanumeric");
		}
		
		Set<String> existingNicknames = userRepository.queryForTerms("nickname", baseNickname, pageSize);
		if (existingNicknames.isEmpty()) {
			return baseNickname;
		}
		
		for (int i = 1; i <= pageSize; i++) {
			String nickname = baseNickname + String.valueOf(i);
			if (!existingNicknames.contains(nickname)) {
				return nickname;
			}
		}
		throw new NonUniqueException("The nickname " + baseNickname + " is used too frequently");
	}
	
	@Override
	public void updateOnlineStatus(Collection<? extends UserContent> items) {
		HashMap<UUID, Boolean> onlineMap = new HashMap<>(items.size());
		for (UserContent item : items) {
			onlineMap.put(item.getProfileId(), Boolean.FALSE);
		}
		List<Object> principals = sessionRegistry.getAllPrincipals();
		for (Object principal : principals) {
			if (principal instanceof ApplicationUser) {
				ApplicationUser user = (ApplicationUser) principal;
				if (user.getProfileId() != null && onlineMap.containsKey(user.getProfileId())) {
					onlineMap.put(user.getProfileId(), Boolean.TRUE);
				}
			}
		}
		for (UserContent item : items) {
			item.setOnline(onlineMap.get(item.getProfileId()));
		}
	}
}
