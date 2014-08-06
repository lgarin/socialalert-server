package com.bravson.socialalert.services;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;

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
	
	@Test
	public void addNewRepostedUri() throws URISyntaxException {
		assertTrue(service.addRepostedUri(new URI("new")));
	}
	
	@Test
	public void addSameRepostedUriTwice() throws URISyntaxException {
		assertTrue(service.addRepostedUri(new URI("new2")));
		assertFalse(service.addRepostedUri(new URI("new2")));
	}
	
	@Test
	public void addNewRepostedComment() throws URISyntaxException {
		assertTrue(service.addRepostedComment(UUID.randomUUID()));
	}
	
	@Test
	public void addSameRepostedCommentTwice() throws URISyntaxException {
		UUID commentId = UUID.randomUUID();
		assertTrue(service.addRepostedComment(commentId));
		assertFalse(service.addRepostedComment(commentId));
	}
}
