package com.bravson.socialalert.facades;

import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.annotation.Resource;
import javax.validation.ValidationException;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.bravson.socialalert.app.domain.ExternalProfileInfo;
import com.bravson.socialalert.app.entities.ApplicationUser;
import com.bravson.socialalert.app.entities.UserProfile;
import com.bravson.socialalert.app.services.OpenIdAuthenticationService;
import com.bravson.socialalert.common.domain.UserConstants;
import com.bravson.socialalert.common.domain.UserInfo;
import com.bravson.socialalert.common.domain.UserRole;
import com.bravson.socialalert.common.domain.UserState;
import com.bravson.socialalert.common.facade.UserFacade;
import com.bravson.socialalert.infrastructure.DataServiceTest;

public class UserFacadeTest extends DataServiceTest {
	
	@Resource
	private UserFacade facade;
	
	private OpenIdAuthenticationService openIdService;
	
	@Before
	public void setUp() throws Exception {
		fullImport(ApplicationUser.class);
		fullImport(UserProfile.class);
		SecurityContextHolder.clearContext();
		openIdService = createMock(facade, "openIdService", OpenIdAuthenticationService.class);
	}
	
	@Test(expected=ValidationException.class)
	public void testNullLogin() throws IOException {
		facade.login(null, "134");
	}
	
	@Test
	public void testInitialData() throws Exception {
		UserInfo currentUser = facade.login("lucien@test.com", "123");
		assertNotNull(currentUser);
		assertEquals("lucien@test.com", currentUser.getEmail());
		assertNotNull(SecurityContextHolder.getContext().getAuthentication());
		assertTrue(SecurityContextHolder.getContext().getAuthentication().isAuthenticated());
	}
	
	@Test
	public void testGuestLogin() throws Exception {
		UserInfo currentUser = facade.login("unverified@test.com", "123");
		assertNotNull(currentUser);
		assertEquals("unverified@test.com", currentUser.getEmail());
		assertNotNull(SecurityContextHolder.getContext().getAuthentication());
		assertTrue(SecurityContextHolder.getContext().getAuthentication().isAuthenticated());
	}
	
	@Test(expected=LockedException.class)
	public void testLockedAccountLogin() throws Exception {
		facade.login("locked@test.com", "123");
	}
	
	@Test(expected=CredentialsExpiredException.class)
	public void testLoginWithExpiredPassword() throws Exception {
		facade.login("webapp@test.com", "Rsp#zD6!eCbd");
	}
	
	@Test
	public void insertNewUser() throws IOException {
		DateTime beforeCreate = DateTime.now();
		UserInfo currentUser = facade.create("test1@test.com", "Test", "hello");
		assertNotNull(currentUser);
		assertEquals("test1@test.com", currentUser.getEmail());
		assertEquals("Test", currentUser.getNickname());
		assertEquals(UserState.UNVERIFIED, currentUser.getState());
		assertTrue(currentUser.hasRole(UserRole.GUEST));
		assertTrue(beforeCreate.compareTo(currentUser.getCreation()) <= 0);
		assertNull(currentUser.getLastLoginSuccess());
	}
	
	@Test(expected=ValidationException.class)
	public void insertInvalidNewUserWithInvalidEmail() throws IOException {
		facade.create("test1", "Test", "hello");
	}
	
	@Test(expected=DuplicateKeyException.class)
	public void testDuplicateUser()  throws IOException {
		UserInfo currentUser = facade.create("test2@test.com", "Test", "hello");
		assertNotNull(currentUser);
		facade.create("test2@test.com", "Test2", "hello");
	}
	
	@Test
	public void testNewUserLogin() throws Exception {
		UserInfo info = facade.create("test3@test.com", "Test", "hello");
		assertNull(info.getLastLoginSuccess());
		UserInfo info2 = facade.login("test3@test.com", "hello");
		assertNotNull(info2.getLastLoginSuccess());
	}

	@Test(expected=BadCredentialsException.class)
	public void testInvalidLogin() throws IOException {
		facade.login("test@test.com", "123");
	}
	
	@Test(expected=AuthenticationCredentialsNotFoundException.class)
	public void testLogoutWithoutLogin() throws IOException {
		facade.logout();
	}
	
	@Test
	public void testLoginLogout() throws IOException {
		facade.login("lucien@test.com", "123");
		facade.logout();
		assertNull(SecurityContextHolder.getContext().getAuthentication());
	}
	
	@Test
	public void testLoginFailure() throws IOException, InterruptedException {
		DateTime beforeFailure = DateTime.now();
		try {
			facade.login("lucien@test.com", "abc");
			assertFalse(true);
		} catch (BadCredentialsException e) {
			assertTrue(true);
		}
		UserInfo info = facade.login("lucien@test.com", "123");
		assertEquals(1, info.getLoginFailureCount());
		assertNotNull(info.getLastLoginFailure());
		assertTrue(beforeFailure.compareTo(info.getLastLoginFailure()) <= 0);
		facade.logout();
		assertNull(SecurityContextHolder.getContext().getAuthentication());
		UserInfo info2 = facade.login("lucien@test.com", "123");
		assertEquals(0, info2.getLoginFailureCount());
		assertNull(info2.getLastLoginFailure());
	}
	
