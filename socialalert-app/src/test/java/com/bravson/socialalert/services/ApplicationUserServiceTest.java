package com.bravson.socialalert.services;

import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

import javax.annotation.Resource;
import javax.validation.ValidationException;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.bravson.socialalert.app.entities.ApplicationUser;
import com.bravson.socialalert.app.entities.UserProfile;
import com.bravson.socialalert.app.exceptions.NonUniqueException;
import com.bravson.socialalert.app.exceptions.UnsafePasswordException;
import com.bravson.socialalert.app.services.ApplicationUserService;
import com.bravson.socialalert.common.domain.UserConstants;
import com.bravson.socialalert.common.domain.UserContent;
import com.bravson.socialalert.common.domain.UserInfo;
import com.bravson.socialalert.common.domain.UserRole;
import com.bravson.socialalert.common.domain.UserState;
import com.bravson.socialalert.infrastructure.DataServiceTest;

public class ApplicationUserServiceTest extends DataServiceTest {
	
	@Resource
	private ApplicationUserService service;
	
	@Value("${user.page.size}")
	private int pageSize;

	@Before
	public void setUp() throws Exception {
		fullImport(ApplicationUser.class);
		fullImport(UserProfile.class);
	}
	
	@Test
	public void findNonExistingUser() {
		ApplicationUser user = service.findUserByEmail("xxx@test.com");
		assertNull(user);
	}
	
	@Test
	public void findExistingUser() {
		ApplicationUser user = service.findUserByEmail("lucien@test.com");
		assertNotNull(user);
		assertEquals("lucien@test.com", user.getUsername());
	}
	
	@Test(expected=UsernameNotFoundException.class)
	public void getNonExistingUser() {
		service.getUserByEmail("xxx@test.com");
	}
	
	@Test
	public void getExistingUser() {
		ApplicationUser user = service.getUserByEmail("lucien@test.com");
		assertNotNull(user);
		assertEquals("lucien@test.com", user.getUsername());
	}
	
	@Test
	public void insertNewUser() {
		DateTime beforeCreate = DateTime.now();
		ApplicationUser currentUser = service.createUser("test1@test.com", "Test", "hello");
		assertNotNull(currentUser);
		assertEquals("test1@test.com", currentUser.getUsername());
		assertEquals("Test", currentUser.getNickname());
		assertEquals(UserState.UNVERIFIED, currentUser.getState());
		assertTrue(currentUser.hasRole(UserRole.GUEST));
		assertTrue(beforeCreate.compareTo(currentUser.getCreation()) <= 0);
		assertNull(currentUser.getLastLoginSuccess());
	}

	@Test(expected=DuplicateKeyException.class)
	public void testDuplicateUser() {
		service.createUser("lucien@test.com", "Test", "hello");
	}
	
	@Test(expected=NonUniqueException.class)
	public void testNonUniqueNickname() {
		service.createUser("test2@test.com", "sg33g5", "hello");
	}
	
	@Test(expected=UnsafePasswordException.class)
	public void testNewUserWithUnsafePassword() {
		service.createUser("test2@test.com", "Test", "test2@test.com");
	}
	
	@Test
	public void unlockOneUser() {
		int result = service.unlockPageOfUsers(pageSize);
		assertEquals(1, result);
	}
	
	@Test
	public void unlockUserTwice() {
		int result1 = service.unlockPageOfUsers(pageSize);
		assertEquals(1, result1);
		int result2 = service.unlockPageOfUsers(pageSize);
		assertEquals(0, result2);
	}
	
	@Test
	public void generateActivationTokenForActiveUser() {
		String token = service.generateActivationToken("lucien@test.com");
		assertNull(token);
	}
	
	@Test
	public void generateActivationTokenForUnverifiedUser() {
		String token = service.generateActivationToken("unverified@test.com");
		assertEquals("e35cf6fbbe2d534c7caac3e75dca2ee2449ef98b7ecd4631e9f191560d66ce32", token);
	}
	
