package com.bravson.socialalert.services;

import java.util.UUID;

import javax.annotation.Resource;

import org.junit.Before;
import org.junit.Test;

import com.bravson.socialalert.app.domain.ProfileStatisticUpdate;
import com.bravson.socialalert.app.entities.ProfileStatistic;
import com.bravson.socialalert.app.services.ProfileStatisticService;
import com.bravson.socialalert.common.domain.QueryResult;
import com.bravson.socialalert.common.domain.ProfileStatisticInfo;
import com.bravson.socialalert.infrastructure.DataServiceTest;

public class ProfileStatisticServiceTest extends DataServiceTest {

	@Resource
	private ProfileStatisticService service;

	@Before
	public void setUp() throws Exception {
		fullImport(ProfileStatistic.class);
	}
	
	@Test
	public void readExistingStatistic() {
		UUID profileId = UUID.fromString("a95472c0-e0e6-11e2-a28f-0800200c9a77");
		ProfileStatisticInfo info = service.getProfileStatistic(profileId);
		assertNotNull(info);
		assertEquals(1432, info.getHitCount());
		assertEquals(42, info.getPictureCount());
		assertEquals(67, info.getCommentCount());
		assertEquals(560, info.getLikeCount());
		assertEquals(10, info.getDislikeCount());
	}
	
	@Test
	public void readNonExistingStatistic() {
		UUID profileId = UUID.fromString("b95472c0-e0e6-11e2-a28f-0800200c9a72");
		ProfileStatisticInfo info = service.getProfileStatistic(profileId);
		assertNotNull(info);
		assertEquals(0, info.getHitCount());
		assertEquals(0, info.getPictureCount());
		assertEquals(0, info.getCommentCount());
		assertEquals(0, info.getLikeCount());
		assertEquals(0, info.getDislikeCount());
	}
	
	@Test
	public void updateExistingStatistic() {
		UUID profileId = UUID.fromString("a95472c0-e0e6-11e2-a28f-0800200c9a77");
		service.updateProfileStatistic(profileId, ProfileStatisticUpdate.INCREMENT_HIT_COUNT);
		ProfileStatisticInfo info = service.getProfileStatistic(profileId);
		assertNotNull(info);
		assertEquals(1433, info.getHitCount());
		assertEquals(42, info.getPictureCount());
		assertEquals(67, info.getCommentCount());
		assertEquals(560, info.getLikeCount());
		assertEquals(10, info.getDislikeCount());
	}
	
	@Test
	public void updateNonExistingStatistic() {
		UUID profileId = UUID.fromString("b95472c0-e0e6-11e2-a28f-0800200c9a72");
		service.updateProfileStatistic(profileId, ProfileStatisticUpdate.INCREMENT_HIT_COUNT);
		ProfileStatisticInfo info = service.getProfileStatistic(profileId);
		assertNotNull(info);
		assertEquals(1, info.getHitCount());
		assertEquals(0, info.getPictureCount());
		assertEquals(0, info.getCommentCount());
		assertEquals(0, info.getLikeCount());
		assertEquals(0, info.getDislikeCount());
	}
	
	@Test
	public void getTopCreators() {
		QueryResult<ProfileStatisticInfo> result = service.getTopCreators(0, 100);
		assertNotNull(result);
		assertEquals(1, result.getPageCount());
		assertEquals(0, result.getPageNumber());
		assertNotNull(result.getContent());
		assertEquals(1, result.getContent().size());
	}
}

