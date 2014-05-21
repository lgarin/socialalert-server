package com.bravson.socialalert.common.domain;


public class ProfileStatisticInfo extends PublicProfileInfo {
	
	private int pictureCount;
	
	private int commentCount;
	
	private int hitCount;
	
	private int likeCount;
	
	private int dislikeCount;
	
	private int followerCount;

	public int getPictureCount() {
		return pictureCount;
	}

	public void setPictureCount(int pictureCount) {
		this.pictureCount = pictureCount;
	}

	public int getCommentCount() {
		return commentCount;
	}

	public void setCommentCount(int commentCount) {
		this.commentCount = commentCount;
	}

	public int getHitCount() {
		return hitCount;
	}

	public void setHitCount(int hitCount) {
		this.hitCount = hitCount;
	}

	public int getLikeCount() {
		return likeCount;
	}

	public void setLikeCount(int likeCount) {
		this.likeCount = likeCount;
	}

	public int getDislikeCount() {
		return dislikeCount;
	}

	public void setDislikeCount(int dislikeCount) {
		this.dislikeCount = dislikeCount;
	}

	public int getFollowerCount() {
		return followerCount;
	}

	public void setFollowerCount(int followerCount) {
		this.followerCount = followerCount;
	}
	
	public void enrich(PublicProfileInfo profile) {
		setNickname(profile.getNickname());
		setImage(profile.getImage());
		setBiography(profile.getBiography());
	}
}
