package com.bravson.socialalert.services;

import java.util.List;

import javax.annotation.Resource;
import javax.validation.ValidationException;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Value;

import com.bravson.socialalert.app.domain.ActivationEmailTaskPayload;
import com.bravson.socialalert.app.domain.BaseTaskPayload;
import com.bravson.socialalert.app.entities.ApplicationUser;
import com.bravson.socialalert.app.entities.QueuedTask;
import com.bravson.socialalert.app.services.QueuedTaskService;
import com.bravson.socialalert.infrastructure.DataServiceTest;

public class QueuedTaskServiceTest extends DataServiceTest {

	@Resource
	private QueuedTaskService service;
	
	@Value("${task.page.size}")
	private int pageSize;
	
	@Before
	public void setUp() throws Exception {
		fullImport(QueuedTask.class);
		fullImport(ApplicationUser.class);
	}
	
	@Test
	public void resetStalledTask() {
		List<BaseTaskPayload<?>> result = service.resetStalledTasks(pageSize);
		assertNotNull(result);
		assertEquals(1, result.size());
	}
	
	@Test
	public void enqueueActivationEmailTask() {
		service.enqueueTask(new ActivationEmailTaskPayload("lgarin@gmx.ch"));
	}
	
	@Test(expected=ValidationException.class)
	public void enqueueNullTask() {
		service.enqueueTask(null);
	}
	
	@Test
	public void beginUnregistredTask() {
		ActivationEmailTaskPayload payload = new ActivationEmailTaskPayload("lucien@test.com");
		assertFalse(service.beginTask(payload));
	}

	@Test(expected=ValidationException.class)
	public void executeNullTask() {
		service.executeTask(null);
	}
	
	@Test(expected=IllegalStateException.class)
	public void executeUnregistredTask() {
		ActivationEmailTaskPayload payload = new ActivationEmailTaskPayload("lucien@test.com");
		service.executeTask(payload);
	}
	
	@Test(expected=IllegalStateException.class)
	public void executeUnstartedTask() {
		ActivationEmailTaskPayload payload = new ActivationEmailTaskPayload("lucien@test.com");
		service.enqueueTask(payload);
		service.executeTask(payload);
	}
	
	@Test
	public void executeNewlyQueuedTask() {
		ActivationEmailTaskPayload payload = new ActivationEmailTaskPayload("lucien@test.com");
		service.enqueueTask(payload);
		assertTrue(service.beginTask(payload));
		service.executeTask(payload);
	}
}
