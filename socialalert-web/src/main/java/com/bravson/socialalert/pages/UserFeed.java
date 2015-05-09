package com.bravson.socialalert.pages;

import java.io.IOException;
import java.net.URI;
import java.util.List;

import org.apache.tapestry5.annotations.InjectPage;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.annotations.SetupRender;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.bravson.socialalert.common.domain.ActivityInfo;
import com.bravson.socialalert.common.domain.ActivityType;
import com.bravson.socialalert.common.domain.UserInfo;
import com.bravson.socialalert.common.domain.UserRole;
import com.bravson.socialalert.common.facade.ProfileFacade;
import com.bravson.socialalert.services.ProtectedPage;

@ProtectedPage(disallow={UserRole.ANONYMOUS})
public class UserFeed {

	@Property
	List<ActivityInfo> activityList;
	
	@Property
	ActivityInfo currentActivity;
	
	@Inject
	ProfileFacade profileFacade;
	
	@Property
	@Inject
	@Symbol("app.thumbnail.url")
	private String thumbnailUrl;
	
	@SessionState(create=false)
    private UserInfo userInfo;
	
	@InjectPage
	private PictureDetail pictureDetail;
	
	private DateTimeFormatter timestampFormat = DateTimeFormat.mediumTime();
	
	@SetupRender
	void setupRender() throws IOException {
		activityList = profileFacade.getProfileActivity(userInfo.getProfileId(), 0, 20).getContent();
	}
	
	String generateMessage(ActivityType activityType, DateTime timestamp) {
		switch (activityType) {
		case LIKE_MEDIA: return "Liked this picture at " + timestampFormat.print(timestamp);
		case UNLIKE_MEDIA: return "Did not like this picture at " + timestampFormat.print(timestamp);
		case NEW_PICTURE: return "Posted this picture at " + timestampFormat.print(timestamp);
		case NEW_COMMENT: return "Commented this picture at " + timestampFormat.print(timestamp);
		case REPOST_PICTURE: return "Reposted this picture at " + timestampFormat.print(timestamp);
		case REPOST_COMMENT: return "Reposted this comment at " + timestampFormat.print(timestamp);
		default: return null;
		}
	}
	
	public String getActivityTitle() {
		return generateMessage(currentActivity.getActivityType(), currentActivity.getTimestamp());
	}

	Object onDetail(URI pictureUri) {
		pictureDetail.init(pictureUri);
		return pictureDetail;
	}
}