	@Test
	public void activateUnverifiedUser() {
		ApplicationUser user = service.getUserByEmail("unverified@test.com");
		assertNotNull(user);
		assertTrue(user.hasRole(UserRole.GUEST));
		assertEquals(UserState.UNVERIFIED, user.getState());
		String token = "e35cf6fbbe2d534c7caac3e75dca2ee2449ef98b7ecd4631e9f191560d66ce32";
		ApplicationUser user2 = service.activateUser("unverified@test.com", token);
		assertNotNull(user2);
		assertTrue(user2.hasRole(UserRole.USER));
		assertEquals(UserState.ACTIVE, user2.getState());
	}
	
	@Test(expected=BadCredentialsException.class)
	public void activateUserWithBadToken() {
		String token = "axbn";
		service.activateUser("unverified@test.com", token);
	}
	
	@Test
	public void activateActiveUser() {
		ApplicationUser user = service.getUserByEmail("lucien@test.com");
		assertEquals(UserState.ACTIVE, user.getState());
		assertTrue(user.hasRole(UserRole.USER));
		String token = "125662dd3f6bda97ab0ba2f7f72df811ef7ae868d63e58b54b6bb73e458a94c7";
		ApplicationUser user2 = service.activateUser("lucien@test.com", token);
		assertEquals(UserState.ACTIVE, user2.getState());
		assertTrue(user.hasRole(UserRole.USER));
	}
	
	@Test(expected=UsernameNotFoundException.class)
	public void activateUnknownUser() {
		String token = "125662dd3f6bda97ab0ba2f7f72df811ef7ae868d63e58b54b6bb73e458a94c7";
		service.activateUser("xyz@test.com", token);
	}
	
	@Test
	public void testLoginSuccess() {
		ApplicationUser user = service.getUserByEmail("lucien@test.com");
		UserInfo before = user.toUserInfo();
		assertNull(before.getLastLoginSuccess());
		user = service.updateLastLoginSuccess("lucien@test.com");
		UserInfo after = user.toUserInfo();
		assertNotNull(after.getLastLoginSuccess());
		assertTrue(before.getLastUpdate().isBefore(after.getLastUpdate()));
	}
	
	@Test
	public void testLoginFailure() {
		ApplicationUser user = service.getUserByEmail("lucien@test.com");
		UserInfo before = user.toUserInfo();
		assertNull(before.getLastLoginFailure());
		assertEquals(0, before.getLoginFailureCount());
		user = service.updateLastLoginFailure("lucien@test.com");
		UserInfo after = user.toUserInfo();
		assertNotNull(after.getLastLoginFailure());
		assertEquals(1, after.getLoginFailureCount());
		assertTrue(before.getLastUpdate().isBefore(after.getLastUpdate()));
	}
	
	@Test
	public void clearLoginFailure() {
		ApplicationUser user = service.getUserByEmail("lucien@test.com");
		user = service.updateLastLoginFailure("lucien@test.com");
		UserInfo before = user.toUserInfo();
		assertNotNull(before.getLastLoginFailure());
		assertEquals(1, before.getLoginFailureCount());
		user = service.clearLoginFailures("lucien@test.com");
		UserInfo after = user.toUserInfo();
		assertNull(after.getLastLoginFailure());
		assertEquals(0, after.getLoginFailureCount());
		assertTrue(!before.getLastUpdate().isAfter(after.getLastUpdate()));
	}
	
	@Test
	public void lockUser() {
		ApplicationUser user = service.getUserByEmail("lucien@test.com");
		UserInfo before = user.toUserInfo();
		assertEquals(UserState.ACTIVE, before.getState());
		assertEquals(0, before.getLoginFailureCount());
		for (int i = 0; i < UserConstants.MAX_LOGIN_RETRY; i++) {
			user = service.updateLastLoginFailure("lucien@test.com");
		}
		UserInfo after = user.toUserInfo();
		assertEquals(UserState.LOCKED, after.getState());
		assertEquals(UserConstants.MAX_LOGIN_RETRY, after.getLoginFailureCount());
		assertTrue(before.getLastUpdate().isBefore(after.getLastUpdate()));
	}
	
	@Test
	public void changePassword() {
		ApplicationUser user = service.getUserByEmail("lucien@test.com");
		UserInfo before = user.toUserInfo();
		user = service.changePassword("lucien@test.com", "abc");
		UserInfo after = user.toUserInfo();
		assertTrue(before.getLastUpdate().isBefore(after.getLastUpdate()));
		assertEquals("f1134b21701bf95557a4e586c1fb65935a586bb1552ea09b7bcba8207e0ccd07", user.getPassword());
	}
	
