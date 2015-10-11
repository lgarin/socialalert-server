package com.bravson.socialalert.app.entities;

import java.util.UUID;

import org.apache.solr.client.solrj.beans.Field;
import org.springframework.data.annotation.Id;
import org.springframework.data.solr.core.mapping.SolrDocument;

import com.bravson.socialalert.app.infrastructure.VersionedEntity;

@SolrDocument(solrCoreName="ApplicationEvent")
public class ApplicationEvent extends VersionedEntity {

	@Id
	@Field
	private UUID uuid;
	
	@Field
	private String ipAddress;
	
	@Field
	private UUID profileId;
	
	@Field
	private String country;
	
	@Field
	private String action;
	
	@Field
	private String parameter;
	

	protected ApplicationEvent() {
    	
    }
    
    public ApplicationEvent(UserProfile profile, String ipAddress, String action, String parameter) {
    	this.uuid = UUID.randomUUID();
    	this.ipAddress = ipAddress;
    	this.profileId = profile.getId();
    	this.country = profile.getCountry();
    	this.action = action;
    	this.parameter = parameter;
    }

	public UUID getUuid() {
		return uuid;
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public UUID getProfileId() {
		return profileId;
	}

	public String getCountry() {
		return country;
	}

	public String getAction() {
		return action;
	}

	public String getParameter() {
		return parameter;
	}
}
