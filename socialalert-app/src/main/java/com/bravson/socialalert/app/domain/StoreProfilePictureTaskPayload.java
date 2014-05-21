package com.bravson.socialalert.app.domain;

import java.net.URL;
import java.util.UUID;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import com.bravson.socialalert.app.services.StoreProfilePictureTask;

@XmlRootElement(name="storeProfilePicture")
public class StoreProfilePictureTaskPayload extends BaseTaskPayload<StoreProfilePictureTask> {

private static final long serialVersionUID = 1L;
	
	@XmlElement
	public UUID profileId;
	
	@XmlElement
	public URL image;
	
	@XmlElement
	public int retryCount;
	
	public StoreProfilePictureTaskPayload() {
	}
	
	private StoreProfilePictureTaskPayload(UUID profileId, URL image, int retryCount, long retryInterval) {
		super(DateTime.now(DateTimeZone.UTC).plus(retryInterval));
		this.profileId = profileId;
		this.image = image;
		this.retryCount = retryCount;
	}
	
	public StoreProfilePictureTaskPayload(UUID profileId, URL image, int retryCount) {
		this(profileId, image, retryCount, 0L);
	}
	
	@Override
	public String toString() {
		return new ToStringBuilder(this).append("profileId", profileId).append("image", image).build();
	}

	public StoreProfilePictureTaskPayload createRetry(long retryInterval) {
		if (retryCount == 0) {
			return null;
		}
		return new StoreProfilePictureTaskPayload(profileId, image, retryCount - 1, retryInterval);
	}
}
