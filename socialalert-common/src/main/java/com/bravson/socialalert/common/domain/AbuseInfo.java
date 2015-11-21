package com.bravson.socialalert.common.domain;

import java.net.URI;
import java.util.UUID;

import org.joda.time.DateTime;

public class AbuseInfo implements UserContent {

	private URI mediaUri;
	
	private UUID profileId;
	
	private AbuseReason reason;
	
	private AbuseStatus status;
	
	private String country;
	
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
	
	public AbuseReason getReason() {
		return reason;
	}

	public void setReason(AbuseReason reason) {
		this.reason = reason;
	}

	public AbuseStatus getStatus() {
		return status;
	}

	public void setStatus(AbuseStatus status) {
		this.status = status;
	}
	
	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
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
