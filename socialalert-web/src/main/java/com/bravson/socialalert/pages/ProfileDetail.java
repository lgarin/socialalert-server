package com.bravson.socialalert.pages;

import java.io.IOException;
import java.util.UUID;

import org.apache.tapestry5.annotations.PageActivationContext;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SetupRender;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.ioc.annotations.Symbol;

import com.bravson.socialalert.common.domain.PublicProfileInfo;
import com.bravson.socialalert.common.domain.ProfileStatisticInfo;
import com.bravson.socialalert.common.domain.UserRole;
import com.bravson.socialalert.common.facade.ProfileFacade;
import com.bravson.socialalert.services.ProtectedPage;

@ProtectedPage(allow={UserRole.USER})
public class ProfileDetail {

	@Inject
    private ProfileFacade profileService;
	
	@Property
	@Inject
	@Symbol("app.preview.url")
	private String previewUrl;
	
	@PageActivationContext
	private UUID profileId;
	
	@Property
	PublicProfileInfo info;
	
	@Property
	ProfileStatisticInfo statistic;
	
	@Property
	boolean followed;
	
	@SetupRender
	boolean setupRender() throws IOException {
		if (profileId == null) {
			return false;
		}
		info = profileService.getUserProfile(profileId);
		statistic = profileService.getUserProfile(profileId);
		followed = profileService.isFollowingSince(profileId) != null;
		return true;
	}
	
	Object onFollow() throws IOException {
		profileService.follow(profileId);
		return this;
	}
	
	Object onUnfollow() throws IOException {
		profileService.unfollow(profileId);
		return this;
	}
}
