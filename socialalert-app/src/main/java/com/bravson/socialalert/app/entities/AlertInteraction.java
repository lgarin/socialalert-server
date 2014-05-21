package com.bravson.socialalert.app.entities;

import java.net.URI;
import java.util.UUID;

import org.apache.solr.client.solrj.beans.Field;
import org.springframework.data.annotation.Id;
import org.springframework.data.solr.core.mapping.SolrDocument;

import com.bravson.socialalert.app.infrastructure.VersionedEntity;
import com.bravson.socialalert.common.domain.ApprovalModifier;

@SolrDocument(solrCoreName="AlertInteraction")
public class AlertInteraction extends VersionedEntity {

	@Id
	@Field
	private String interactionId;
	
	@Field
    private URI mediaUri;
	 
	@Field
    private UUID profileId;
	
	@Field
	private ApprovalModifier approval;
	
	public static String buildInteractionId(URI mediaUri, UUID profileId) {
		return mediaUri.toString() + " @ " + profileId.toString();
	}
	
	public AlertInteraction(URI mediaUri, UUID profileId) {
		this.interactionId = buildInteractionId(mediaUri, profileId);
		this.mediaUri = mediaUri;
		this.profileId = profileId;
	}

	public String getId() {
		return interactionId;
	}

	public URI getMediaUri() {
		return mediaUri;
	}

	public UUID getProfileId() {
		return profileId;
	}
	
	public void setApproval(ApprovalModifier modifier) {
		approval = modifier;
		touch();
	}
	
	public ApprovalModifier getApproval() {
		return approval;
	}
}
