package com.bravson.socialalert.components;

import java.io.IOException;

import org.apache.tapestry5.annotations.InjectComponent;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.Request;
import org.apache.tapestry5.services.Session;

import com.bravson.socialalert.common.domain.UserInfo;
import com.bravson.socialalert.common.facade.UserFacade;
import com.bravson.socialalert.pages.Albums;
import com.bravson.socialalert.pages.EditProfile;
import com.bravson.socialalert.pages.Index;
import com.bravson.socialalert.pages.RecentPictures;
import com.bravson.socialalert.pages.SearchProfile;
import com.bravson.socialalert.pages.UserFeed;
import com.bravson.socialalert.pages.UserHome;
import com.googlecode.jsonrpc4j.JsonRpcClientException;

public class Navigation {

	@SessionState(create=false)
    private UserInfo userInfo;
	
	@Inject
    private Request request;
	
	@InjectComponent
	private LoginRegister loginRegister;
	
	
	@Inject
    private UserFacade userService;
	
	public String getUsername() {
		return userInfo != null ? userInfo.getNickname() : null;
	}
	
	public Object onActionFromHome() {
		return userInfo != null ? UserHome.class : Index.class;
	}
	
	public Object onActionFromLogout() throws IOException {
		try {
			userService.logout();
		} catch (JsonRpcClientException e) {
		}
		Session session = request.getSession(false);
        if (session != null) {
        	session.invalidate();
        }
		return Index.class;
	}
	
	public Object onActionFromEditProfile() throws IOException {
		return EditProfile.class;
	}
	
	public Object onActionFromPicture() throws IOException {
		return RecentPictures.class;
	}
	
	public Object onActionFromFeed() throws IOException {
		return UserFeed.class;
	}
	
	public Object onActionFromAlbums() {
		return Albums.class;
	}
	
	public Object onActionFromProfiles() {
		return SearchProfile.class;
	}
	
//	public Object onActionFromLogin() {
//		return loginRegister.showLogin();
//	}
}
