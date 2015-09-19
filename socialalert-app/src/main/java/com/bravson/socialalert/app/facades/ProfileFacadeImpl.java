package com.bravson.socialalert.app.facades;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Resource;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import com.bravson.socialalert.app.domain.ProfileStatisticUpdate;
import com.bravson.socialalert.app.entities.ApplicationUser;
import com.bravson.socialalert.app.entities.UserProfile;
import com.bravson.socialalert.app.services.AbuseReportService;
import com.bravson.socialalert.app.services.AlertActivityService;
import com.bravson.socialalert.app.services.AlertCommentService;
import com.bravson.socialalert.app.services.ApplicationUserService;
import com.bravson.socialalert.app.services.ProfileLinkService;
import com.bravson.socialalert.app.services.ProfileStatisticService;
import com.bravson.socialalert.app.services.UserProfileService;
import com.bravson.socialalert.app.utilities.SecurityUtils;
import com.bravson.socialalert.common.domain.AbuseInfo;
import com.bravson.socialalert.common.domain.AbuseReason;
import com.bravson.socialalert.common.domain.ActivityCount;
import com.bravson.socialalert.common.domain.ActivityInfo;
import com.bravson.socialalert.common.domain.ProfileInfo;
import com.bravson.socialalert.common.domain.ProfileStatisticInfo;
import com.bravson.socialalert.common.domain.PublicProfileInfo;
import com.bravson.socialalert.common.domain.QueryResult;
import com.bravson.socialalert.common.facade.ProfileFacade;

@Service
@Validated
public class ProfileFacadeImpl implements ProfileFacade {

	@Resource
	private UserProfileService profileService;
	
	@Resource
	private ProfileStatisticService statisticService;
	
	@Resource
	private AlertActivityService activityService;
	
	@Resource
	private ApplicationUserService userService;
	
	@Resource
	private AlertCommentService commentService;
	
	@Resource
	private ProfileLinkService linkService;
	
	@Resource
	private AbuseReportService abuseService;
	
	@Value("${query.max.result}")
	private int maxPageSize;
	
	@Value("${profile.max.follow}")
	private int maxFollowCount;

	@Override
	@PreAuthorize("hasRole('USER')")
	public ProfileInfo getCurrentUserProfile() {
		ApplicationUser user = SecurityUtils.findAuthenticatedPrincipal();
		return profileService.getProfileById(user.getProfileId()).toProfileInfo();
	}
	
	@Override
	@PreAuthorize("hasRole('USER')")
	public ProfileInfo updateProfile(ProfileInfo info) {
		ApplicationUser user = SecurityUtils.findAuthenticatedPrincipal();
		info.setNickname(user.getNickname());
		return profileService.updateProfile(user.getProfileId(), info).toProfileInfo();
	}

	@Override
	@PreAuthorize("hasRole('USER')")
	public ProfileInfo claimProfilePicture(URI pictureUri) {
		ApplicationUser user = SecurityUtils.findAuthenticatedPrincipal();
		UserProfile profile = profileService.claimProfilePicture(user.getProfileId(), pictureUri);
		return profile.toProfileInfo();
	}

	@Override
	public QueryResult<ActivityInfo> getProfileActivity(UUID profileId, int pageNumber, int pageSize) {
		QueryResult<ActivityInfo> items = activityService.searchActivityBySourceProfileId(Collections.singletonList(profileId), pageNumber, pageSize);
		userService.populateCreators(items.getContent());
		commentService.populateComments(items.getContent());
		userService.updateOnlineStatus(items.getContent());
		return items;
	}
	
	public QueryResult<ActivityInfo> getNetworkedProfileActivity(UUID profileId, int pageNumber, int pageSize) {
		List<UUID> profileIds = new ArrayList<>(maxFollowCount + 1);
		profileIds.add(profileId);
		profileIds.addAll(linkService.getObservedProfiles(profileId, 0, maxFollowCount).getContent());
		QueryResult<ActivityInfo> items = activityService.searchActivityBySourceProfileId(profileIds, pageNumber, pageSize);
		userService.populateCreators(items.getContent());
		commentService.populateComments(items.getContent());
		userService.updateOnlineStatus(items.getContent());
		return items;
	}
	
	@Override
	public ProfileStatisticInfo getUserProfile(UUID profileId) throws IOException {
		ProfileStatisticInfo info = statisticService.getProfileStatistic(profileId);
		info.enrich(profileService.getProfileById(profileId).toPublicInfo());
		userService.updateOnlineStatus(Collections.singleton(info));
		ApplicationUser currentUser = SecurityUtils.findAuthenticatedPrincipal();
		if (currentUser != null && currentUser.getProfileId() != null) {
			info.setFollowedSince(linkService.getLinkTimestamp(currentUser.getProfileId(), profileId));
		}
		return info;
	}
	
	private QueryResult<PublicProfileInfo> getPublicProfiles(ApplicationUser currentUser, QueryResult<UUID> profileIds) {
		ArrayList<PublicProfileInfo> result = new ArrayList<>(profileIds.getContent().size());
		for (UserProfile profile : profileService.getProfileMap(profileIds.getContent()).values()) {
			result.add(profile.toPublicInfo());
		}
		userService.updateOnlineStatus(result);
		if (currentUser != null && currentUser.getProfileId() != null) {
			linkService.updateObservedStatus(currentUser.getProfileId(), result);
		}
		return new QueryResult<>(result, profileIds.getPageNumber(), profileIds.getPageNumber());
	}
	
