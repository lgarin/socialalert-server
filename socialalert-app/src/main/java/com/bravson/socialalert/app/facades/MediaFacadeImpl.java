package com.bravson.socialalert.app.facades;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import com.bravson.socialalert.app.domain.ArchiveMediaTaskPayload;
import com.bravson.socialalert.app.domain.DeleteMediaTaskPayload;
import com.bravson.socialalert.app.domain.PictureMetadata;
import com.bravson.socialalert.app.domain.TagStatisticUpdate;
import com.bravson.socialalert.app.domain.VideoMetadata;
import com.bravson.socialalert.app.entities.ApplicationUser;
import com.bravson.socialalert.app.exceptions.DataMissingException;
import com.bravson.socialalert.app.exceptions.SystemExeption;
import com.bravson.socialalert.app.services.AlertActivityService;
import com.bravson.socialalert.app.services.AlertAlbumService;
import com.bravson.socialalert.app.services.AlertCommentService;
import com.bravson.socialalert.app.services.AlertInteractionService;
import com.bravson.socialalert.app.services.AlertMediaService;
import com.bravson.socialalert.app.services.ApplicationUserService;
import com.bravson.socialalert.app.services.GeocoderService;
import com.bravson.socialalert.app.services.MediaStorageService;
import com.bravson.socialalert.app.services.PictureFileService;
import com.bravson.socialalert.app.services.ProfileLinkService;
import com.bravson.socialalert.app.services.SearchHistoryService;
import com.bravson.socialalert.app.services.TagStatisticService;
import com.bravson.socialalert.app.services.UserSessionService;
import com.bravson.socialalert.app.services.VideoFileService;
import com.bravson.socialalert.app.tasks.QueuedTaskScheduler;
import com.bravson.socialalert.app.utilities.SecurityUtils;
import com.bravson.socialalert.common.domain.ActivityInfo;
import com.bravson.socialalert.common.domain.ActivityType;
import com.bravson.socialalert.common.domain.AlbumInfo;
import com.bravson.socialalert.common.domain.ApprovalModifier;
import com.bravson.socialalert.common.domain.CommentInfo;
import com.bravson.socialalert.common.domain.GeoAddress;
import com.bravson.socialalert.common.domain.GeoArea;
import com.bravson.socialalert.common.domain.GeoStatistic;
import com.bravson.socialalert.common.domain.MediaCategory;
import com.bravson.socialalert.common.domain.MediaConstants;
import com.bravson.socialalert.common.domain.MediaInfo;
import com.bravson.socialalert.common.domain.MediaType;
import com.bravson.socialalert.common.domain.QueryResult;
import com.bravson.socialalert.common.domain.TagInfo;
import com.bravson.socialalert.common.facade.MediaFacade;
import com.drew.imaging.jpeg.JpegProcessingException;

@Service
@Validated
public class MediaFacadeImpl implements MediaFacade {

	@Resource
	private AlertMediaService alertService;
	
	@Resource
	private MediaStorageService storageService;
	
	@Resource
	private VideoFileService videoService;
	
	@Resource
	private PictureFileService pictureService;
	
	@Resource
	private AlertInteractionService interactionService;
	
	@Resource
	private QueuedTaskScheduler queuedTaskScheduler;
	
	@Resource
	private ApplicationUserService userService;
	
	@Resource
	private GeocoderService geocoderService;
	
	@Resource
	private AlertCommentService commentService;
	
	@Resource
	private SearchHistoryService historyService;
	
	@Resource
	private AlertActivityService activityService;
	
	@Resource
	private AlertAlbumService albumService;
	
	@Resource
	private TagStatisticService tagService;
	
	@Resource
	private ProfileLinkService linkService;
	
	@Resource
	private UserSessionService sessionService;
	
	@Value("${media.delete.delay}")
	private long mediaDeleteDelay;
	
