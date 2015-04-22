package com.bravson.socialalert.app.services;

import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;
import org.joda.time.DateTime;
import org.springframework.validation.annotation.Validated;

import com.bravson.socialalert.app.domain.PictureMetadata;
import com.bravson.socialalert.app.domain.VideoMetadata;
import com.bravson.socialalert.common.domain.ApprovalModifier;
import com.bravson.socialalert.common.domain.GeoAddress;
import com.bravson.socialalert.common.domain.GeoArea;
import com.bravson.socialalert.common.domain.GeoStatistic;
import com.bravson.socialalert.common.domain.MediaCategory;
import com.bravson.socialalert.common.domain.MediaInfo;
import com.bravson.socialalert.common.domain.MediaType;
import com.bravson.socialalert.common.domain.QueryResult;

@Validated
public interface AlertMediaService {

	MediaInfo createPictureAlert(@NotNull URI mediaUri, @NotNull UUID profileId, @NotEmpty String title, GeoAddress location, @NotNull PictureMetadata metadata, @NotNull Collection<MediaCategory> categories, @NotNull Collection<String> tags);
	
	MediaInfo createVideoAlert(@NotNull URI mediaUri, @NotNull UUID profileId, @NotEmpty String title, GeoAddress location, @NotNull VideoMetadata metadata, @NotNull Collection<MediaCategory> categories, @NotNull Collection<String> tags);
	
	MediaInfo updateAlert(@NotNull URI mediaUri, @NotNull UUID profileId, @NotEmpty String title, String description, GeoAddress location, String cameraMaker, String cameraModel, DateTime mediaTimestamp, @NotNull Collection<MediaCategory> categories, @NotNull Collection<String> tags);
	
	QueryResult<MediaInfo> searchMedia(MediaType type, GeoArea area, String keywords, @Min(0) long maxAge, @Min(0) int pageNumber, @Min(1) int pageSize);
	
	QueryResult<MediaInfo> searchMediaInCategory(MediaType type, GeoArea area, String keywords, @Min(0) long maxAge, @NotEmpty String category, @Min(0) int pageNumber, @Min(1) int pageSize);
	
	QueryResult<MediaInfo> listMediaByProfile(MediaType type, @NotNull UUID profileId, @Min(0) int pageNumber, @Min(1) int pageSize);
	
	MediaInfo viewMediaDetail(@NotNull URI mediaUri);
	
	MediaInfo getMediaInfo(@NotNull URI mediaUri);
	
	void deleteMedia(@NotNull URI mediaUri, @NotNull UUID profileId);

	MediaInfo updateLikeDislike(@NotNull URI mediaUri, ApprovalModifier oldModifier, ApprovalModifier newModifier);
	
	MediaInfo increaseCommentCount(@NotNull URI mediaUri);

	List<String> findKeywordSuggestions(MediaType type, @NotNull String partial);
	
	List<GeoStatistic> mapMediaMatchCount(MediaType type, @NotNull GeoArea area, String keywords, @Min(0) long maxAge, List<UUID> profileIds);
}