	@Override
	@PreAuthorize("hasRole('USER')")
	public QueryResult<PublicProfileInfo> getFollowedProfiles(int pageNumber, int pageSize) {
		ApplicationUser user = SecurityUtils.findAuthenticatedPrincipal();
		QueryResult<UUID> profileIds = linkService.getObservedProfiles(user.getProfileId(), pageNumber, pageSize);
		return getPublicProfiles(user, profileIds);
	}

	@Override
	@PreAuthorize("hasRole('USER')")
	public QueryResult<PublicProfileInfo> getFollowerProfiles(int pageNumber, int pageSize) {
		ApplicationUser user = SecurityUtils.findAuthenticatedPrincipal();
		QueryResult<UUID> profileIds = linkService.getObserverProfiles(user.getProfileId(), pageNumber, pageSize);
		return getPublicProfiles(user, profileIds);
	}
	
	@Override
	@PreAuthorize("hasRole('USER')")
	public DateTime isFollowingSince(UUID profileId) {
		ApplicationUser user = SecurityUtils.findAuthenticatedPrincipal();
		return linkService.getLinkTimestamp(user.getProfileId(), profileId);
	}
	
	@Override
	@PreAuthorize("hasRole('USER')")
	public boolean follow(UUID profileId) {
		ApplicationUser user = SecurityUtils.findAuthenticatedPrincipal();
		profileService.getProfileById(profileId);
		ProfileStatisticInfo statistic = statisticService.getProfileStatistic(user.getProfileId());
		if (statistic.getFollowerCount() >= maxFollowCount) {
			return false;
		}
		boolean result = linkService.addObservedProfile(user.getProfileId(), profileId);
		if (result) {
			statisticService.updateProfileStatistic(profileId, ProfileStatisticUpdate.INCREMENT_FOLLOWER_COUNT);
		}
		return result;
	}
	
	@Override
	@PreAuthorize("hasRole('USER')")
	public boolean unfollow(UUID profileId) {
		ApplicationUser user = SecurityUtils.findAuthenticatedPrincipal();
		boolean result = linkService.removeObservedProfile(user.getProfileId(), profileId);
		if (result) {
			statisticService.updateProfileStatistic(profileId, ProfileStatisticUpdate.DECREMENT_FOLLOWER_COUNT);
		}
		return result;
	}
	
	@Override
	public QueryResult<PublicProfileInfo> searchProfiles(String keyword, int pageNumber, int pageSize) {
		QueryResult<PublicProfileInfo> result = profileService.searchProfiles(keyword, pageNumber, pageSize);
		userService.updateOnlineStatus(result.getContent());
		linkService.updateObservedStatus(SecurityUtils.findAuthenticatedPrincipal().getProfileId(), result.getContent());
		return result;
	}
	
	@Override
	public List<String> findNicknameSuggestions(String partial) {
		return profileService.findNicknameSuggestions(partial);
	}
	
	@Override
	public QueryResult<ProfileStatisticInfo> getTopCreators(int pageNumber, int pageSize) throws IOException {
		QueryResult<ProfileStatisticInfo> result = statisticService.getTopCreators(pageNumber, pageSize);
		ArrayList<UUID> profileIds = new ArrayList<>(result.getContent().size());
		for (ProfileStatisticInfo info : result.getContent()) {
			profileIds.add(info.getProfileId());
		}
		Map<UUID, UserProfile> profileMap = profileService.getProfileMap(profileIds);
		for (ProfileStatisticInfo current : result.getContent()) {
			current.enrich(profileMap.get(current.getProfileId()).toPublicInfo());
		}
		userService.updateOnlineStatus(result.getContent());
		
		ApplicationUser currentUser = SecurityUtils.findAuthenticatedPrincipal();
		if (currentUser != null && currentUser.getProfileId() != null) {
			linkService.updateObservedStatus(currentUser.getProfileId(), result.getContent());
		}
		return result;
	}
	
	@Override
	@PreAuthorize("hasRole('USER')")
	public List<ActivityCount> getRecentActivityStatistic(DateTime lastCheck) {
		ApplicationUser user = SecurityUtils.findAuthenticatedPrincipal();
		QueryResult<UUID> profileIds = linkService.getObservedProfiles(user.getProfileId(), 0, maxPageSize);
		return activityService.getRecentActivityStatistic(profileIds.getContent(), lastCheck);
	}
	
	@Override
	@PreAuthorize("hasRole('USER')")
	public AbuseInfo reportAbusiveComment(UUID commentId, String country, AbuseReason reason) {
		ApplicationUser user = SecurityUtils.findAuthenticatedPrincipal();
		AbuseInfo result = abuseService.reportAbusiveComment(commentId, user.getProfileId(), country, reason);
		userService.populateCreators(Collections.singletonList(result));
		userService.updateOnlineStatus(Collections.singletonList(result));
		return result;
	}
	
	@Override
	@PreAuthorize("hasRole('USER')")
	public AbuseInfo reportAbusiveMedia(URI mediaId, String country, AbuseReason reason) {
		ApplicationUser user = SecurityUtils.findAuthenticatedPrincipal();
		AbuseInfo result = abuseService.reportAbusiveMedia(mediaId, user.getProfileId(), country, reason);
		userService.populateCreators(Collections.singletonList(result));
		userService.updateOnlineStatus(Collections.singletonList(result));
		return result;
	}
}