	@Override
	@PreAuthorize("hasRole('USER')")
	@Transactional(rollbackFor={Throwable.class})
	public MediaInfo claimPicture(URI pictureUri, String title, GeoAddress location, Collection<MediaCategory> categories, Collection<String> tags) {
		ApplicationUser user = SecurityUtils.findAuthenticatedPrincipal();
		PictureMetadata metadata = getPictureMetadata(pictureUri);
		DateTime claimTimestamp = DateTime.now(DateTimeZone.UTC);
		metadata.setDefaultTimestamp(claimTimestamp);
		if (metadata.getLatitude() != null && metadata.getLongitude() != null && location == null) {
			location = new GeoAddress();
			location.setLatitude(metadata.getLatitude());
			location.setLongitude(metadata.getLongitude());
		}
		URI finalUri = storageService.buildFinalMediaUri(pictureUri, claimTimestamp);
		MediaInfo info = alertService.createPictureAlert(finalUri, user.getProfileId(), title, location, metadata, categories, tags);
		activityService.addActivity(finalUri, user.getProfileId(), ActivityType.NEW_PICTURE, null);
		info.setCreator(user.getNickname());
		queuedTaskScheduler.scheduleTask(new ArchiveMediaTaskPayload(pictureUri, finalUri));
		return info;
	}
	
	private PictureMetadata getPictureMetadata(URI pictureUri) {
		File pictureFile = storageService.resolveMediaUri(pictureUri);
		if (pictureFile == null) {
			throw new DataMissingException("The picture " + pictureUri + " does not exists");
		}
		try {
			return pictureService.parseJpegMetadata(pictureFile);
		} catch (JpegProcessingException | IOException e) {
			throw new SystemExeption("Cannot parse picture " + pictureUri, e);
		}
	}
	
	@Override
	@PreAuthorize("hasRole('USER')")
	@Transactional(rollbackFor={Throwable.class})
	public MediaInfo claimVideo(URI videoUri, String title, GeoAddress location, Collection<MediaCategory> categories, Collection<String> tags) {
		ApplicationUser user = SecurityUtils.findAuthenticatedPrincipal();
		VideoMetadata metadata = getVideoMetadata(videoUri);
		DateTime claimTimestamp = DateTime.now(DateTimeZone.UTC);
		metadata.setDefaultTimestamp(claimTimestamp);
		if (metadata.getLatitude() != null && metadata.getLongitude() != null && location == null) {
			location = new GeoAddress();
			location.setLatitude(metadata.getLatitude());
			location.setLongitude(metadata.getLongitude());
		}
		URI finalUri = storageService.buildFinalMediaUri(videoUri, claimTimestamp);
		MediaInfo info = alertService.createVideoAlert(finalUri, user.getProfileId(), title, location, metadata, categories, tags);
		activityService.addActivity(finalUri, user.getProfileId(), ActivityType.NEW_PICTURE, null);
		info.setCreator(user.getNickname());
		queuedTaskScheduler.scheduleTask(new ArchiveMediaTaskPayload(videoUri, finalUri));
		return info;
	}
	
	private VideoMetadata getVideoMetadata(URI videoUri) {
		File videoFile = storageService.resolveMediaUri(videoUri);
		if (videoFile == null) {
			throw new DataMissingException("The video " + videoUri + " does not exists");
		}
		try {
			return videoService.parseMetadata(videoFile);
		} catch (IOException e) {
			throw new SystemExeption("Cannot parse video " + videoUri, e);
		}
	}
	
	@Override
	@PreAuthorize("hasRole('USER')")
	@Transactional(rollbackFor={Throwable.class})
	public MediaInfo updateMediaInfo(URI mediaUri, String title, String description, GeoAddress newLocation, String cameraMaker, String cameraModel, DateTime pictureTimestamp, Collection<MediaCategory> categories, Collection<String> tags) {
		ApplicationUser user = SecurityUtils.findAuthenticatedPrincipal();
		MediaInfo info = alertService.updateAlert(mediaUri, user.getProfileId(), title, description, newLocation, cameraMaker, cameraModel, pictureTimestamp, categories, tags);
		info.setCreator(user.getNickname());
		return info;
	}
	
