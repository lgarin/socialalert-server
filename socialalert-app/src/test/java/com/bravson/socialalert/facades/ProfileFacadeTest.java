package com.bravson.socialalert.facades;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import javax.annotation.Resource;
import javax.validation.ValidationException;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.context.SecurityContextHolder;

import com.bravson.socialalert.app.entities.AlertActivity;
import com.bravson.socialalert.app.entities.ApplicationUser;
import com.bravson.socialalert.app.entities.ProfileLink;
import com.bravson.socialalert.app.entities.ProfileStatistic;
import com.bravson.socialalert.app.entities.UserProfile;
import com.bravson.socialalert.common.domain.ActivityCount;
import com.bravson.socialalert.common.domain.ActivityInfo;
import com.bravson.socialalert.common.domain.ActivityType;
import com.bravson.socialalert.common.domain.ProfileInfo;
import com.bravson.socialalert.common.domain.ProfileStatisticInfo;
import com.bravson.socialalert.common.domain.PublicProfileInfo;
import com.bravson.socialalert.common.domain.QueryResult;
import com.bravson.socialalert.common.facade.ProfileFacade;
import com.bravson.socialalert.common.facade.UserFacade;
import com.bravson.socialalert.infrastructure.DataServiceTest;

public class ProfileFacadeTest extends DataServiceTest {

	@Resource
	private ProfileFacade profileFacade;
	
	@Resource
	private UserFacade userFacade;
	
	@Before
	public void setUp() throws Exception {
		fullImport(ApplicationUser.class);
		fullImport(UserProfile.class);
		fullImport(ProfileStatistic.class);
		fullImport(AlertActivity.class);
		fullImport(ProfileLink.class);
		SecurityContextHolder.clearContext();
	}

	@Test(expected=AuthenticationCredentialsNotFoundException.class)
	public void getCurrentProfileWithoutLogin() throws IOException {
		profileFacade.getCurrentUserProfile();
	}
	
	@Test
	public void getCurrentProfileWithActiveUser() throws IOException {
		userFacade.login("lucien@test.com", "123");
		ProfileInfo info = profileFacade.getCurrentUserProfile();
		assertNotNull(info);
		assertEquals("Garin", info.getLastname());
		assertEquals("Lucien", info.getFirstname());
		assertNull(info.getBirthdate());
		assertNull(info.getImage());
	}
	
	@Test(expected=AccessDeniedException.class)
	public void getCurrentProfileWithGuestUser() throws IOException {
		userFacade.login("unverified@test.com", "123");
		profileFacade.getCurrentUserProfile();
	}
	
	@Test(expected=AuthenticationCredentialsNotFoundException.class)
	public void updateProfileWithoutLogin() throws IOException {
		ProfileInfo info = new ProfileInfo();
		profileFacade.updateProfile(info);
	}
	
	@Test
	public void updateProfileWithActiveUser() throws IOException {
		userFacade.login("lucien@test.com", "123");
		ProfileInfo info1 = new ProfileInfo();
		info1.setFirstname("Mister");
		info1.setLastname("Mike");
		ProfileInfo info2 = profileFacade.updateProfile(info1);
		assertNotNull(info2);
		assertEquals("Mister", info2.getFirstname());
		assertEquals("Mike", info2.getLastname());
		assertNull(info2.getBirthdate());
		assertNull(info2.getImage());
	}
	
	@Test(expected=ValidationException.class)
	public void updateProfileWithInvalidInput() throws IOException {
		userFacade.login("lucien@test.com", "123");
		ProfileInfo info1 = new ProfileInfo();
		info1.setFirstname("This is a very very long first name exceeding our upper limit");
		profileFacade.updateProfile(info1);
	}
	
	@Test(expected=AccessDeniedException.class)
	public void updateProfileWithGuestUser() throws IOException {
		userFacade.login("unverified@test.com", "123");
		ProfileInfo info = new ProfileInfo();
		profileFacade.updateProfile(info);
	}

	@Test
	public void readExistingStatistic() throws IOException {
		userFacade.login("unverified@test.com", "123");
		UUID profileId = UUID.fromString("a95472c0-e0e6-11e2-a28f-0800200c9a77");
		ProfileStatisticInfo info = profileFacade.getUserProfile(profileId);
		assertNotNull(info);
		assertEquals(1432, info.getHitCount());
		assertEquals(42, info.getPictureCount());
		assertEquals(67, info.getCommentCount());
		assertEquals(560, info.getLikeCount());
		assertEquals(10, info.getDislikeCount());
	}
	
