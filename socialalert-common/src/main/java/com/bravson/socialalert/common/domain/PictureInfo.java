package com.bravson.socialalert.common.domain;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import org.joda.time.DateTime;

@Deprecated
public class PictureInfo implements UserContent {

    private URI pictureUri;

    private String title;
    
    private String description;
	
	private UUID profileId;
	
	private DateTime creation;
	
    private DateTime lastUpdate;
	 
	private DateTime pictureTimestamp;
	
	private Integer pictureWidth;
	
	private Integer pictureHeight;

	private Double pictureLongitude;
	
	private Double pictureLatitude;
	
	private String locality;
	
	private String country;
	
	private String cameraMaker;
	
	private String cameraModel;
	
	private int hitCount;
	
	private int likeCount;
	
	private int dislikeCount;
	
	private int commentCount;
	
	private List<String> categories;
	
	private List<String> tags;
	
	private ApprovalModifier userApprovalModifier;
	
	private String creator;
	
	private boolean online;

	public URI getPictureUri() {
		return pictureUri;
	}

	public void setPictureUri(URI pictureUri) {
		this.pictureUri = pictureUri;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}

	public UUID getProfileId() {
		return profileId;
	}

	public void setProfileId(UUID profileId) {
		this.profileId = profileId;
	}

	public DateTime getCreation() {
		return creation;
	}

	public void setCreation(DateTime creation) {
		this.creation = creation;
	}

	public DateTime getLastUpdate() {
		return lastUpdate;
	}

	public void setLastUpdate(DateTime lastUpdate) {
		this.lastUpdate = lastUpdate;
	}

	public DateTime getPictureTimestamp() {
		return pictureTimestamp;
	}

	public void setPictureTimestamp(DateTime pictureTimestamp) {
		this.pictureTimestamp = pictureTimestamp;
	}

	public Integer getPictureWidth() {
		return pictureWidth;
	}

	public void setPictureWidth(Integer pictureWidth) {
		this.pictureWidth = pictureWidth;
	}

	public Integer getPictureHeight() {
		return pictureHeight;
	}

	public void setPictureHeight(Integer pictureHeight) {
		this.pictureHeight = pictureHeight;
	}

	public Double getPictureLongitude() {
		return pictureLongitude;
	}

	public void setPictureLongitude(Double pictureLongitude) {
		this.pictureLongitude = pictureLongitude;
	}

	public Double getPictureLatitude() {
		return pictureLatitude;
	}

	public void setPictureLatitude(Double pictureLatitude) {
		this.pictureLatitude = pictureLatitude;
	}
	
	public String getLocality() {
		return locality;
	}

	public void setLocality(String locality) {
		this.locality = locality;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getCameraMaker() {
		return cameraMaker;
	}

	public void setCameraMaker(String cameraMaker) {
		this.cameraMaker = cameraMaker;
	}

	public String getCameraModel() {
		return cameraModel;
	}

	public void setCameraModel(String cameraModel) {
		this.cameraModel = cameraModel;
	}

	public int getHitCount() {
		return hitCount;
	}

	public void setHitCount(int hitCount) {
		this.hitCount = hitCount;
	}

	public int getLikeCount() {
		return likeCount;
	}

	public void setLikeCount(int likeCount) {
		this.likeCount = likeCount;
	}

	public int getDislikeCount() {
		return dislikeCount;
	}

	public void setDislikeCount(int dislikeCount) {
		this.dislikeCount = dislikeCount;
	}
	
	public int getCommentCount() {
		return commentCount;
	}
	
	public void setCommentCount(int commentCount) {
		this.commentCount = commentCount;
	}
	
	public List<String> getCategories() {
		return categories;
	}

	public void setCategories(List<String> categories) {
		this.categories = categories;
	}

	public List<String> getTags() {
		return tags;
	}

	public void setTags(List<String> tags) {
		this.tags = tags;
	}
	
	public ApprovalModifier getUserApprovalModifier() {
		return userApprovalModifier;
	}

	public void setUserApprovalModifier(ApprovalModifier userApprovalModifier) {
		this.userApprovalModifier = userApprovalModifier;
	}
	
	public String getCreator() {
		return creator;
	}

	public void setCreator(String creator) {
		this.creator = creator;
	}

	public PictureInfo enrich(ApprovalModifier modifier) {
		userApprovalModifier = modifier;
		return this;
	}
	
	@Override
	public boolean isOnline() {
		return online;
	}
	
	@Override
	public void setOnline(boolean online) {
		this.online = online;
	}
}
