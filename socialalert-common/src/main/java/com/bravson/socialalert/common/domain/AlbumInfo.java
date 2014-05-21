package com.bravson.socialalert.common.domain;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import org.joda.time.DateTime;

public class AlbumInfo implements UserContent {

	private UUID albumId;
    private UUID profileId;
    private List<URI> mediaList;
    private String title;
    private String description;
    private DateTime creation;
    private DateTime lastUpdate;
    private String creator;
    private boolean online;
    
	public UUID getAlbumId() {
		return albumId;
	}
	public void setAlbumId(UUID albumId) {
		this.albumId = albumId;
	}
	public UUID getProfileId() {
		return profileId;
	}
	public void setProfileId(UUID profileId) {
		this.profileId = profileId;
	}
	public List<URI> getMediaList() {
		return mediaList;
	}
	public void setMediaList(List<URI> mediaList) {
		this.mediaList = mediaList;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public DateTime getCreation() {
		return creation;
	}
	public void setCreation(DateTime creation) {
		this.creation = creation;
	}
	public DateTime getLastUpdate() {
		return lastUpdate;
	}
	public void setLastUpdate(DateTime lastUpdate) {
		this.lastUpdate = lastUpdate;
	}
	public String getCreator() {
		return creator;
	}
	public void setCreator(String creator) {
		this.creator = creator;
	}
	@Override
	public boolean isOnline() {
		return online;
	}
	
	@Override
	public void setOnline(boolean online) {
		this.online = online;
	}
}
