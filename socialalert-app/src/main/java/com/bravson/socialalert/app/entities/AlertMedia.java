package com.bravson.socialalert.app.entities;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.beans.Field;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.springframework.data.annotation.Id;
import org.springframework.data.geo.Point;
import org.springframework.data.solr.core.mapping.SolrDocument;
import org.springframework.format.annotation.DateTimeFormat;

import com.bravson.socialalert.app.domain.PictureMetadata;
import com.bravson.socialalert.app.domain.VideoMetadata;
import com.bravson.socialalert.app.infrastructure.VersionedEntity;
import com.bravson.socialalert.common.domain.MediaCategory;
import com.bravson.socialalert.common.domain.MediaInfo;
import com.bravson.socialalert.common.domain.MediaType;

@SolrDocument(solrCoreName="AlertMedia")
public class AlertMedia extends VersionedEntity {
	
	public static final int GEOHASH_PRECISION = 7;

	@Id
	@Field
    private URI mediaUri;
	
	@Field
	private MediaType type;
	
	@Field
    private String title;
	
	@Field
	private String description;
	
	@Field
	private UUID profileId;

	@Field
	@DateTimeFormat
	private DateTime timestamp;
	
	@Field
	private Integer width;
	
	@Field
	private Integer height;
	
	@Field
	private Long duration;

	@Field
	private Point location;
	
	@Field
	private String locality;
	
	@Field
	private String country;
	
	@Field
	private String cameraMaker;
	
	@Field
	private String cameraModel;
	
	@Field
	private int hitCount;
	
	@Field
	private int likeCount;
	
	@Field
	private int dislikeCount;
	
	@Field
	private int commentCount;

	@Field
	private List<String> categories;
	
	@Field
	private List<String> tags;
	
	@Field
	private String geohash1;
	
	@Field
	private String geohash2;
	
	@Field
	private String geohash3;
	
	@Field
	private String geohash4;
	
	@Field
	private String geohash5;
	
	@Field
	private String geohash6;
	
	@Field
	private String geohash7;
	
	protected AlertMedia() {
		
	}
	
	public AlertMedia(URI mediaUri, UUID profileId, String title, PictureMetadata metadata, Collection<MediaCategory> categories, Collection<String> tags) {
		this.title = title;
		this.type = MediaType.PICTURE;
		this.profileId = profileId;
		this.mediaUri = mediaUri;
		this.timestamp = metadata.getTimestamp();
		this.height = metadata.getHeight();
		this.width = metadata.getWidth();
		this.cameraMaker = metadata.getCameraMaker();
		this.cameraModel = metadata.getCameraModel();
		setCategories(categories);
		setTags(tags);
	}
	
	public AlertMedia(URI mediaUri, UUID profileId, String title, VideoMetadata metadata, Collection<MediaCategory> categories, Collection<String> tags) {
		this.title = title;
		this.type = MediaType.VIDEO;
		this.profileId = profileId;
		this.mediaUri = mediaUri;
		this.timestamp = metadata.getTimestamp();
		this.height = metadata.getHeight();
		this.width = metadata.getWidth();
		this.duration = metadata.getDuration().getMillis();
		this.cameraMaker = metadata.getCameraMaker();
		this.cameraModel = metadata.getCameraModel();
		setCategories(categories);
		setTags(tags);
	}
	
	public URI getMediaUri() {
		return mediaUri;
	}
	
	public MediaType getType() {
		return type;
	}
	
	public UUID getProfileId() {
		return profileId;
	}
	
	private void setCategories(Collection<MediaCategory> collection) {
		if (categories == null) {
			categories = new ArrayList<>();
		} else {
			categories.clear();
		}
		for (MediaCategory category : collection) {
			if (!categories.contains(category)) {
				categories.add(category.name());
			}
		}
	}
	
	private void setTags(Collection<String> collection) {
		if (tags == null) {
			tags = new ArrayList<>();
		} else {
			tags.clear();
		}
		for (String tag : collection) {
			if (!tags.contains(tag)) {
				tags.add(tag);
			}
		}
	}
	
