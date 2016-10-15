package com.bravson.socialalert.common.domain;


public interface MediaConstants {

	int MAX_TITLE_LENGTH = 200;
	int MAX_DESCRIPTION_LENGTH = 1000;
	int MAX_TAG_COUNT = 10;
	int LIKE_WEIGHT = 2;
	int DISLIKE_WEIGHT = -LIKE_WEIGHT;
	
	String MOV_MEDIA_TYPE = "video/quicktime";
	String MP4_MEDIA_TYPE = "video/mp4";
	String JPG_MEDIA_TYPE = "image/jpeg";
	
	String JPG_EXTENSION = "jpg";
	String MP4_EXTENSION = "mp4";
	String MOV_EXTENSION = "mov";
}
