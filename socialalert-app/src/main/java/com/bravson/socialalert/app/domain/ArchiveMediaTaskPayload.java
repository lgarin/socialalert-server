package com.bravson.socialalert.app.domain;

import java.net.URI;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.builder.ToStringBuilder;

import com.bravson.socialalert.app.services.ArchiveMediaTask;

@XmlRootElement(name="archiveMedia")
public class ArchiveMediaTaskPayload extends BaseTaskPayload<ArchiveMediaTask> {

	private static final long serialVersionUID = 1L;
	
	@XmlElement
	public URI tempUri;
	
	@XmlElement
	public URI finalUri;
	
	public ArchiveMediaTaskPayload() {
	}
	
	public ArchiveMediaTaskPayload(URI tempUri, URI finalUri) {
		this.tempUri = tempUri;
		this.finalUri = finalUri;
	}
	
	@Override
	public String toString() {
		return new ToStringBuilder(this).append("tempUri", tempUri).append("finalUri", finalUri).build();
	}
}
