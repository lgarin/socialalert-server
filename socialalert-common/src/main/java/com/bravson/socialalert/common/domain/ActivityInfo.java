package com.bravson.socialalert.common.domain;

import java.net.URI;
import java.util.UUID;

import org.joda.time.DateTime;

public class ActivityInfo implements UserContent {

	private URI mediaUri;
	
	private UUID profileId;
	
	private ActivityType activityType;
	
	private DateTime timestamp;
	
	private UUID commentId;
	
	private String message;
	
	private String creator;
	
	private boolean online;

	public URI getMediaUri() {
		return mediaUri;
	}

	public void setMediaUri(URI mediaUri) {
		this.mediaUri = mediaUri;
	}

	public UUID getProfileId() {
		return profileId;
	}

	public void setProfileId(UUID profileId) {
		this.profileId = profileId;
	}

	public ActivityType getActivityType() {
		return activityType;
	}

	public void setActivityType(ActivityType activityType) {
		this.activityType = activityType;
	}

	public DateTime getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(DateTime timestamp) {
		this.timestamp = timestamp;
	}
	
	public UUID getCommentId() {
		return commentId;
	}
	
	public void setCommentId(UUID commentId) {
		this.commentId = commentId;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
	
	@Override
	public String getCreator() {
		return creator;
	}
	
	@Override
	public void setCreator(String creator) {
		this.creator = creator;
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
