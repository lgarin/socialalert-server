package com.bravson.socialalert.app.domain;

import org.joda.time.DateTime;
import org.joda.time.Duration;

public class VideoMetadata {

	private Integer width;
	private Integer height;
	private DateTime timestamp;
	private Duration duration;
	private Double longitude;
	private Double latitude;
	private String cameraMaker;
	private String cameraModel;
	
	public void setWidth(Integer width) {
		this.width = width;
	}

	public void setHeight(Integer height) {
		this.height = height;
	}

	public void setTimestamp(DateTime timestamp) {
		this.timestamp = timestamp;
	}
	
	public void setDuration(Duration duration) {
		this.duration = duration;
	}

	public void setLongitude(Double longitude) {
		this.longitude = longitude;
	}

	public void setLatitude(Double latitude) {
		this.latitude = latitude;
	}

	public void setCameraMaker(String cameraMaker) {
		this.cameraMaker = cameraMaker;
	}

	public void setCameraModel(String cameraModel) {
		this.cameraModel = cameraModel;
	}

	public Integer getWidth() {
		return width;
	}

	public Integer getHeight() {
		return height;
	}

	public DateTime getTimestamp() {
		return timestamp;
	}
	
	public Duration getDuration() {
		return duration;
	}
	
	public Double getLongitude() {
		return longitude;
	}
	public Double getLatitude() {
		return latitude;
	}
	public String getCameraMaker() {
		return cameraMaker;
	}
	public String getCameraModel() {
		return cameraModel;
	}

	public void setDefaultTimestamp(DateTime defaultTimestamp) {
		if (timestamp == null) {
			timestamp = defaultTimestamp;
		}
	}

	public void setDefaultLatitude(Double defaultLatitude) {
		if (latitude == null) {
			latitude = defaultLatitude;
		}
	}

	public void setDefaultLongitude(Double defaultLongitude) {
		if (longitude == null) {
			longitude = defaultLongitude;
		}
	}
	
	public boolean hasLocation() {
		return longitude != null && latitude != null;
	}
}