	@Test(expected=LockedException.class)
	public void testAccountLocked() throws IOException, InterruptedException {
		for (int i = 0; i <= UserConstants.MAX_LOGIN_RETRY; i++) {
			try {
				facade.login("lucien@test.com", "abc");
				assertFalse(true);
			} catch (BadCredentialsException e) {
				assertTrue(true);
			}
		}
		facade.login("lucien@test.com", "123");
	}
	
	@Test
	public void testChangePassword() throws IOException, InterruptedException {
		facade.changePassword("lucien@test.com", "123", "abc");
		facade.login("lucien@test.com", "abc");
	}
	
	@Test(expected=ValidationException.class)
	public void testChangePasswordTooShort() throws IOException {
		facade.changePassword("lucien@test.com", "123", "a");
	}
	
	@Test
	public void testChangePasswordWithBadCredential() throws IOException, InterruptedException {
		try {
			facade.changePassword("lucien@test.com", "zzz", "abc");
			assertFalse(true);
		} catch (BadCredentialsException e) {
			assertTrue(true);
		}
		UserInfo info = facade.login("lucien@test.com", "123");
		assertEquals(1, info.getLoginFailureCount());
		assertNotNull(info.getLastLoginFailure());
	}
	
	@Test(expected=CredentialsExpiredException.class)
	public void testUnchangedPassword() throws IOException {
		facade.changePassword("lucien@test.com", "123", "123");
	}
	
	@Test(expected=UsernameNotFoundException.class)
	public void testChangePasswordWithUnknownUser() throws IOException {
		facade.changePassword("xyz@test.com", "123", "123");
	}
	
	@Test(expected=BadCredentialsException.class)
	public void activateUserWithBadToken() throws IOException {
		facade.login("unverified@test.com", "123");
		facade.activateUser("xyz");
	}
	
	@Test
	public void activateActiveUser() throws IOException {
		facade.login("lucien@test.com", "123");
		facade.activateUser("125662dd3f6bda97ab0ba2f7f72df811ef7ae868d63e58b54b6bb73e458a94c7");
	}
	
	@Test
	public void activateGuestUser() throws IOException {
		facade.login("unverified@test.com", "123");
		facade.activateUser("e35cf6fbbe2d534c7caac3e75dca2ee2449ef98b7ecd4631e9f191560d66ce32");
	}
	
	@Test(expected=AuthenticationCredentialsNotFoundException.class)
	public void activateUserWithoutLogin() throws IOException {
		facade.activateUser("xyz");
	}
	
	@Test
	public void initiatePasswordReset() throws IOException {
		facade.initiatePasswordReset("lucien@test.com");
	}
	
	@Test(expected=UsernameNotFoundException.class)
	public void initiatePasswordResetWithUnknownUser() throws IOException {
		facade.initiatePasswordReset("xyz@test.com");
	}
	
	@Test
	public void resetPassword() throws IOException {
		String token = "81300a42bf75a1f613529b407ac3079093fe927f35f47db15b79b5c8b92f2afb";
		facade.resetPassword("locked@test.com", token, "abc");
	}
	
	@Test(expected=BadCredentialsException.class)
	public void resetPasswordWithBadToken() throws IOException {
		facade.resetPassword("locked@test.com", "badtoken", "abc");
	}

	@Test(expected=UsernameNotFoundException.class)
	public void resetPasswordForUnknownUser() throws IOException {
		facade.resetPassword("xyz@test.com", "badtoken", "123");
	}
	
	
	@Test
	public void beginGoogleLogin() throws MalformedURLException, IOException {
		URL providerUrl = new URL("https://www.google.com/accounts/o8/id");
		URL redirectUrl = new URL("http://localhost:9092/loginSuccess");
		expect(openIdService.beginOpenIdConsumption(eq(providerUrl), eq(redirectUrl))).andReturn(new URL("https://www.google.com/accounts/o8/ud"));
		replay(openIdService);
		URL url = facade.beginOpenIdLogin(providerUrl, redirectUrl);
		assertNotNull(url);
		assertTrue(url.toString().startsWith("https://www.google.com/accounts/o8/ud"));
		assertNull(SecurityContextHolder.getContext().getAuthentication());
		verify(openIdService);
	}
	
