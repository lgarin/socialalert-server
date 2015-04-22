package com.bravson.socialalert.app.domain;

import org.springframework.data.solr.core.query.SimpleUpdateField;
import org.springframework.data.solr.core.query.UpdateAction;
import org.springframework.data.solr.core.query.UpdateField;

public enum ProfileStatisticUpdate {

	INCREMENT_HIT_COUNT("hitCount", 1),
	INCREMENT_PICTURE_COUNT("pictureCount", 1),
	INCREMENT_VIDEO_COUNT("videoCount", 1),
	INCREMENT_COMMENT_COUNT("commentCount", 1),
	INCREMENT_LIKE_COUNT("likeCount", 1),
	INCREMENT_DISLIKE_COUNT("dislikeCount", 1),
	INCREMENT_FOLLOWER_COUNT("followerCount", 1),
	DECREMENT_PICTURE_COUNT("pictureCount", -1),
	DECREMENT_LIKE_COUNT("likeCount", -1),
	DECREMENT_DISLIKE_COUNT("dislikeCount", -1),
	DECREMENT_FOLLOWER_COUNT("followerCount", -1);
	
	private final SimpleUpdateField updateField;
	
	private ProfileStatisticUpdate(String fieldName, int delta) {
		updateField = new SimpleUpdateField(fieldName, delta, UpdateAction.INC);
	}
	
	public UpdateField getUpdateField() {
		return updateField;
	}
}
