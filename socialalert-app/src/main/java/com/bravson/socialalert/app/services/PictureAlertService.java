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
import com.bravson.socialalert.common.domain.ApprovalModifier;
import com.bravson.socialalert.common.domain.GeoAddress;
import com.bravson.socialalert.common.domain.GeoArea;
import com.bravson.socialalert.common.domain.GeoStatistic;
import com.bravson.socialalert.common.domain.MediaCategory;
import com.bravson.socialalert.common.domain.PictureInfo;
import com.bravson.socialalert.common.domain.QueryResult;

@Deprecated
@Validated
public interface PictureAlertService {

	PictureInfo createAlert(@NotNull URI pictureUri, @NotNull UUID profileId, @NotEmpty String title, GeoAddress location, @NotNull PictureMetadata metadata, @NotNull Collection<MediaCategory> categories, @NotNull Collection<String> tags);
	
	PictureInfo updateAlert(@NotNull URI pictureUri, @NotNull UUID profileId, @NotEmpty String title, String description, GeoAddress location, String cameraMaker, String cameraModel, DateTime pictureTimestamp, @NotNull Collection<MediaCategory> categories, @NotNull Collection<String> tags);
	
	QueryResult<PictureInfo> searchPictures(GeoArea area, String keywords, @Min(0) long maxAge, @Min(0) int pageNumber, @Min(1) int pageSize);
	
	QueryResult<PictureInfo> searchPicturesInCategory(GeoArea area, String keywords, @Min(0) long maxAge, @NotEmpty String category, @Min(0) int pageNumber, @Min(1) int pageSize);
	
	QueryResult<PictureInfo> listPicturesByProfile(@NotNull UUID profileId, @Min(0) int pageNumber, @Min(1) int pageSize);
	
	PictureInfo viewPictureDetail(@NotNull URI pictureUri);
	
	PictureInfo getPictureInfo(@NotNull URI pictureUri);
	
	void deletePicture(@NotNull URI pictureUri, @NotNull UUID profileId);

	PictureInfo updateLikeDislike(@NotNull URI pictureUri, ApprovalModifier oldModifier, ApprovalModifier newModifier);
	
	PictureInfo increaseCommentCount(@NotNull URI pictureUri);

	List<String> findKeywordSuggestions(@NotNull String partial);
	
	List<GeoStatistic> mapPictureMatchCount(@NotNull GeoArea area, String keywords, @Min(0) long maxAge, List<UUID> profileIds);
}
