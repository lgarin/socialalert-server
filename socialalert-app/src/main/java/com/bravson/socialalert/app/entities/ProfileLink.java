package com.bravson.socialalert.app.entities;

import java.util.UUID;

import org.apache.solr.client.solrj.beans.Field;
import org.springframework.data.annotation.Id;
import org.springframework.data.solr.core.mapping.SolrDocument;

import com.bravson.socialalert.app.infrastructure.VersionedEntity;

@SolrDocument(solrCoreName="ProfileLink")
public class ProfileLink extends VersionedEntity {

	@Id
	@Field
	private String linkId;
	
	@Field
    private UUID sourceProfileId;
	 
	@Field
    private UUID targetProfileId;
	
	@Field
	private int weight;
	
	public static String buildLinkId(UUID sourceProfileId, UUID targetProfileId) {
		return sourceProfileId.toString() + "->" + targetProfileId.toString();
	}
	
	public ProfileLink(UUID sourceProfileId, UUID targetProfileId) {
		this.linkId = buildLinkId(sourceProfileId, targetProfileId);
		this.sourceProfileId = sourceProfileId;
		this.targetProfileId = targetProfileId;
	}

	public String getId() {
		return linkId;
	}

	public UUID getSourceProfileId() {
		return sourceProfileId;
	}

	public UUID getTargetProfileId() {
		return targetProfileId;
	}
	
	public void increaseWeight() {
		weight++;
		touch();
	}
}
