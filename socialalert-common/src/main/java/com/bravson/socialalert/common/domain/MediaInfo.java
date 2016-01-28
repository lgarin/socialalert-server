package com.bravson.socialalert.common.domain;

import java.io.Serializable;
import java.net.URI;
import java.util.List;
import java.util.UUID;

import org.joda.time.DateTime;
import org.joda.time.Duration;

public class MediaInfo implements UserContent, Serializable {

	private static final long serialVersionUID = 1L;

	private URI mediaUri;
    
    private MediaType type;

    private String title;
    
    private String description;
	
	private UUID profileId;
	
	private DateTime creation;
	
    private DateTime lastUpdate;
	 
	private DateTime timestamp;
	
	private Integer width;
	
	private Integer height;
	
	private Duration duration;

	private Double longitude;
	
	private Double latitude;
	
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

	public MediaInfo enrich(ApprovalModifier modifier) {
		userApprovalModifier = modifier;
		return this;
	}

	public URI getMediaUri() {
		return mediaUri;
	}

	public void setMediaUri(URI mediaUri) {
		this.mediaUri = mediaUri;
	}

	public MediaType getType() {
		return type;
	}

	public void setType(MediaType type) {
		this.type = type;
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

	public DateTime getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(DateTime timestamp) {
		this.timestamp = timestamp;
	}

	public Integer getWidth() {
		return width;
	}

	public void setWidth(Integer width) {
		this.width = width;
	}

	public Integer getHeight() {
		return height;
	}

	public void setHeight(Integer height) {
		this.height = height;
	}

	public Duration getDuration() {
		return duration;
	}

	public void setDuration(Duration duration) {
		this.duration = duration;
	}

	public Double getLongitude() {
		return longitude;
	}

	public void setLongitude(Double longitude) {
		this.longitude = longitude;
	}

	public Double getLatitude() {
		return latitude;
	}

	public void setLatitude(Double latitude) {
		this.latitude = latitude;
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

	public boolean isOnline() {
		return online;
	}

	public void setOnline(boolean online) {
		this.online = online;
	}

}
