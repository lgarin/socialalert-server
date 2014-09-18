package com.bravson.socialalert.app.entities;

import java.net.URI;
import java.util.UUID;

import org.apache.solr.client.solrj.beans.Field;
import org.springframework.data.annotation.Id;
import org.springframework.data.solr.core.mapping.SolrDocument;

import com.bravson.socialalert.app.infrastructure.VersionedEntity;
import com.bravson.socialalert.common.domain.AbuseInfo;
import com.bravson.socialalert.common.domain.AbuseReason;
import com.bravson.socialalert.common.domain.AbuseStatus;

@SolrDocument(solrCoreName="AbuseReport")
public class AbuseReport extends VersionedEntity {

	@Id
	@Field
	private UUID abuseId;
	
	@Field
    private URI mediaUri;
	
	@Field
	private UUID commentId;
	
	@Field
	private UUID sourceId;
	 
	@Field
    private UUID profileId;
	
	@Field
	private AbuseReason reason;
	
	@Field
	private AbuseStatus status;
	
	@Field
	private String country;
	
	public AbuseReport(URI mediaUri, UUID profileId, UUID sourceId, String country, AbuseReason reason) {
		this.abuseId = UUID.randomUUID();
		this.mediaUri = mediaUri;
		this.sourceId = sourceId;
		this.profileId = profileId;
		this.reason = reason;
		this.country = country;
		this.status = AbuseStatus.NEW;
	}
	
	public AbuseReport(UUID commentId, UUID profileId, UUID sourceId, String country, AbuseReason reason) {
		this.abuseId = UUID.randomUUID();
		this.commentId = commentId;
		this.sourceId = sourceId;
		this.profileId = profileId;
		this.reason = reason;
		this.country = country;
		this.status = AbuseStatus.NEW;
	}

	public UUID getId() {
		return abuseId;
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

	public AbuseInfo toAbuseInfo() {
		AbuseInfo info = new AbuseInfo();
		info.setReason(reason);
		info.setTimestamp(creation);
		info.setMediaUri(mediaUri);
		info.setProfileId(sourceId);
		info.setCommentId(commentId);
		info.setStatus(status);
		info.setReason(reason);
		info.setCountry(country);
		return info;
	}
}
