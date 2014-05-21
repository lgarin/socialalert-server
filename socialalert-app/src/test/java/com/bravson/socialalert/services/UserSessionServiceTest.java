package com.bravson.socialalert.services;

import java.net.URI;
import java.net.URISyntaxException;

import javax.annotation.Resource;

import org.junit.Test;

import com.bravson.socialalert.app.services.UserSessionService;
import com.bravson.socialalert.infrastructure.SimpleServiceTest;

public class UserSessionServiceTest extends SimpleServiceTest {

	@Resource
	private UserSessionService service;

	@Test
	public void addNewViewedUri() throws URISyntaxException {
		assertTrue(service.addViewedUri(new URI("new")));
	}
	
	@Test
	public void addSameViewedUriTwice() throws URISyntaxException {
		assertTrue(service.addViewedUri(new URI("new2")));
		assertFalse(service.addViewedUri(new URI("new2")));
	}
}
