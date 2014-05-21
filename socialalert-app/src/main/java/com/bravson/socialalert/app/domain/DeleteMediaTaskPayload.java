package com.bravson.socialalert.app.domain;

import java.net.URI;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import com.bravson.socialalert.app.services.DeleteMediaTask;

@XmlRootElement(name="deleteMedia")
public class DeleteMediaTaskPayload extends BaseTaskPayload<DeleteMediaTask> {

	private static final long serialVersionUID = 1L;
	
	@XmlElement
	public URI mediaUri;
	
	public DeleteMediaTaskPayload() {
	}
	
	public DeleteMediaTaskPayload(URI mediaUri, long deleteDelay) {
		// TODO specify delay using config
		super(DateTime.now(DateTimeZone.UTC).withDurationAdded(deleteDelay, 1));
		this.mediaUri = mediaUri;
	}
	
	@Override
	public String toString() {
		return new ToStringBuilder(this).append("mediaUri", mediaUri).build();
	}
}
