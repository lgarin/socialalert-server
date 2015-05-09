package com.bravson.socialalert.common.facade;

import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.NotEmpty;
import org.joda.time.DateTime;

import com.bravson.socialalert.common.domain.ActivityInfo;
import com.bravson.socialalert.common.domain.AlbumConstants;
import com.bravson.socialalert.common.domain.AlbumInfo;
import com.bravson.socialalert.common.domain.ApprovalModifier;
import com.bravson.socialalert.common.domain.CommentInfo;
import com.bravson.socialalert.common.domain.GeoAddress;
import com.bravson.socialalert.common.domain.GeoStatistic;
import com.bravson.socialalert.common.domain.MediaCategory;
import com.bravson.socialalert.common.domain.MediaConstants;
import com.bravson.socialalert.common.domain.PictureInfo;
import com.bravson.socialalert.common.domain.QueryResult;
import com.bravson.socialalert.common.domain.TagInfo;
import com.googlecode.jsonrpc4j.JsonRpcParam;
import com.googlecode.jsonrpc4j.JsonRpcService;

@JsonRpcService(value="pictureFacade", useNamedParams=true)
@Deprecated
public interface PictureFacade {

	PictureInfo claimPicture(@JsonRpcParam("pictureUri") @NotNull URI pictureUri, @JsonRpcParam("title") @NotEmpty @Size(max=MediaConstants.MAX_TITLE_LENGTH) String title, @JsonRpcParam("location") GeoAddress location, @JsonRpcParam("categories") @NotNull Collection<MediaCategory> categories, @JsonRpcParam("tags") @NotNull @Size(max=MediaConstants.MAX_TAG_COUNT) Collection<String> tags) throws IOException;
	
	PictureInfo updatePictureInfo(@JsonRpcParam("pictureUri") @NotNull URI pictureUri,  @JsonRpcParam("title") @NotEmpty @Size(max=MediaConstants.MAX_TITLE_LENGTH) String title, @JsonRpcParam("description") @Size(max=MediaConstants.MAX_DESCRIPTION_LENGTH) String description, @JsonRpcParam("location") GeoAddress location, @JsonRpcParam("cameraMaker") String cameraMaker, @JsonRpcParam("cameraModel") String cameraModel, @JsonRpcParam("pictureTimestamp") DateTime pictureTimestamp, @JsonRpcParam("categories") @NotNull Collection<MediaCategory> categories, @JsonRpcParam("tags") @NotNull @Size(max=MediaConstants.MAX_TAG_COUNT) Collection<String> tags) throws IOException;
	
	QueryResult<PictureInfo> searchPictures(@JsonRpcParam("latitude") Double latitude, @JsonRpcParam("longitude") Double longitude, @JsonRpcParam("maxDistance") Double maxDistance, @JsonRpcParam("keywords") String keywords, @JsonRpcParam("maxAge") @Min(0) long maxAge, @JsonRpcParam("pageNumber") @Min(0) int pageNumber, @JsonRpcParam("pageSize") @Min(1) int pageSize) throws IOException;
	
	QueryResult<PictureInfo> searchPicturesInCategory(@JsonRpcParam("latitude") Double latitude, @JsonRpcParam("longitude") Double longitude, @JsonRpcParam("maxDistance") Double maxDistance, @JsonRpcParam("keywords") String keywords, @JsonRpcParam("maxAge") @Min(0) long maxAge, @JsonRpcParam("category") @NotEmpty String category, @JsonRpcParam("pageNumber") @Min(0) int pageNumber, @JsonRpcParam("pageSize") @Min(1) int pageSize) throws IOException;
	
	Map<String, List<PictureInfo>> searchTopPicturesByCategories(@JsonRpcParam("latitude") Double latitude, @JsonRpcParam("longitude") Double longitude, @JsonRpcParam("maxDistance") Double maxDistance, @JsonRpcParam("keywords") String keywords, @JsonRpcParam("maxAge") @Min(0) long maxAge, @JsonRpcParam("categories") @NotEmpty Collection<String> categories, @JsonRpcParam("groupSize") @Min(1) int groupSize);
	
