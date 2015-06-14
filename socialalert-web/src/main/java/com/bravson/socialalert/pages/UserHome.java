package com.bravson.socialalert.pages;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.annotations.SetupRender;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.bravson.socialalert.common.domain.MediaInfo;
import com.bravson.socialalert.common.domain.MediaType;
import com.bravson.socialalert.common.domain.ProfileStatisticInfo;
import com.bravson.socialalert.common.domain.PublicProfileInfo;
import com.bravson.socialalert.common.domain.QueryResult;
import com.bravson.socialalert.common.domain.UserInfo;
import com.bravson.socialalert.common.domain.UserRole;
import com.bravson.socialalert.common.facade.MediaFacade;
import com.bravson.socialalert.common.facade.ProfileFacade;
import com.bravson.socialalert.services.ProtectedPage;

@ProtectedPage(disallow={UserRole.ANONYMOUS})
public class UserHome {

	@Inject
    private MediaFacade pictureService;
	
	@Inject
	private ProfileFacade profileService;
	
	@SessionState(create=false)
    private UserInfo userInfo;
	
	@Property
	private ProfileStatisticInfo statisticInfo;
	
	@Property
	private QueryResult<MediaInfo> userPictures;
	
	@Property
	private List<PublicProfileInfo> followedProfiles;
	
	@Property
	private List<PublicProfileInfo> followerProfiles;
	
	@Property
	private PublicProfileInfo currentProfile;
	
	@Property
	@Persist
	private int pageNumber;
	
	@Inject
	private Locale locale;

	private DateTimeFormatter dateTimeFormat = DateTimeFormat.forStyle("MM");
	
	@Property
	@Inject
	@Symbol("app.thumbnail.url")
	private String thumbnailUrl;
	
	@SetupRender
	void setupRender() throws IOException {
		if (userInfo != null && userInfo.getProfileId() != null) {
			userPictures = pictureService.listMediaByProfile(MediaType.PICTURE, userInfo.getProfileId(), pageNumber, 5);
			statisticInfo = profileService.getUserProfile(userInfo.getProfileId());
			followedProfiles = profileService.getFollowedProfiles(0, 10).getContent();
			followerProfiles = profileService.getFollowerProfiles(0, 10).getContent();
			profileService.getNetworkedProfileActivity(userInfo.getProfileId(), 0, 10);
		} else {
			userPictures = null;
			statisticInfo = new ProfileStatisticInfo();
			followedProfiles = Collections.emptyList();
			followerProfiles = Collections.emptyList();
		}
	}
	
	public String getUsername() {
		return userInfo != null ? userInfo.getNickname() : null;
	}
	
	public int getLoginFailureCount() {
		return userInfo != null ? userInfo.getLoginFailureCount() : 0;
	}
	
	public String getLoginFailureDate() {
		if (userInfo == null || userInfo.getLastLoginFailure() == null) {
			return null;
		}
		return dateTimeFormat.withLocale(locale).print(userInfo.getLastLoginFailure());
	}
	
	public String getLoginSuccessDate() {
		if (userInfo == null || userInfo.getLastLoginSuccess() == null) {
			return null;
		}
		return dateTimeFormat.withLocale(locale).print(userInfo.getLastLoginSuccess());
	}
	
	public Object onUpload() {
		return UploadPicture.class;
	}
}
