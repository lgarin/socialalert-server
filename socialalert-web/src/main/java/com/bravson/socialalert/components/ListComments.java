package com.bravson.socialalert.components;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.UUID;

import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SetupRender;
import org.apache.tapestry5.ioc.annotations.Inject;

import com.bravson.socialalert.common.domain.AbuseReason;
import com.bravson.socialalert.common.domain.CommentInfo;
import com.bravson.socialalert.common.domain.QueryResult;
import com.bravson.socialalert.common.facade.PictureFacade;
import com.bravson.socialalert.common.facade.ProfileFacade;

public class ListComments {

	@Inject
    private PictureFacade pictureService;
	
	@Persist
	private URI pictureUri;
	
	@Property
	private List<CommentInfo> commentList;
	
	@Property
	private CommentInfo currentComment;
	
	@Inject
    private ProfileFacade profileService;
	
	@Persist
	private int pageNumber;
	
	private int pageCount;
	
	@SetupRender
	void setupRender() throws IOException {
		QueryResult<CommentInfo> result = pictureService.listComments(pictureUri, pageNumber, 5);
		commentList = result.getContent();
		pageCount = result.getPageCount();
		pageNumber = result.getPageNumber();
	}
	
	public void init(URI pictureUri) {
		this.pictureUri = pictureUri;
	}
	
	public boolean isNextPageDisabled() {
		return pageNumber + 1 >= pageCount;
	}
	
	public boolean isPreviousPageDisabled() {
		return pageNumber <= 0;
	}
	
	Object onNextPage() {
		pageNumber++;
		return this;
	}
	
	Object onPreviousPage() {
		pageNumber--;
		return this;
	}

	Object onRepost(UUID commentId) throws IOException {
		pictureService.repostComment(commentId);
		return this;
	}
	
	Object onReportAbuse(UUID commentId) throws IOException {
		profileService.reportAbusiveComment(commentId, "Switzerland", AbuseReason.BAD_LANGUAGE);
		return this;
	}
}
