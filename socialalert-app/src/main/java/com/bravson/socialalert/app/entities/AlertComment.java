package com.bravson.socialalert.app.entities;

import java.net.URI;
import java.util.UUID;

import org.apache.solr.client.solrj.beans.Field;
import org.springframework.data.annotation.Id;
import org.springframework.data.solr.core.mapping.SolrDocument;

import com.bravson.socialalert.app.infrastructure.VersionedEntity;
import com.bravson.socialalert.common.domain.CommentInfo;

@SolrDocument(solrCoreName="AlertComment")
public class AlertComment extends VersionedEntity {

	@Id
	@Field
	private UUID commentId;
	
	@Field
    private URI mediaUri;
	 
	@Field
    private UUID profileId;
	
	@Field
	private String comment;
	
	@Field
	private int approvalCount;
	
	public AlertComment(URI mediaUri, UUID profileId, String comment) {
		this.commentId = UUID.randomUUID();
		this.mediaUri = mediaUri;
		this.profileId = profileId;
		this.comment = comment;
	}

	public UUID getId() {
		return commentId;
	}

	public URI getMediaUri() {
		return mediaUri;
	}

	public UUID getProfileId() {
		return profileId;
	}

	public String getComment() {
		return comment;
	}
	
	public void updateApprovalCount(int approvalDelta) {
		if (Math.abs(approvalDelta) > 2) {
			throw new IllegalArgumentException("Approval delta must me either -2, -1, 0, 1 or 2 but was " + approvalDelta); 
		}
		approvalCount += approvalDelta;
		touch();
	}
	
	public CommentInfo toCommentInfo() {
		CommentInfo info = new CommentInfo();
		info.setCommentId(commentId);
		info.setComment(comment);
		info.setCreation(creation);
		info.setMediaUri(mediaUri);
		info.setProfileId(profileId);
		info.setApprovalCount(approvalCount);
		return info;
	}
}