	public void completeOpenIdLoginForNewUser() throws MalformedURLException, IOException {
		URL response = new URL("http://localhost:9092/loginSuccess?openid.mode=id_res");
		ExternalProfileInfo openIdInfo = new ExternalProfileInfo();
		openIdInfo.setEmail("newUser@test.com");
		openIdInfo.setNickname("NewUser");
		openIdInfo.setIdentifier("newUser");
		expect(openIdService.endOpenIdConsumption(eq(response))).andReturn(openIdInfo);
		replay(openIdService);
		DateTime beforeLogin = DateTime.now();
		UserInfo userInfo = facade.completeOpenIdLogin(response);
		assertEquals("newUser@test.com", userInfo.getEmail());
		assertEquals("NewUser", userInfo.getNickname());
		assertTrue(beforeLogin.isBefore(userInfo.getCreation()));
		assertTrue(beforeLogin.isBefore(userInfo.getLastLoginSuccess()));
		assertEquals(0, userInfo.getLoginFailureCount());
		assertNull(userInfo.getLastLoginFailure());
		assertEquals(UserState.ACTIVE, userInfo.getState());
		assertTrue(userInfo.hasRole(UserRole.USER));
		assertNotNull(SecurityContextHolder.getContext().getAuthentication());
		assertTrue(SecurityContextHolder.getContext().getAuthentication().isAuthenticated());
		verify(openIdService);
	}
	
	public void completeOpenIdLoginForGuestUser() throws MalformedURLException, IOException {
		URL response = new URL("http://localhost:9092/loginSuccess?openid.mode=id_res");
		ExternalProfileInfo openIdInfo = new ExternalProfileInfo();
		openIdInfo.setEmail("unverified@test.com");
		openIdInfo.setNickname("anotherNickname");
		openIdInfo.setIdentifier("unverified2");
		expect(openIdService.endOpenIdConsumption(eq(response))).andReturn(openIdInfo);
		replay(openIdService);
		DateTime beforeLogin = DateTime.now();
		UserInfo userInfo = facade.completeOpenIdLogin(response);
		assertEquals("unverified@test.com", userInfo.getEmail());
		assertEquals("unverified", userInfo.getNickname());
		assertTrue(beforeLogin.isAfter(userInfo.getCreation()));
		assertTrue(beforeLogin.isBefore(userInfo.getLastLoginSuccess()));
		assertEquals(0, userInfo.getLoginFailureCount());
		assertNull(userInfo.getLastLoginFailure());
		assertEquals(UserState.ACTIVE, userInfo.getState());
		assertTrue(userInfo.hasRole(UserRole.USER));
		assertNotNull(SecurityContextHolder.getContext().getAuthentication());
		assertTrue(SecurityContextHolder.getContext().getAuthentication().isAuthenticated());
		verify(openIdService);
	}
	
	public void completeOpenIdLoginForActiveUser() throws MalformedURLException, IOException {
		URL response = new URL("http://localhost:9092/loginSuccess?openid.mode=id_res");
		ExternalProfileInfo openIdInfo = new ExternalProfileInfo();
		openIdInfo.setEmail("lucien@test.com");
		openIdInfo.setNickname("anotherNickname");
		openIdInfo.setIdentifier("unverified2");
		expect(openIdService.endOpenIdConsumption(eq(response))).andReturn(openIdInfo);
		replay(openIdService);
		DateTime beforeLogin = DateTime.now();
		UserInfo userInfo = facade.completeOpenIdLogin(response);
		assertEquals("lucien@test.com", userInfo.getEmail());
		assertEquals("sg33g5", userInfo.getNickname());
		assertTrue(beforeLogin.isAfter(userInfo.getCreation()));
		assertTrue(beforeLogin.isBefore(userInfo.getLastLoginSuccess()));
		assertEquals(0, userInfo.getLoginFailureCount());
		assertNull(userInfo.getLastLoginFailure());
		assertEquals(UserState.ACTIVE, userInfo.getState());
		assertTrue(userInfo.hasRole(UserRole.USER));
		assertNotNull(SecurityContextHolder.getContext().getAuthentication());
		assertTrue(SecurityContextHolder.getContext().getAuthentication().isAuthenticated());
		verify(openIdService);
	}
	
	@Test(expected=BadCredentialsException.class)
	public void completeGoogleLoginWithBadCredential() throws MalformedURLException, IOException {
		URL response = new URL("http://localhost:9092/loginSuccess?openid.mode=id_res");
		expect(openIdService.endOpenIdConsumption(eq(response))).andThrow(new BadCredentialsException("expected"));
		replay(openIdService);
		facade.completeOpenIdLogin(response);
		verify(openIdService);
	}
	
	@Test
	public void generateUniqueNickname() throws IOException {
		String result = facade.generateUniqueNickname("UniqueNickname");
		assertEquals("UniqueNickname", result);
	}
	
	@Test(expected=ValidationException.class)
	public void generateUniqueNicknameForNonAlphanumericInput() throws IOException {
		facade.generateUniqueNickname("Include Space");
	}
	
	@Test
	public void generateUniqueNicknameWithConflict() throws IOException {
		String result = facade.generateUniqueNickname("sg33g5");
		assertEquals("sg33g51", result);
	}
}