	@Override
	public QueryResult<MediaInfo> searchMedia(MediaType mediaType, Double latitude, Double longitude, Double maxDistance, String keywords, long maxAge, int pageNumber, int pageSize) {
		GeoArea area = null;
		if (latitude != null && longitude != null && maxDistance != null) {
			area = new GeoArea(latitude, longitude, maxDistance);
		}
		ApplicationUser user = SecurityUtils.findAuthenticatedPrincipal();
		if (user != null) {
			historyService.addSearch(user.getProfileId(), keywords, area);
		}
		if (StringUtils.isNotBlank(keywords)) {
			tagService.updateTagStatistic(keywords, TagStatisticUpdate.INCREMENT_SEARCH_COUNT);
		}
		QueryResult<MediaInfo> items = alertService.searchMedia(mediaType, area, keywords, maxAge, pageNumber, pageSize);
		userService.populateCreators(items.getContent());
		userService.updateOnlineStatus(items.getContent());
		return items;
	}
	
	@Override
	public QueryResult<MediaInfo> searchMediaInCategory(MediaType mediaType, Double latitude, Double longitude, Double maxDistance, String keywords, long maxAge, String category, int pageNumber, int pageSize) {
		GeoArea area = null;
		if (latitude != null && longitude != null && maxDistance != null) {
			area = new GeoArea(latitude, longitude, maxDistance);
		}
		ApplicationUser user = SecurityUtils.findAuthenticatedPrincipal();
		if (user != null) {
			historyService.addSearch(user.getProfileId(), keywords, area);
		}
		if (StringUtils.isNotBlank(keywords)) {
			tagService.updateTagStatistic(keywords, TagStatisticUpdate.INCREMENT_SEARCH_COUNT);
		}
		QueryResult<MediaInfo> items = alertService.searchMediaInCategory(mediaType, area, keywords, maxAge, category, pageNumber, pageSize);
		userService.populateCreators(items.getContent());
		userService.updateOnlineStatus(items.getContent());
		return items;
	}
	
	@Override
	public Map<String, List<MediaInfo>> searchTopMediaByCategories(MediaType mediaType, Double latitude, Double longitude, Double maxDistance, String keywords, long maxAge, Collection<String> categories, int groupSize) {
		GeoArea area = null;
		if (latitude != null && longitude != null && maxDistance != null) {
			area = new GeoArea(latitude, longitude, maxDistance);
		}
		ApplicationUser user = SecurityUtils.findAuthenticatedPrincipal();
		if (user != null) {
			historyService.addSearch(user.getProfileId(), keywords, area);
		}
		if (StringUtils.isNotBlank(keywords)) {
			tagService.updateTagStatistic(keywords, TagStatisticUpdate.INCREMENT_SEARCH_COUNT);
		}
		HashMap<String, List<MediaInfo>> result = new HashMap<String, List<MediaInfo>>(categories.size());
		for (String category : categories) {
			QueryResult<MediaInfo> items = alertService.searchMediaInCategory(mediaType, area, keywords, maxAge, category, 0, groupSize);
			userService.populateCreators(items.getContent());
			userService.updateOnlineStatus(items.getContent());
			result.put(category, items.getContent());
		}
		return result;
	}

	@Override
	@PreAuthorize("hasRole('USER')")
	public QueryResult<MediaInfo> listMediaByProfile(MediaType mediaType, UUID profileId, int pageNumber, int pageSize) {
		QueryResult<MediaInfo> items = alertService.listMediaByProfile(mediaType, profileId, pageNumber, pageSize);
		userService.populateCreators(items.getContent());
		userService.updateOnlineStatus(items.getContent());
		return items;
	}

