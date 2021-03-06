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
	
	private int approvalCount;

	private ApprovalModifier userApprovalModifier;
	
	private String creator;
	
	private boolean online;
	
	public CommentInfo enrich(ApprovalModifier modifier) {
		userApprovalModifier = modifier;
		return this;
	}
	
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
	
	public int getApprovalCount() {
		return approvalCount;
	}

	public void setApprovalCount(int approvalCount) {
		this.approvalCount = approvalCount;
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
