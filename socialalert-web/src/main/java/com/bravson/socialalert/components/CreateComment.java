package com.bravson.socialalert.components;

import java.io.IOException;
import java.net.URI;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.beaneditor.Validate;
import org.apache.tapestry5.ioc.annotations.Inject;

import com.bravson.socialalert.common.facade.PictureFacade;
import com.bravson.socialalert.pages.PictureDetail;
import com.bravson.socialalert.services.DisplayState;

public class CreateComment {
	
	@SessionState
	private DisplayState displayState;
	
	@Persist
	private URI pictureUri;
	
	@Validate("required")
    @Property
    private String text;
	
	@Inject
    private PictureFacade pictureService;

	public void init(URI pictureUri) {
		this.pictureUri = pictureUri;
	}

	void onValidateFromCommentForm() throws IOException {
		pictureService.addComment(pictureUri, text);
	}
	
    Object onSuccess() {
    	Object nextPage = ObjectUtils.defaultIfNull(displayState.getNextPage(), PictureDetail.class);
    	displayState.clear();
        return nextPage;
    }
    
    Object onFailure() {
    	displayState.showDialog("commentModal");
    	return PictureDetail.class;
    }
}
