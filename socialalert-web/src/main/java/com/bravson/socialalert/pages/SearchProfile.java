package com.bravson.socialalert.pages;

import java.io.IOException;
import java.util.List;

import org.apache.tapestry5.annotations.InjectComponent;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SetupRender;
import org.apache.tapestry5.corelib.components.Form;
import org.apache.tapestry5.ioc.annotations.Inject;

import com.bravson.socialalert.common.domain.PublicProfileInfo;
import com.bravson.socialalert.common.domain.QueryResult;
import com.bravson.socialalert.common.domain.UserRole;
import com.bravson.socialalert.common.facade.ProfileFacade;
import com.bravson.socialalert.services.ProtectedPage;

@ProtectedPage(disallow={UserRole.ANONYMOUS})
public class SearchProfile {
	@Inject
	private ProfileFacade profileFacade;
	
	@Property
	@Persist
	private int pageNumber;
	
	@Property
	QueryResult<PublicProfileInfo> searchResult;
	
	@Persist
	@Property
	private String nickname;
	
	@InjectComponent("search")
    private Form searchForm;
	
	@SetupRender
	void setupRender() throws IOException {
		if (nickname != null) {
			searchResult = profileFacade.searchProfiles(nickname, pageNumber, 5);
		}
	}
	
	Object onClear() {
		pageNumber = 0;
		nickname = null;
		return this;
	}
	
	void onSuccessFromSearch() throws IOException {
		pageNumber = 0;
	}
	
	List<String> onProvideCompletionsFromNickname(String partial) throws IOException {
		return profileFacade.findNicknameSuggestions(partial);
	}
}
