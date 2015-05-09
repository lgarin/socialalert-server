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
import org.springframework.data.annotation.Id;
import org.springframework.data.geo.Point;
import org.springframework.data.solr.core.mapping.SolrDocument;
import org.springframework.format.annotation.DateTimeFormat;

import com.bravson.socialalert.app.domain.PictureMetadata;
import com.bravson.socialalert.app.infrastructure.VersionedEntity;
import com.bravson.socialalert.common.domain.MediaCategory;
import com.bravson.socialalert.common.domain.PictureInfo;

@SolrDocument(solrCoreName="PictureAlert")
@Deprecated
public class PictureAlert extends VersionedEntity {
	
	public static final int GEOHASH_PRECISION = 7;

	@Id
	@Field
    private URI pictureUri;
	
	@Field
    private String title;
	
	@Field
	private String description;
	
	@Field
	private UUID profileId;

	@Field
	@DateTimeFormat
	private DateTime pictureTimestamp;
	
	@Field
	private Integer pictureWidth;
	
	@Field
	private Integer pictureHeight;

	@Field
	private Point pictureLocation;
	
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
	
	protected PictureAlert() {
		
	}
	
	public PictureAlert(URI pictureUri, UUID profileId, String title, PictureMetadata metadata, Collection<MediaCategory> categories, Collection<String> tags) {
		this.title = title;
		this.profileId = profileId;
		this.pictureUri = pictureUri;
		this.pictureTimestamp = metadata.getTimestamp();
		this.pictureHeight = metadata.getHeight();
		this.pictureWidth = metadata.getWidth();
		this.cameraMaker = metadata.getCameraMaker();
		this.cameraModel = metadata.getCameraModel();
		setCategories(categories);
		setTags(tags);
	}
	
	public URI getPictureUri() {
		return pictureUri;
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
	
	public PictureInfo toPictureInfo() {
		PictureInfo info = new PictureInfo();
		info.setTitle(title);
		info.setDescription(description);
		info.setProfileId(profileId);
		info.setPictureUri(pictureUri);
		info.setPictureHeight(pictureHeight);
		info.setPictureWidth(pictureWidth);
		info.setPictureTimestamp(pictureTimestamp);
		info.setCameraMaker(cameraMaker);
		info.setCameraModel(cameraModel);
		info.setPictureLatitude(pictureLocation == null ? null : pictureLocation.getX());
		info.setPictureLongitude(pictureLocation == null ? null : pictureLocation.getY());
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

	public void update(String title, String description, String cameraMaker, String cameraModel, DateTime pictureTimestamp, Collection<MediaCategory> categories, Collection<String> tags) {
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
		if (pictureTimestamp != null) {
			this.pictureTimestamp = pictureTimestamp;
		}
		setCategories(categories);
		setTags(tags);
		touch();
	}
	
	public Double getLatitude() {
		return pictureLocation != null ? pictureLocation.getX() : null;
	}
	
	public Double getLongitude() {
		return pictureLocation != null ? pictureLocation.getY() : null;
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
			this.pictureLocation = new Point(latitude, longitude);
		} else {
			this.pictureLocation = null;
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

	public AlertMedia toMedia() {
		PictureMetadata metadata = new PictureMetadata();
		metadata.setCameraMaker(cameraMaker);
		metadata.setCameraModel(cameraModel);
		metadata.setHeight(pictureHeight);
		metadata.setWidth(pictureWidth);
		metadata.setDefaultTimestamp(pictureTimestamp);
		ArrayList<MediaCategory> mediaCategories = new ArrayList<MediaCategory>();
		if (categories != null) {
			for (String category : categories) {
				mediaCategories.add(MediaCategory.valueOf(category));
			}
		}
		AlertMedia media = new AlertMedia(pictureUri, profileId, title, metadata, mediaCategories, tags == null ? Collections.<String>emptyList() : tags);
		for (int i = 0; i < likeCount; i++) {
			media.updateLikeDislikeCount(1, 0);
		}
		for (int i = 0; i < dislikeCount; i++) {
			media.updateLikeDislikeCount(0, 1);
		}
		for (int i = 0; i < hitCount; i++) {
			media.increaseHitCount();
		}
		for (int i = 0; i < commentCount; i++) {
			media.increaseCommentCount();
		}
		if (pictureLocation != null) {
			media.updateLocation(pictureLocation.getX(), pictureLocation.getY(), geohash7, locality, country);
		}
		return media;
	}
}
