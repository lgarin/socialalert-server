package com.bravson.socialalert.services;

import javax.annotation.Resource;

import org.junit.Before;
import org.junit.Test;

import com.bravson.socialalert.app.domain.PasswordResetEmailTaskPayload;
import com.bravson.socialalert.app.entities.ApplicationUser;
import com.bravson.socialalert.app.services.PasswordResetEmailTask;
import com.bravson.socialalert.infrastructure.DataServiceTest;

public class PasswordResetEmailTaskTest extends DataServiceTest {

	@Resource
	private PasswordResetEmailTask task;

	@Before
	public void setUp() throws Exception {
		fullImport(ApplicationUser.class);
	}
	
	@Test
	public void executeTaskWithActiveUser() {
		task.execute(new PasswordResetEmailTaskPayload("lucien@test.com"));
	}
	
	@Test
	public void executeTaskWithLockedUser() {
		task.execute(new PasswordResetEmailTaskPayload("locked@test.com"));
	}
	
	@Test
	public void executeTaskWithNonExistingUser() {
		task.execute(new PasswordResetEmailTaskPayload("xxx@test.com"));
	}
}
