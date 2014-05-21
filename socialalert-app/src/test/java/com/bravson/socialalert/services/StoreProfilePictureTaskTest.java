package com.bravson.socialalert.services;

import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;

import javax.annotation.Resource;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

import com.bravson.socialalert.app.domain.StoreProfilePictureTaskPayload;
import com.bravson.socialalert.app.entities.UserProfile;
import com.bravson.socialalert.app.services.StoreProfilePictureTask;
import com.bravson.socialalert.app.tasks.QueuedTaskScheduler;
import com.bravson.socialalert.infrastructure.DataServiceTest;

public class StoreProfilePictureTaskTest extends DataServiceTest {

	@Resource
	private StoreProfilePictureTask task;
	
	@Resource
	private QueuedTaskScheduler scheduler;

	@Before
	public void setUp() throws Exception {
		fullImport(UserProfile.class);
		scheduler = createMock(task, "taskScheduler", QueuedTaskScheduler.class);
	}
	
	@Test
	public void executeTaskSuccessfully() throws MalformedURLException {
		UUID profileId = UUID.fromString("a95472c0-e0e6-11e2-a28f-0800200c9a77");
		URL pictureUrl = new URL("http://www.w3.org/MarkUp/Test/xhtml-print/20050519/tests/jpeg420exif.jpg");
		replay(scheduler);
		task.execute(new StoreProfilePictureTaskPayload(profileId, pictureUrl, 1));
		verify(scheduler);
	}
	
	@Test
	public void storeInvalidUrlWithRetry() throws MalformedURLException {
		UUID profileId = UUID.fromString("a95472c0-e0e6-11e2-a28f-0800200c9a77");
		URL pictureUrl = new URL("http://www.w3.org/test123.jpg");
		scheduler.scheduleTask(EasyMock.isA(StoreProfilePictureTaskPayload.class));
		EasyMock.expectLastCall().once();
		replay(scheduler);
		task.execute(new StoreProfilePictureTaskPayload(profileId, pictureUrl, 1));
		verify(scheduler);
	}
	
	@Test
	public void storeInvalidUrlWithoutRetry() throws MalformedURLException {
		UUID profileId = UUID.fromString("a95472c0-e0e6-11e2-a28f-0800200c9a77");
		URL pictureUrl = new URL("http://www.w3.org/test123.jpg");
		replay(scheduler);
		task.execute(new StoreProfilePictureTaskPayload(profileId, pictureUrl, 0));
		verify(scheduler);
	}
}
