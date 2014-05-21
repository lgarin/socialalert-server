package com.bravson.socialalert.components;

import java.io.IOException;
import java.util.List;

import org.apache.tapestry5.BindingConstants;
import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SetupRender;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.ioc.annotations.Symbol;

import com.bravson.socialalert.common.domain.PublicProfileInfo;
import com.bravson.socialalert.common.domain.QueryResult;
import com.bravson.socialalert.common.facade.ProfileFacade;

public class ProfilePager {

	@Inject
    private ProfileFacade profileService;
	
	@Property
	@Parameter(defaultPrefix=BindingConstants.LITERAL)
	String title;
	
	@Parameter
	QueryResult<PublicProfileInfo> result;
	
	@Parameter
	private int pageNumber;
	
	private int pageCount;
	
	@Property
	private List<PublicProfileInfo> profileList;
	
	@Property
	private PublicProfileInfo currentProfile;
	
	@Property
	@Inject
	@Symbol("app.thumbnail.url")
	private String thumbnailUrl;

	@SetupRender
	void setupRender() throws IOException {
		if (result != null) {
			profileList = result.getContent();
			pageCount = result.getPageCount();
			pageNumber = result.getPageNumber();
		} else {
			profileList = null;
			pageCount = 0;
			pageNumber = 0;
		}
	}
	
	public boolean isNextPageDisabled() {
		return pageNumber + 1 >= pageCount;
	}
	
	public boolean isPreviousPageDisabled() {
		return pageNumber <= 0;
	}
	
	Object onNextPage() {
		pageNumber++;
		return null;
	}
	
	Object onPreviousPage() {
		pageNumber--;
		return null;
	}
}
