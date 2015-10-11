package com.bravson.socialalert.services;

import java.util.UUID;

import javax.annotation.Resource;

import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.web.WebAppConfiguration;

import com.bravson.socialalert.app.entities.ApplicationEvent;
import com.bravson.socialalert.app.entities.UserProfile;
import com.bravson.socialalert.app.exceptions.DataMissingException;
import com.bravson.socialalert.app.services.ApplicationEventService;
import com.bravson.socialalert.infrastructure.DataServiceTest;

@WebAppConfiguration
public class ApplicationEventServiceTest extends DataServiceTest {

	@Resource
	private ApplicationEventService service;
	
	@Resource
	private MockHttpServletRequest httpRequest;
	
	@Before
	public void setUp() throws Exception {
		fullImport(UserProfile.class);
	}
	
	@Test
	public void createEventWithParameter() {
		httpRequest.setRemoteAddr("192.168.120.85");
		UUID profileId = UUID.fromString("a95472c0-e0e6-11e2-a28f-0800200c9a77");
		ApplicationEvent event = service.createEvent(profileId, "testAction", "testParameter");
		assertEquals(httpRequest.getRemoteAddr(), event.getIpAddress());
		assertEquals(profileId, event.getProfileId());
		assertEquals("testAction", event.getAction());
		assertNull(event.getCountry());
	}
	
	@Test(expected=DataMissingException.class)
	public void createEventWithInvalidProfile() {
		httpRequest.setRemoteAddr("192.168.120.85");
		UUID profileId = UUID.randomUUID();
		service.createEvent(profileId, "testAction", "testParameter");
	}
}