	@Override
	@Transactional(rollbackFor={Throwable.class})
	public MediaInfo viewMediaDetail(URI mediaUri) {
		ApplicationUser user = SecurityUtils.findAuthenticatedPrincipal();
		ApprovalModifier modifier = null;
		if (user != null && user.getProfileId() != null) {
			modifier = interactionService.getApprovalModifier(mediaUri, user.getProfileId());
		}
		MediaInfo result = alertService.viewMediaDetail(mediaUri).enrich(modifier);
		userService.populateCreators(Collections.singletonList(result));
		userService.updateOnlineStatus(Collections.singletonList(result));
		return result;
	}
	
	@Override
	@PreAuthorize("hasRole('USER')")
	@Transactional(rollbackFor={Throwable.class})
	public void deleteMedia(URI mediaUri) {
		ApplicationUser user = SecurityUtils.findAuthenticatedPrincipal();
		alertService.deleteMedia(mediaUri, user.getProfileId());
		queuedTaskScheduler.scheduleTask(new DeleteMediaTaskPayload(mediaUri, mediaDeleteDelay));
	}
	
	@Override
	@PreAuthorize("hasRole('USER')")
	@Transactional(rollbackFor={Throwable.class})
	public MediaInfo setMediaApproval(URI mediaUri, ApprovalModifier modifier) {
		ApplicationUser user = SecurityUtils.findAuthenticatedPrincipal();
		MediaInfo media = alertService.getMediaInfo(mediaUri);
		ApprovalModifier oldModifier = interactionService.setApprovalModifier(mediaUri, user.getProfileId(), modifier);
		if (modifier != null) {
			activityService.addActivity(mediaUri, user.getProfileId(), modifier.toActivtiyType(), null);
		}
		if (modifier == ApprovalModifier.LIKE) {
			linkService.increaseActivityWeight(user.getProfileId(), media.getProfileId(), MediaConstants.LIKE_WEIGHT);
		} else if (modifier == ApprovalModifier.DISLIKE) {
			linkService.increaseActivityWeight(user.getProfileId(), media.getProfileId(), MediaConstants.DISLIKE_WEIGHT);
		}
		MediaInfo result = alertService.updateLikeDislike(mediaUri, oldModifier, modifier).enrich(modifier);
		userService.populateCreators(Collections.singletonList(result));
		userService.updateOnlineStatus(Collections.singletonList(result));
		return result;
	}
	
	@Override
	@PreAuthorize("hasRole('USER')")
	public List<GeoAddress> findLocation(String address, String region, String preferredLanguage) {
		return geocoderService.findLocation(address, region, preferredLanguage);
	}

	@Override
	@PreAuthorize("hasRole('USER')")
	@Transactional(rollbackFor={Throwable.class})
	public CommentInfo addComment(URI mediaUri, String comment) {
		ApplicationUser user = SecurityUtils.findAuthenticatedPrincipal();
		MediaInfo media = alertService.getMediaInfo(mediaUri);
		CommentInfo info = commentService.addComment(mediaUri, user.getProfileId(), comment);
		activityService.addActivity(mediaUri, user.getProfileId(), ActivityType.NEW_COMMENT, info.getCommentId());
		linkService.increaseActivityWeight(user.getProfileId(), media.getProfileId(), 1);
		alertService.increaseCommentCount(mediaUri);
		info.setCreator(user.getNickname());
		info.setOnline(true);
		return info;
	}
	
	@Override
	@PreAuthorize("hasRole('USER')")
	@Transactional(rollbackFor={Throwable.class})
	public ActivityInfo repostComment(UUID commentId) {
		if (!sessionService.addRepostedComment(commentId)) {
			return null;
		}
		ApplicationUser user = SecurityUtils.findAuthenticatedPrincipal();
		CommentInfo comment = commentService.getCommentInfo(commentId);
		ActivityInfo activity = activityService.addActivity(comment.getMediaUri(), user.getProfileId(), ActivityType.REPOST_COMMENT, comment.getCommentId());
		linkService.increaseActivityWeight(user.getProfileId(), comment.getProfileId(), 1);
		activity.setCreator(user.getNickname());
		activity.setMessage(comment.getComment());
		userService.updateOnlineStatus(Collections.singleton(activity));
		return activity;
	}
	
