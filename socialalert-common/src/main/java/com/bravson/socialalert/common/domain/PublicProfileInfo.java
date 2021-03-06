package com.bravson.socialalert.common.domain;

import java.net.URI;
import java.util.UUID;

import org.joda.time.DateTime;

public class PublicProfileInfo implements UserContent {

	private UUID profileId;
	private URI image;
	private String nickname;
	private String biography;
	private boolean online;
	private DateTime followedSince;
	
	public UUID getProfileId() {
		return profileId;
	}
	public void setProfileId(UUID profileId) {
		this.profileId = profileId;
	}
	public String getNickname() {
		return nickname;
	}
	public void setNickname(String nickname) {
		this.nickname = nickname;
	}
	public URI getImage() {
		return image;
	}
	public void setImage(URI image) {
		this.image = image;
	}
	public String getBiography() {
		return biography;
	}
	public void setBiography(String biography) {
		this.biography = biography;
	}
	public boolean isOnline() {
		return online;
	}
	public void setOnline(boolean online) {
		this.online = online;
	}
	public String getCreator() {
		return nickname;
	}
	public void setCreator(String creator) {
		this.nickname = creator;
	}
	public boolean isFollowed() {
		return followedSince != null;
	}
	public DateTime getFollowedSince() {
		return followedSince;
	}
	public void setFollowedSince(DateTime followedSince) {
		this.followedSince = followedSince;
	}
}