	public Collection<String> getTags() {
		if (tags == null) {
			return Collections.emptyList();
		}
		return Collections.unmodifiableList(tags);
	}

	public void increaseHitCount() {
		hitCount++;
		touch();
	}
	
	public void increaseCommentCount() {
		commentCount++;
		touch();
	}
	
	public MediaInfo toMediaInfo() {
		MediaInfo info = new MediaInfo();
		info.setTitle(title);
		info.setType(type);
		info.setDescription(description);
		info.setProfileId(profileId);
		info.setMediaUri(mediaUri);
		info.setHeight(height);
		info.setWidth(width);
		info.setDuration(duration == null ? null : Duration.millis(duration));
		info.setTimestamp(timestamp);
		info.setCameraMaker(cameraMaker);
		info.setCameraModel(cameraModel);
		info.setLatitude(location == null ? null : location.getX());
		info.setLongitude(location == null ? null : location.getY());
		info.setLocality(locality);
		info.setCountry(country);
		info.setCreation(creation);
		info.setLastUpdate(lastUpdate);
		info.setHitCount(hitCount);
		info.setLikeCount(likeCount);
		info.setDislikeCount(dislikeCount);
		info.setCommentCount(commentCount);
		info.setTags(tags == null ? Collections.<String>emptyList() : Collections.unmodifiableList(tags));
		info.setCategories(categories == null ? Collections.<String>emptyList() : Collections.unmodifiableList(categories));
		return info;
	}

	public boolean isOwnedBy(UUID profileId) {
		return Objects.equals(this.profileId, profileId);
	}

	public void updateLikeDislikeCount(int likeDelta, int dislikeDelta) {
		if (Math.abs(likeDelta) > 1) {
			throw new IllegalArgumentException("Like delta must me either -1, 0 or 1 but was " + likeDelta); 
		}
		if (Math.abs(dislikeDelta) > 1) {
			throw new IllegalArgumentException("Dislike delta must me either -1, 0 or 1 but was " + dislikeDelta); 
		}
		likeCount += likeDelta;
		dislikeCount += dislikeDelta;
		touch();
	}

	public void update(String title, String description, String cameraMaker, String cameraModel, DateTime timestamp, Collection<MediaCategory> categories, Collection<String> tags) {
		if (title != null) {
			this.title = title;
		}
		if (description != null) {
			this.description = description;
		}
		if (cameraMaker != null) {
			this.cameraMaker = cameraMaker;
		}
		if (cameraModel != null) {
			this.cameraModel = cameraModel;
		}
		if (timestamp != null) {
			this.timestamp = timestamp;
		}
		setCategories(categories);
		setTags(tags);
		touch();
	}
	
	public Double getLatitude() {
		return location != null ? location.getX() : null;
	}
	
	public Double getLongitude() {
		return location != null ? location.getY() : null;
	}
	
	public void updateLocation(Double latitude, Double longitude, String geohash, String locality, String country) {
		if (this.locality != null) {
			tags.remove(this.locality);
		}
		if (this.country != null) {
			tags.remove(this.country);
		}
		this.locality = locality;
		this.country = country;
		if (this.locality != null) {
			tags.add(this.locality);
		}
		if (this.country != null) {
			tags.add(this.country);
		}
		
		if (latitude != null && longitude != null) {
			this.location = new Point(latitude, longitude);
		} else {
			this.location = null;
		}
		geohash1 = StringUtils.substring(geohash, 0, 1);
		geohash2 = StringUtils.substring(geohash, 0, 2);
		geohash3 = StringUtils.substring(geohash, 0, 3);
		geohash4 = StringUtils.substring(geohash, 0, 4);
		geohash5 = StringUtils.substring(geohash, 0, 5);
		geohash6 = StringUtils.substring(geohash, 0, 6);
		geohash7 = StringUtils.substring(geohash, 0, 7);
		touch();
	}
}
