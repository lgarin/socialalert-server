package com.bravson.socialalert.services;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import javax.annotation.Resource;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;

import com.bravson.socialalert.app.entities.AlertActivity;
import com.bravson.socialalert.app.entities.AlertMedia;
import com.bravson.socialalert.app.exceptions.DataMissingException;
import com.bravson.socialalert.app.services.AlertActivityService;
import com.bravson.socialalert.common.domain.ActivityCount;
import com.bravson.socialalert.common.domain.ActivityInfo;
import com.bravson.socialalert.common.domain.ActivityType;
import com.bravson.socialalert.common.domain.QueryResult;
import com.bravson.socialalert.infrastructure.DataServiceTest;

public class AlertActivityServiceTest extends DataServiceTest {

	@Resource
	private AlertActivityService service;

	@Before
	public void setUp() throws Exception {
		fullImport(AlertMedia.class);
		fullImport(AlertActivity.class);
	}
	
	@Test
	public void createNewActivity() {
		UUID profileId = UUID.fromString("12345678-e0e6-11e2-a28f-0800200c9a77");
		URI mediaUri = URI.create("20130716/f317f3c7918c83ff6ec24aabb6c017fd.jpg");
		ActivityInfo activity = service.addActivity(mediaUri, profileId, ActivityType.LIKE_MEDIA, null);
		assertNotNull(activity);
		assertEquals(profileId, activity.getProfileId());
		assertEquals(mediaUri, activity.getMediaUri());
		assertEquals(ActivityType.LIKE_MEDIA, activity.getActivityType());
		assertNull(activity.getMessage());
		assertNull(activity.getCreator());
		assertNotNull(activity.getTimestamp());
	}
	
	@Test(expected=DataMissingException.class)
	public void createNewActivityForInvalidMedia() {
		UUID profileId = UUID.fromString("12345678-e0e6-11e2-a28f-0800200c9a77");
		URI mediaUri = URI.create("xyz.jpg");
		service.addActivity(mediaUri, profileId, ActivityType.LIKE_MEDIA, null);
	}
	
	@Test
	public void searchActivity() {
		UUID profileId = UUID.fromString("a95472c0-e0e6-11e2-a28f-0800200c9a77");
		QueryResult<ActivityInfo> result = service.searchActivityBySourceProfileId(Collections.singletonList(profileId), 0, 10);
		assertNotNull(result);
		assertEquals(3, result.getContent().size());
		assertTrue(result.getContent().get(0).getTimestamp().isAfter(result.getContent().get(1).getTimestamp()));
		assertTrue(result.getContent().get(1).getTimestamp().isAfter(result.getContent().get(2).getTimestamp()));
	}
	
	@Test
	public void getActivityStatistic() {
		UUID profileId = UUID.fromString("a95472c0-e0e6-11e2-a28f-0800200c9a77");
		List<ActivityCount> statistic = service.getRecentActivityStatistic(Collections.singletonList(profileId), new DateTime(2000, 1, 1, 0, 0));
		assertNotNull(statistic);
		assertEquals(3, statistic.size());
	}
	
	@Test
	public void getEmptyActivityStatistic() {
		List<ActivityCount> statistic = service.getRecentActivityStatistic(Collections.<UUID>emptyList(), new DateTime(2000, 1, 1, 0, 0));
		assertNotNull(statistic);
		assertEquals(0, statistic.size());
	}
	
	@Test
	public void getRecentActivityStatistic() {
		UUID profileId = UUID.fromString("a95472c0-e0e6-11e2-a28f-0800200c9a77");
		List<ActivityCount> statistic = service.getRecentActivityStatistic(Collections.singletonList(profileId), new DateTime(2013, 8, 13, 13, 0));
		assertNotNull(statistic);
		assertEquals(1, statistic.size());
		ActivityCount activity = statistic.get(0);
		assertEquals(1, activity.getCount());
		assertEquals(ActivityType.NEW_COMMENT, activity.getType());
	}
}
