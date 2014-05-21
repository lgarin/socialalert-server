package com.bravson.socialalert.pages;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Locale;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.ClientProtocolException;
import org.apache.tapestry5.annotations.CleanupRender;
import org.apache.tapestry5.annotations.InjectComponent;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.annotations.SetupRender;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.ioc.services.TypeCoercer;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.bravson.socialalert.common.domain.GeoAddress;
import com.bravson.socialalert.common.domain.MediaCategory;
import com.bravson.socialalert.common.domain.PictureInfo;
import com.bravson.socialalert.common.domain.UserRole;
import com.bravson.socialalert.common.facade.PictureFacade;
import com.bravson.socialalert.components.CreateComment;
import com.bravson.socialalert.components.ListComments;
import com.bravson.socialalert.services.DisplayState;
import com.bravson.socialalert.services.ProtectedPage;

@ProtectedPage(disallow={UserRole.ANONYMOUS})
public class PictureDetail {

	@SessionState
	private DisplayState displayState;
	
	@Persist
	private URI pictureUri;
	
	private PictureInfo info;
	
	@InjectComponent
	private CreateComment createComment;
	
	@InjectComponent
	private ListComments listComments;
	
	@Inject
	@Symbol("app.picture.url")
	private String basePictureUrl;
	
	@Inject
	@Symbol("app.preview.url")
	private String basePreviewUrl;
	
	@Inject
	private Locale locale;
	
	@Inject
	private TypeCoercer typeCoercer;

	private DateTimeFormatter dateTimeFormat = DateTimeFormat.forStyle("MM");
	
	@Inject
    private PictureFacade pictureService;
	
	private String newDescription;
	
	public void init(URI pictureUri) {
		this.pictureUri = pictureUri;
		createComment.init(pictureUri);
		listComments.init(pictureUri);
	}
	
	Object onComment(URI pictureUri) {
		createComment.init(pictureUri);
		displayState.showDialog("commentModal");
		return this;
	}
	
	Object onRepost(URI pictureUri) throws IOException {
		pictureService.repostPicture(pictureUri);
		return this;
	}
	
	@SetupRender
	void setupRender() throws IOException {
		if (pictureUri != null) {
			info = pictureService.viewPictureDetail(pictureUri);
		}
	}
	
	@CleanupRender
	void cleanupRender() {
		displayState.showDialog(null);
	}

	public String getPreviewUrl() {
		return basePreviewUrl + "/" + getPictureUri();
	}
	
	public String getPictureUrl() {
		return basePictureUrl + "/" + getPictureUri();
	}
	
	public String getTitle() {
		return info == null ? "" : info.getTitle();
	}
	
	public String getDescription() {
		return info == null ? "" : info.getDescription();
	}
	
	public void setDescription(String description) {
		newDescription = description;
	}
	
	public String getCreator() {
		return info == null ? "" : info.getCreator();
	}
	
	public int getHitCount() {
		return info == null ? 0 : info.getHitCount();
	}
	
	public int getLikeCount() {
		return info == null ? 0 : info.getLikeCount();
	}
	
	public int getDislikeCount() {
		return info == null ? 0 : info.getDislikeCount();
	}
	
	public int getCommentCount() {
		return info == null ? 0 : info.getCommentCount();
	}
	
	public String getFormattedCamera() {
		if (info == null || info.getCameraMaker() == null || info.getCameraModel() == null) {
			return "";
		}
		return info.getCameraMaker() + " / " + info.getCameraModel();
	}

	public String getFormattedPosition() {
		if (info == null || info.getPictureLatitude() == null || info.getPictureLongitude() == null) {
			return "";
		}
		return info.getPictureLatitude() + " - " + info.getPictureLongitude();
	}
	
	public String getPictureUri() {
		return info == null ? "" : info.getPictureUri().toString();
	}
	
	public String getFormattedCategories() {
		if (info == null || info.getCategories().isEmpty()) {
			return "";
		}
		ArrayList<String> categoryList = new ArrayList<>(info.getCategories().size());
		for (String category : info.getCategories()) {
			categoryList.add(typeCoercer.coerce(category, String.class));
		}
		return StringUtils.join(categoryList, ", ");
	}
	
	public String getFormattedTags() {
		return  info == null ? "" : StringUtils.join(info.getTags(), ",");
	}
	
	public String getFormattedDate() {
		return  info == null ? "" : dateTimeFormat.withLocale(locale).print(info.getPictureTimestamp());
	}
	
	public Double getLongitude() {
		return info == null ? null : info.getPictureLongitude();
	}
	
	public Double getLatitude() {
		return info == null ? null : info.getPictureLatitude();
	}
	
	public UUID getProfileId() {
		return info == null ? null : info.getProfileId();
	}
	
	public Object onSuccess() throws ClientProtocolException, IOException
    {
		info = pictureService.viewPictureDetail(pictureUri);
		GeoAddress address = new GeoAddress(7.5, 46.9, "Terrassenrain 6, 3072 Ostermundigen, Suisse", "Ostermundigen", "Switzerland");
		ArrayList<MediaCategory> categoryList = new ArrayList<>(info.getCategories().size());
		for (String category : info.getCategories()) {
			categoryList.add(typeCoercer.coerce(category, MediaCategory.class));
		}
		pictureService.updatePictureInfo(pictureUri, info.getTitle(), newDescription, address, null, null, null, categoryList, info.getTags());
		return this;
    }
}
