package com.bravson.socialalert.app.entities;

import java.util.UUID;

import org.apache.solr.client.solrj.beans.Field;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.solr.core.mapping.SolrDocument;
import org.springframework.format.annotation.DateTimeFormat;

@SolrDocument(solrCoreName="QueuedTask")
public class QueuedTask {

	@Id
	@Field
	private UUID uuid;
	
	@Field
	@Version
	private long _version_;
	
	@Field
	@DateTimeFormat
    private DateTime trigger;
	 
	@Field
    @DateTimeFormat
    private DateTime started;
	
	@Field
	private String payload;
	 
	public QueuedTask(DateTime trigger, String payload) {
		this.uuid = UUID.randomUUID();
		this.trigger = trigger;
		this.payload = payload;
	}
	
	public boolean startProcessing() {
		if (started != null) {
			return false;
		}
		started = DateTime.now(DateTimeZone.UTC);
		return true;
	}
	
	public String resubmit() {
		started = null;
		return payload;
	}

	public UUID getId() {
		return uuid;
	}

	public boolean isStarted() {
		return started != null;
	}
}
