package com.bravson.socialalert.components;

import java.io.IOException;
import java.net.URI;
import java.util.List;

import org.apache.tapestry5.BindingConstants;
import org.apache.tapestry5.annotations.InjectPage;
import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.annotations.SetupRender;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.ioc.annotations.Symbol;

import com.bravson.socialalert.common.domain.ApprovalModifier;
import com.bravson.socialalert.common.domain.PictureInfo;
import com.bravson.socialalert.common.domain.QueryResult;
import com.bravson.socialalert.common.facade.PictureFacade;
import com.bravson.socialalert.pages.PictureDetail;
import com.bravson.socialalert.services.DisplayState;

public class PicturePager {

	@Inject
    private PictureFacade pictureService;
	
	@Property
	@Parameter(defaultPrefix=BindingConstants.LITERAL)
	String title;
	
	@Parameter
	QueryResult<PictureInfo> result;
	
	@Parameter
	private int pageNumber;
	
	private int pageCount;
	
	@Property
	private List<PictureInfo> pictureList;
	
	@Property
	private PictureInfo currentPicture;
	
	@Property
	@Inject
	@Symbol("app.thumbnail.url")
	private String thumbnailUrl;
	
	@InjectPage
	private PictureDetail pictureDetail;
	
	@SessionState
	private DisplayState displayState;
	
	@SetupRender
	void setupRender() throws IOException {
		if (result != null) {
			pictureList = result.getContent();
			pageCount = result.getPageCount();
			pageNumber = result.getPageNumber();
		} else {
			pictureList = null;
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
	
	Object onDetail(URI pictureUri) {
		pictureDetail.init(pictureUri);
		return pictureDetail;
	}
	
	Object onDelete(URI pictureUri) throws IOException {
		pictureService.deletePicture(pictureUri);
		return null;
	}
	
	Object onLike(URI pictureUri) throws IOException {
		pictureService.setPictureApproval(pictureUri, ApprovalModifier.LIKE);
		return null;
	}
	
	Object onDislike(URI pictureUri) throws IOException {
		pictureService.setPictureApproval(pictureUri, ApprovalModifier.DISLIKE);
		return null;
	}
	
	Object onComment(URI pictureUri) {
		pictureDetail.init(pictureUri);
		displayState.showDialog("commentModal");
		return pictureDetail;
	}
}
