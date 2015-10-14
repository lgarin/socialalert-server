package com.bravson.socialalert.app.entities;

import java.net.URI;
import java.util.UUID;

import org.apache.solr.client.solrj.beans.Field;
import org.springframework.data.annotation.Id;
import org.springframework.data.solr.core.mapping.SolrDocument;

import com.bravson.socialalert.app.infrastructure.VersionedEntity;
import com.bravson.socialalert.common.domain.ActivityInfo;
import com.bravson.socialalert.common.domain.ActivityType;

@SolrDocument(solrCoreName="AlertActivity")
public class AlertActivity extends VersionedEntity {

	@Id
	@Field
	private UUID activityId;
	
	@Field
    private URI mediaUri;
	
	@Field
	private UUID sourceId;
	 
	@Field
    private UUID profileId;
	
	@Field
	private ActivityType activityType;
	
	@Field
	private UUID commentId;
	
	@Field
	private String ipAddress;
	
	@Field
	private String country;
	
	public AlertActivity(URI mediaUri, UUID profileId, UUID sourceId, ActivityType activityType, UUID commentId, String country, String ipAddress) {
		this.activityId = UUID.randomUUID();
		this.mediaUri = mediaUri;
		this.sourceId = sourceId;
		this.profileId = profileId;
		this.activityType = activityType;
		this.commentId = commentId;
		this.ipAddress = ipAddress;
		this.country = country;
	}

	public UUID getId() {
		return activityId;
	}

	public URI getMediaUri() {
		return mediaUri;
	}
	
	public UUID getSourceId() {
		return sourceId;
	}

	public UUID getProfileId() {
		return profileId;
	}

	public UUID getCommentId() {
		return commentId;
	}

	public ActivityInfo toActivityInfo() {
		ActivityInfo info = new ActivityInfo();
		info.setActivityType(activityType);
		info.setTimestamp(creation);
		info.setMediaUri(mediaUri);
		info.setProfileId(sourceId);
		info.setCommentId(commentId);
		info.setCountry(country);
		return info;
	}
}