	@Test(expected=UnsafePasswordException.class)
	public void changePasswordWithUnsafePassword() {
		service.changePassword("lucien@test.com", "sg33g5");
	}
	
	@Test
	public void generatePasswordResetTokenForActiveUser() {
		String token = service.generatePasswordResetToken("lucien@test.com");
		assertNull(token);
	}
	
	@Test
	public void generatePasswordResetTokenForLockedUser() {
		String token = service.generatePasswordResetToken("locked@test.com");
		assertEquals("81300a42bf75a1f613529b407ac3079093fe927f35f47db15b79b5c8b92f2afb", token);
	}
	
	@Test
	public void resetPassword() {
		ApplicationUser user = service.getUserByEmail("locked@test.com");
		assertEquals(UserState.LOCKED, user.getState());
		String token = "81300a42bf75a1f613529b407ac3079093fe927f35f47db15b79b5c8b92f2afb";
		ApplicationUser user2 = service.resetPassword("locked@test.com", token, "abc");
		assertEquals(UserState.ACTIVE, user2.getState());
	}
	
	@Test(expected=BadCredentialsException.class)
	public void resetPasswordWithInvalidToken() {
		ApplicationUser user = service.getUserByEmail("locked@test.com");
		assertEquals(UserState.LOCKED, user.getState());
		String token = "xyt";
		ApplicationUser user2 = service.resetPassword("locked@test.com", token, "abc");
		assertEquals(UserState.ACTIVE, user2.getState());
	}
	
	@Test(expected=UsernameNotFoundException.class)
	public void resetPasswordForUnknowUser() {
		String token = "xyt";
		service.resetPassword("xyz@test.com", token, "abc");
	}

	@Test
	public void generateUniqueNickname() {
		String result = service.generateUniqueNickname("UniqueNickname");
		assertEquals("UniqueNickname", result);
	}
	
	@Test(expected=ValidationException.class)
	public void generateUniqueNicknameForNonAlphanumericInput() {
		service.generateUniqueNickname("Include Space");
	}
	
	@Test
	public void generateUniqueNicknameWithConflict() {
		String result = service.generateUniqueNickname("sg33g5");
		assertEquals("sg33g51", result);
	}
	
	@Test
	public void populateCreator() {
		TestUserContent content = new TestUserContent(UUID.fromString("a95472c0-e0e6-11e2-a28f-0800200c9a77"));
		service.populateCreators(Collections.singletonList(content));
		assertEquals("sg33g5", content.getCreator());
	}
	
	@Test
	public void populateCreators() {
		TestUserContent content1 = new TestUserContent(UUID.fromString("a95472c0-e0e6-11e2-a28f-0800200c9a77"));
		TestUserContent content2 = new TestUserContent(UUID.fromString("e7d166ae-9b3f-4405-be0d-fa1567728593"));
		service.populateCreators(Arrays.asList(content1, content2));
		assertEquals("sg33g5", content1.getCreator());
		assertNull(content2.getCreator());
	}
	
	private static class TestUserContent implements UserContent {
		private UUID profileId;
		private String creator;
		private boolean online;
		
		public TestUserContent(UUID profileId) {
			this.profileId = profileId;
		}
		
		@Override
		public String getCreator() {
			return creator;
		}
		@Override
		public UUID getProfileId() {
			return profileId;
		}
		@Override
		public void setCreator(String creator) {
			this.creator = creator;
		}
		@Override
		public boolean isOnline() {
			return online;
		}
		@Override
		public void setOnline(boolean online) {
			this.online = online;
		}
	}
	
	@Test
	public void updateOnlineStatus() {
		TestUserContent content1 = new TestUserContent(UUID.fromString("a95472c0-e0e6-11e2-a28f-0800200c9a77"));
		TestUserContent content2 = new TestUserContent(UUID.fromString("e7d166ae-9b3f-4405-be0d-fa1567728593"));
		service.updateOnlineStatus(Arrays.asList(content1, content2));
		assertFalse(content1.isOnline());
		assertFalse(content2.isOnline());
	}
}

