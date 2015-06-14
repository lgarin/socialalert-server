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

import com.bravson.socialalert.common.domain.AbuseReason;
import com.bravson.socialalert.common.domain.GeoAddress;
import com.bravson.socialalert.common.domain.MediaCategory;
import com.bravson.socialalert.common.domain.MediaInfo;
import com.bravson.socialalert.common.domain.UserRole;
import com.bravson.socialalert.common.facade.MediaFacade;
import com.bravson.socialalert.common.facade.ProfileFacade;
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
	
	private MediaInfo info;
	
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
    private MediaFacade pictureService;
	
	@Inject
    private ProfileFacade profileService;
	
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
		pictureService.repostMedia(pictureUri);
		return this;
	}
	
	Object onReportAbuse(URI pictureUri) throws IOException {
		profileService.reportAbusiveMedia(pictureUri, "Switzerland", AbuseReason.DISCRIMINATION);
		return this;
	}
	
	@SetupRender
	void setupRender() throws IOException {
		if (pictureUri != null) {
			info = pictureService.viewMediaDetail(pictureUri);
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
		if (info == null || info.getLatitude() == null || info.getLongitude() == null) {
			return "";
		}
		return info.getLatitude() + " - " + info.getLongitude();
	}
	
	public String getPictureUri() {
		return info == null ? "" : info.getMediaUri().toString();
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
		return  info == null ? "" : dateTimeFormat.withLocale(locale).print(info.getTimestamp());
	}
	
	public Double getLongitude() {
		return info == null ? null : info.getLongitude();
	}
	
	public Double getLatitude() {
		return info == null ? null : info.getLatitude();
	}
	
	public UUID getProfileId() {
		return info == null ? null : info.getProfileId();
	}
	
	public Object onSuccess() throws ClientProtocolException, IOException
    {
		info = pictureService.viewMediaDetail(pictureUri);
		GeoAddress address = new GeoAddress(7.5, 46.9, "Terrassenrain 6, 3072 Ostermundigen, Suisse", "Ostermundigen", "Switzerland");
		ArrayList<MediaCategory> categoryList = new ArrayList<>(info.getCategories().size());
		for (String category : info.getCategories()) {
			categoryList.add(typeCoercer.coerce(category, MediaCategory.class));
		}
		pictureService.updateMediaInfo(pictureUri, info.getTitle(), newDescription, address, null, null, null, categoryList, info.getTags());
		return this;
    }
}
