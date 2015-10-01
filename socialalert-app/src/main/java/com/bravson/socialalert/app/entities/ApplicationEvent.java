package com.bravson.socialalert.app.entities;

import java.util.UUID;

import org.apache.solr.client.solrj.beans.Field;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.solr.core.mapping.SolrDocument;
import org.springframework.format.annotation.DateTimeFormat;

@SolrDocument(solrCoreName="ApplicationEvent")
public class ApplicationEvent {

	@Id
	@Field
	private UUID uuid;
	
	@Field
	@DateTimeFormat
	@CreatedDate
	private DateTime timestamp;
	
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
	
	@Field
	@Version
	private long _version_;
	
	protected ApplicationEvent() {
    	
    }
    
    public ApplicationEvent(UserProfile profile, String ipAddress, String action, String parameter) {
    	this.timestamp = DateTime.now(DateTimeZone.UTC);
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

	public DateTime getTimestamp() {
		return timestamp;
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
