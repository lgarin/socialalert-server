package com.bravson.socialalert.components;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.UUID;

import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SetupRender;
import org.apache.tapestry5.ioc.annotations.Inject;

import com.bravson.socialalert.common.domain.CommentInfo;
import com.bravson.socialalert.common.domain.QueryResult;
import com.bravson.socialalert.common.facade.PictureFacade;

public class ListComments {

	@Inject
    private PictureFacade pictureService;
	
	@Persist
	private URI pictureUri;
	
	@Property
	private List<CommentInfo> commentList;
	
	@Property
	private CommentInfo currentComment;
	
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

	Object onRepost(UUID commetnId) throws IOException {
		pictureService.repostComment(commetnId);
		return this;
	}
}
