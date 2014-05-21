package com.bravson.socialalert.app.entities;

import java.util.UUID;

import org.apache.solr.client.solrj.beans.Field;
import org.springframework.data.annotation.Id;
import org.springframework.data.solr.core.geo.GeoLocation;
import org.springframework.data.solr.core.mapping.SolrDocument;

import com.bravson.socialalert.app.infrastructure.VersionedEntity;
import com.bravson.socialalert.common.domain.GeoArea;

@SolrDocument(solrCoreName="SearchHistory")
public class SearchHistory extends VersionedEntity {

	@Id
	@Field
	private UUID searchId;
	
	@Field
	private UUID profileId;
	
	@Field
	private String keywords;
	
	@Field
	private GeoLocation location;
	
	@Field
	private Double radius;
	
	protected SearchHistory() {
	}
	
	public SearchHistory(UUID profileId, String keywords, GeoArea area) {
		this.searchId = UUID.randomUUID();
		this.profileId = profileId;
		this.keywords = keywords;
		if (area != null) {
			this.location = new GeoLocation(area.getLatitude(), area.getLongitude());
			this.radius = area.getRadius();
		}
	}
}
