package com.bravson.socialalert.app.entities;

import java.util.UUID;

import org.apache.solr.client.solrj.beans.Field;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.solr.core.mapping.SolrDocument;

import com.bravson.socialalert.common.domain.ProfileStatisticInfo;

@SolrDocument(solrCoreName="ProfileStatistic")
public class ProfileStatistic {

	@Id
	@Field
	private UUID profileId;
	
	@Field
	@Version
	private long _version_;
	
	@Field
	private int pictureCount;
	
	@Field
	private int videoCount;
	
	@Field
	private int commentCount;
	
	@Field
	private int hitCount;
	
	@Field
	private int likeCount;
	
	@Field
	private int dislikeCount;
	
	@Field
	private int followerCount;
	
	public ProfileStatisticInfo toStatisticInfo() {
		ProfileStatisticInfo info = new ProfileStatisticInfo();
		info.setProfileId(profileId);
		info.setPictureCount(pictureCount);
		info.setVideoCount(videoCount);
		info.setCommentCount(commentCount);
		info.setLikeCount(likeCount);
		info.setDislikeCount(dislikeCount);
		info.setHitCount(hitCount);
		info.setFollowerCount(followerCount);
		return info;
	}
}