	@Override
	@PreAuthorize("hasRole('USER')")
	@Transactional(rollbackFor={Throwable.class})
	public ActivityInfo repostMedia(URI mediaUri) {
		if (!sessionService.addRepostedUri(mediaUri)) {
			return null;
		}
		ApplicationUser user = SecurityUtils.findAuthenticatedPrincipal();
		MediaInfo media = alertService.getMediaInfo(mediaUri);
		ActivityInfo activity = activityService.addActivity(mediaUri, user.getProfileId(), ActivityType.REPOST_PICTURE, null);
		linkService.increaseActivityWeight(user.getProfileId(), media.getProfileId(), 1);
		activity.setCreator(user.getNickname());
		userService.updateOnlineStatus(Collections.singleton(activity));
		return activity;
	}
	
	@Override
	public QueryResult<CommentInfo> listComments(URI pictureUri, int pageNumber, int pageSize) {
		QueryResult<CommentInfo> items = commentService.searchCommentByMediaUri(pictureUri, pageNumber, pageSize);
		userService.populateCreators(items.getContent());
		userService.updateOnlineStatus(items.getContent());
		return items;
	}
	
	@Override
	public List<String> findKeywordSuggestions(MediaType mediaType, String partial) {
		return alertService.findKeywordSuggestions(mediaType, partial);
	}
	
	@Override
	public QueryResult<AlbumInfo> getAlbums(UUID profileId, int pageNumber, int pageSize) {
		QueryResult<AlbumInfo> items = albumService.getAlbums(profileId, pageNumber, pageSize);
		userService.populateCreators(items.getContent());
		userService.updateOnlineStatus(items.getContent());
		return items;
	}
	
	@Override
	@PreAuthorize("hasRole('USER')")
	@Transactional(rollbackFor={Throwable.class})
	public AlbumInfo createEmptyAlbum(String title, String description) {
		ApplicationUser user = SecurityUtils.findAuthenticatedPrincipal();
		AlbumInfo item = albumService.createEmptyAlbum(user.getProfileId(), title, description);
		userService.populateCreators(Collections.singletonList(item));
		userService.updateOnlineStatus(Collections.singletonList(item));
		return item;
	}
	
	@Override
	@PreAuthorize("hasRole('USER')")
	@Transactional(rollbackFor={Throwable.class})
	public AlbumInfo updateAlbum(UUID albumId, String title, String description, List<URI> mediaList) {
		ApplicationUser user = SecurityUtils.findAuthenticatedPrincipal();
		AlbumInfo item = albumService.updateAlbum(albumId, user.getProfileId(), title, description, mediaList);
		userService.populateCreators(Collections.singletonList(item));
		userService.updateOnlineStatus(Collections.singletonList(item));
		return item;
	}
	
	@Override
	@PreAuthorize("hasRole('USER')")
	@Transactional(rollbackFor={Throwable.class})
	public void deleteAlbum(UUID albumId) {
		ApplicationUser user = SecurityUtils.findAuthenticatedPrincipal();
		albumService.deleteAlbum(albumId, user.getProfileId());
	}
	
	@Override
	public List<TagInfo> getTopSearchedTags(int count) {
		return tagService.getTopSearchedTags(count);
	}
	
	@Override
	public List<TagInfo> getTopUsedTags(int count) {
		return tagService.getTopUsedTags(count);
	}
	
	@Override
	public List<GeoStatistic> mapMediaMatchCount(MediaType mediaType, double latitude, double longitude, double radius, String keywords, long maxAge, List<UUID> profileIds) {
		GeoArea area = new GeoArea(latitude, longitude, radius);
		return alertService.mapMediaMatchCount(mediaType, area, keywords, maxAge, profileIds);
	}
}
