package com.bravson.socialalert.common.domain;

import java.net.URI;
import java.util.UUID;

import org.joda.time.DateTime;

public class CommentInfo implements UserContent {

	private UUID commentId;
	
	private URI mediaUri;

	private UUID profileId;
	
	private DateTime creation;
	
	private String comment;
	
	private String creator;
	
	private boolean online;
	
	public UUID getCommentId() {
		return commentId;
	}
	
	public void setCommentId(UUID commentId) {
		this.commentId = commentId;
	}

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

	public DateTime getCreation() {
		return creation;
	}

	public void setCreation(DateTime creation) {
		this.creation = creation;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public String getCreator() {
		return creator;
	}

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
