package com.bravson.socialalert.services;

import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

import javax.annotation.Resource;

import org.junit.Before;
import org.junit.Test;
import org.springframework.transaction.annotation.Transactional;

import com.bravson.socialalert.app.entities.ProfileLink;
import com.bravson.socialalert.app.services.ProfileLinkService;
import com.bravson.socialalert.common.domain.PublicProfileInfo;
import com.bravson.socialalert.common.domain.QueryResult;
import com.bravson.socialalert.infrastructure.DataServiceTest;

public class ProfileLinkServiceTest extends DataServiceTest {

	@Resource
	private ProfileLinkService service;
	
	@Before
	public void setUp() throws Exception {
		fullImport(ProfileLink.class);
	}
	
	@Test
	public void isObserver() {
		UUID sourceProfileId = UUID.fromString("a95472c0-e0e6-11e2-a28f-0800200c9a77");
		UUID targetProfileId = UUID.fromString("e7d166ae-9b3f-4405-be0d-fa1567728593");
		assertTrue(service.isObserverOf(sourceProfileId, targetProfileId));
	}
	
	@Test
	public void isNotObserver() {
		UUID sourceProfileId = UUID.fromString("a95472c0-e0e6-11e2-a28f-0800200c9a77");
		UUID targetProfileId = UUID.fromString("a7d166ae-9b3f-4405-be0d-fa156772859a");
		assertFalse(service.isObserverOf(sourceProfileId, targetProfileId));
	}
	
	@Test
	public void getObservedProfiles() {
		UUID sourceProfileId = UUID.fromString("a95472c0-e0e6-11e2-a28f-0800200c9a77");
		UUID targetProfileId = UUID.fromString("e7d166ae-9b3f-4405-be0d-fa1567728593");
		QueryResult<UUID> profileIds = service.getObservedProfiles(sourceProfileId, 0, 10);
		assertEquals(Arrays.asList(targetProfileId), profileIds.getContent());
	}
	
	@Test
	public void getOppositeObservedProfiles() {
		UUID sourceProfileId = UUID.fromString("e7d166ae-9b3f-4405-be0d-fa1567728593");
		QueryResult<UUID> profileIds = service.getObservedProfiles(sourceProfileId, 0, 10);
		assertNotNull(profileIds);
		assertEquals(Collections.emptyList(), profileIds.getContent());
	}
	
	@Test
	public void getObserverProfiles() {
		UUID sourceProfileId = UUID.fromString("a95472c0-e0e6-11e2-a28f-0800200c9a77");
		UUID targetProfileId = UUID.fromString("e7d166ae-9b3f-4405-be0d-fa1567728593");
		QueryResult<UUID> profileIds = service.getObserverProfiles(targetProfileId, 0, 10);
		assertEquals(Arrays.asList(sourceProfileId), profileIds.getContent());
	}
	
	@Test
	public void getOppositeObserverProfiles() {
		UUID targetProfileId = UUID.fromString("a95472c0-e0e6-11e2-a28f-0800200c9a77");
		QueryResult<UUID> profileIds = service.getObserverProfiles(targetProfileId, 0, 10);
		assertNotNull(profileIds);
		assertEquals(Collections.emptyList(), profileIds.getContent());
	}
	
	@Test
	public void addNewObservedProfile() {
		UUID sourceProfileId = UUID.fromString("e7d166ae-9b3f-4405-be0d-fa1567728593");
		UUID targetProfileId = UUID.fromString("a95472c0-e0e6-11e2-a28f-0800200c9a77");
		assertTrue(service.addObservedProfile(sourceProfileId, targetProfileId));
	}
	
	@Test
	public void addExistingObservedProfile() {
		UUID sourceProfileId = UUID.fromString("a95472c0-e0e6-11e2-a28f-0800200c9a77");
		UUID targetProfileId = UUID.fromString("e7d166ae-9b3f-4405-be0d-fa1567728593");
		assertFalse(service.addObservedProfile(sourceProfileId, targetProfileId));
	}
	
	@Test
	public void removeExistingObservedProfile() {
		UUID sourceProfileId = UUID.fromString("a95472c0-e0e6-11e2-a28f-0800200c9a77");
		UUID targetProfileId = UUID.fromString("e7d166ae-9b3f-4405-be0d-fa1567728593");
		assertTrue(service.removeObservedProfile(sourceProfileId, targetProfileId));
	}
	
	@Test
	public void removeNonExistingObservedProfile() {
		UUID sourceProfileId = UUID.fromString("e7d166ae-9b3f-4405-be0d-fa1567728593");
		UUID targetProfileId = UUID.fromString("a95472c0-e0e6-11e2-a28f-0800200c9a77");
		assertFalse(service.removeObservedProfile(sourceProfileId, targetProfileId));
	}
	
	@Test
	@Transactional
	public void increaseActivity() {
		UUID sourceProfileId = UUID.fromString("a95472c0-e0e6-11e2-a28f-0800200c9a77");
		UUID targetProfileId = UUID.fromString("e7d166ae-9b3f-4405-be0d-fa1567728593");
		assertTrue(service.increaseActivityWeight(sourceProfileId, targetProfileId, 1));
	}
	
	@Test
	@Transactional
	public void noActivity() {
		UUID sourceProfileId = UUID.fromString("a95472c0-e0e6-11e2-a28f-0800200c9a77");
		UUID targetProfileId = UUID.fromString("a7d166ae-9b3f-4405-be0d-fa156772859a");
		assertFalse(service.increaseActivityWeight(sourceProfileId, targetProfileId, 1));
	}
	
	@Test
	public void updateObservedStatus() {
		UUID sourceProfileId = UUID.fromString("a95472c0-e0e6-11e2-a28f-0800200c9a77");
		UUID targetProfileId = UUID.fromString("e7d166ae-9b3f-4405-be0d-fa1567728593");
		PublicProfileInfo info = new PublicProfileInfo();
		info.setProfileId(targetProfileId);
		service.updateObservedStatus(sourceProfileId, Collections.singleton(info));
		assertTrue(info.isFollowed());
	}
	
	@Test
	public void updateNotObservedStatus() {
		UUID sourceProfileId = UUID.fromString("a95472c0-e0e6-11e2-a28f-0800200c9a77");
		UUID targetProfileId = UUID.fromString("a7d166ae-9b3f-4405-be0d-fa156772859a");
		PublicProfileInfo info = new PublicProfileInfo();
		info.setProfileId(targetProfileId);
		service.updateObservedStatus(sourceProfileId, Collections.singleton(info));
		assertFalse(info.isFollowed());
	}
}