	QueryResult<PictureInfo> listPicturesByProfile(@JsonRpcParam("profileId") @NotNull UUID profileId, @JsonRpcParam("pageNumber") @Min(0) int pageNumber, @JsonRpcParam("pageSize") @Min(1) int pageSize) throws IOException;
	
	PictureInfo viewPictureDetail(@JsonRpcParam("pictureUri") @NotNull URI pictureUri) throws IOException;
	
	void deletePicture(@JsonRpcParam("pictureUri") @NotNull URI pictureUri) throws IOException;
	
	PictureInfo setPictureApproval(@JsonRpcParam("pictureUri") @NotNull URI pictureUri, @JsonRpcParam("modifier") ApprovalModifier modifier) throws IOException;
	
	List<GeoAddress> findLocation(@JsonRpcParam("address") @NotEmpty String address, @JsonRpcParam("region") String region, @JsonRpcParam("preferredLanguage") String preferredLanguage) throws IOException;

	CommentInfo addComment(@JsonRpcParam("pictureUri") @NotNull URI pictureUri, @JsonRpcParam("comment") @NotEmpty String comment) throws IOException;
	
	ActivityInfo repostComment(@JsonRpcParam("commentId") @NotNull UUID commentId) throws IOException;
	
	ActivityInfo repostPicture(@JsonRpcParam("pictureId") @NotNull URI pictureUri) throws IOException;

	QueryResult<CommentInfo> listComments(@JsonRpcParam("pictureUri") @NotNull URI pictureUri, @JsonRpcParam("pageNumber") @Min(0) int pageNumber, @JsonRpcParam("pageSize") @Min(1) int pageSize) throws IOException;
	
	List<String> findKeywordSuggestions(@JsonRpcParam("partial") @NotEmpty String partial) throws IOException;
	
	QueryResult<AlbumInfo> getAlbums(@JsonRpcParam("profileId") @NotNull UUID profileId, @JsonRpcParam("pageNumber") @Min(0) int pageNumber, @JsonRpcParam("pageSize") @Min(1) int pageSize) throws IOException;
	
	AlbumInfo createEmptyAlbum(@JsonRpcParam("title") @NotEmpty @Size(max=AlbumConstants.MAX_TITLE_LENGTH) String title, @JsonRpcParam("description") @Size(max=AlbumConstants.MAX_DESCRIPTION_LENGTH) String description) throws IOException;
	
	AlbumInfo updateAlbum(@JsonRpcParam("albumId") @NotNull UUID albumId, @JsonRpcParam("title") @NotEmpty @Size(max=AlbumConstants.MAX_TITLE_LENGTH) String title, @JsonRpcParam("description") @Size(max=AlbumConstants.MAX_DESCRIPTION_LENGTH) String description, @JsonRpcParam("mediaList") @Size(max=AlbumConstants.MAX_MEDIA_COUNT) List<URI> mediaList) throws IOException;
	
	void deleteAlbum(@JsonRpcParam("albumId") @NotNull UUID albumId) throws IOException;
	
	List<TagInfo> getTopSearchedTags(@JsonRpcParam("count") @Min(1) int count) throws IOException;
	
	List<TagInfo> getTopUsedTags(@JsonRpcParam("count") @Min(1) int count) throws IOException;
	
	List<GeoStatistic> mapPictureMatchCount(@JsonRpcParam("latitude") double latitude, @JsonRpcParam("longitude") double longitude, @JsonRpcParam("radius") @Min(0) double radius, @JsonRpcParam("keywords") String keywords, @JsonRpcParam("maxAge") @Min(0) long maxAge, @JsonRpcParam("profileId") List<UUID> profileIds) throws IOException;
}