	@Test
	public void getRecentProfileActivity() throws IOException {
		userFacade.login("unverified@test.com", "123");
		UUID profileId = UUID.fromString("a95472c0-e0e6-11e2-a28f-0800200c9a77");
		List<ActivityInfo> result = profileFacade.getRecentProfileActivity(profileId, 10);
		assertNotNull(result);
		assertEquals(3, result.size());
	}
	
	@Test
	public void getProfileActivity() throws IOException {
		userFacade.login("unverified@test.com", "123");
		UUID profileId = UUID.fromString("a95472c0-e0e6-11e2-a28f-0800200c9a77");
		QueryResult<ActivityInfo> result = profileFacade.getProfileActivity(profileId, 0, 10);
		assertNotNull(result);
		assertEquals(0, result.getPageNumber());
		assertEquals(1, result.getPageCount());
		List<ActivityInfo> list = result.getContent();
		assertNotNull(list);
		assertEquals(3, list.size());
	}
	
	 // TODO add tests for claimProfilePicture
	
	@Test
	public void getFollowedProfiles() throws IOException {
		userFacade.login("lucien@test.com", "123");
		QueryResult<PublicProfileInfo> result = profileFacade.getFollowedProfiles(0, 10);
		assertNotNull(result);
		assertNotNull(result.getContent());
		assertEquals(1, result.getContent().size());
		PublicProfileInfo profile = result.getContent().get(0);
		assertEquals("test", profile.getNickname());
	}
	
	 @Test
	 public void isFollowing() throws IOException {
		userFacade.login("lucien@test.com", "123");
		assertTrue(profileFacade.isFollowing(UUID.fromString("e7d166ae-9b3f-4405-be0d-fa1567728593")));
	 }
	 
	 @Test
	 public void unfollow() throws IOException {
		 userFacade.login("lucien@test.com", "123");
		 assertTrue(profileFacade.unfollow(UUID.fromString("e7d166ae-9b3f-4405-be0d-fa1567728593")));
		 assertFalse(profileFacade.isFollowing(UUID.fromString("e7d166ae-9b3f-4405-be0d-fa1567728593")));
	 }
	 
	 @Test
	 public void follow() throws IOException {
		 userFacade.login("lucien@test.com", "123");
		 assertFalse(profileFacade.follow(UUID.fromString("e7d166ae-9b3f-4405-be0d-fa1567728593")));
	 }
	 
	 @Test
	public void searchProfileWithNickname() throws IOException {
		 userFacade.login("lucien@test.com", "123");
		QueryResult<PublicProfileInfo> result = profileFacade.searchProfiles("sg33", 0, 10);
		assertNotNull(result);
		assertEquals(1, result.getPageCount());
		assertEquals(0, result.getPageNumber());
		assertNotNull(result.getContent());
		assertEquals(1, result.getContent().size());
		PublicProfileInfo profile = result.getContent().get(0);
		assertNotNull(profile);
		assertEquals("sg33g5", profile.getNickname());
	}
	
	@Test
	public void findNicknameSuggestion() throws IOException {
		userFacade.login("lucien@test.com", "123");
		List<String> result = profileFacade.findNicknameSuggestions("sg33");
		assertEquals(Arrays.asList("sg33g5"), result);
	}
	
	@Test
	public void getTopCreators() throws IOException {
		userFacade.login("lucien@test.com", "123");
		QueryResult<ProfileStatisticInfo> result = profileFacade.getTopCreators(0, 100);
		assertNotNull(result);
		assertEquals(1, result.getPageCount());
		assertEquals(0, result.getPageNumber());
		assertNotNull(result.getContent());
		assertEquals(1, result.getContent().size());
	}
	

	@Test
	public void getActivityStatistic() throws IOException {
		userFacade.login("lucien@test.com", "123");
		List<ActivityCount> statistic = profileFacade.getRecentActivityStatistic(new DateTime(2000, 1, 1, 0, 0));
		assertNotNull(statistic);
		assertEquals(1, statistic.size());
		ActivityCount activity = statistic.get(0);
		assertEquals(1, activity.getCount());
		assertEquals(ActivityType.LIKE_MEDIA, activity.getType());
	}
}
