package com.bravson.socialalert.services;

import javax.annotation.Resource;

import org.junit.Before;
import org.junit.Test;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.bravson.socialalert.app.entities.ApplicationUser;
import com.bravson.socialalert.infrastructure.DataServiceTest;

public class UserDetailsServiceTest extends DataServiceTest {

	@Resource(name="userAuth")
	private UserDetailsService service;
	
	@Before
	public void setUp() throws Exception {
		fullImport(ApplicationUser.class);
	}
	
	@Test
	public void loadExistingUser() {
		UserDetails user = service.loadUserByUsername("lucien@test.com");
		assertNotNull(user);
		assertEquals("lucien@test.com", user.getUsername());
		assertEquals("178879f9ceb4af92183e5cd84cb5416097d41f386941316fbf1f2428474c1c78", user.getPassword());
		assertTrue(user.isEnabled());
		assertTrue(user.isCredentialsNonExpired());
		assertTrue(user.isAccountNonLocked());
		assertTrue(user.isAccountNonExpired());
	}
	
	@Test(expected=UsernameNotFoundException.class)
	public void loadNonExistingUser() {
		service.loadUserByUsername("abc");
	}
}
