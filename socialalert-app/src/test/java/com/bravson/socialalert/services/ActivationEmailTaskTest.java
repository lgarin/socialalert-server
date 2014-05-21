package com.bravson.socialalert.services;

import javax.annotation.Resource;

import org.junit.Before;
import org.junit.Test;

import com.bravson.socialalert.app.domain.ActivationEmailTaskPayload;
import com.bravson.socialalert.app.entities.ApplicationUser;
import com.bravson.socialalert.app.services.ActivationEmailTask;
import com.bravson.socialalert.infrastructure.DataServiceTest;

public class ActivationEmailTaskTest extends DataServiceTest {

	@Resource
	private ActivationEmailTask task;

	@Before
	public void setUp() throws Exception {
		fullImport(ApplicationUser.class);
	}
	
	@Test
	public void executeTaskWithVerifiedUser() {
		task.execute(new ActivationEmailTaskPayload("lucien@test.com"));
	}
	
	@Test
	public void executeTaskWithUnverifiedUser() {
		task.execute(new ActivationEmailTaskPayload("unverified@test.com"));
	}
	
	@Test
	public void executeTaskWithNonExistingUser() {
		task.execute(new ActivationEmailTaskPayload("xxx@test.com"));
	}
}
