package com.bravson.socialalert.app.infrastructure;

import org.apache.solr.client.solrj.beans.Field;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.format.annotation.DateTimeFormat;

public abstract class VersionedEntity {

	@Field
	@Version
	private long _version_;
	
	@Field
	@DateTimeFormat
	@CreatedDate
	protected DateTime creation;
	
	@Field
	@DateTimeFormat
	@LastModifiedDate
	protected DateTime lastUpdate;
	
	public VersionedEntity() {
		this.creation = DateTime.now(DateTimeZone.UTC);
		this.lastUpdate = creation;
	}
	
	protected void touch() {
		lastUpdate = DateTime.now(DateTimeZone.UTC);
	}

	public DateTime getCreation() {
		return creation;
	}
}
