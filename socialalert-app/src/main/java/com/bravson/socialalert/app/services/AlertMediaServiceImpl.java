package com.bravson.socialalert.app.services;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Point;
import org.springframework.data.solr.core.query.Criteria;
import org.springframework.data.solr.core.query.FacetOptions;
import org.springframework.data.solr.core.query.SimpleStringCriteria;
import org.springframework.data.solr.core.query.result.FacetFieldEntry;
import org.springframework.data.solr.core.query.result.FacetPage;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bravson.socialalert.app.domain.PictureMetadata;
import com.bravson.socialalert.app.domain.ProfileStatisticUpdate;
import com.bravson.socialalert.app.domain.TagStatisticUpdate;
import com.bravson.socialalert.app.domain.VideoMetadata;
import com.bravson.socialalert.app.entities.AlertMedia;
import com.bravson.socialalert.app.exceptions.DataMissingException;
import com.bravson.socialalert.app.repositories.AlertMediaRepository;
import com.bravson.socialalert.app.utilities.SolrUtils;
import com.bravson.socialalert.common.domain.ApprovalModifier;
import com.bravson.socialalert.common.domain.GeoAddress;
import com.bravson.socialalert.common.domain.GeoArea;
import com.bravson.socialalert.common.domain.GeoStatistic;
import com.bravson.socialalert.common.domain.MediaCategory;
import com.bravson.socialalert.common.domain.MediaInfo;
import com.bravson.socialalert.common.domain.MediaType;
import com.bravson.socialalert.common.domain.QueryResult;

@Service
public class AlertMediaServiceImpl implements AlertMediaService {

	public static final int MAX_FACET_RESULTS = 1000;
	
	@Resource
	private AlertMediaRepository mediaRepository;
	
	@Resource
	private UserSessionService sessionService;
	
	@Resource
	private ProfileStatisticService statisticService;
	
	@Resource
	private TagStatisticService tagService;
	
	@Resource
	private GeocoderService geocoderService;
	
	@Value("${query.max.result}")
	private int maxPageSize;
	
	@Transactional(rollbackFor={Throwable.class})
	@Override
	public MediaInfo createPictureAlert(URI pictureUri, UUID profileId, String title, GeoAddress location, PictureMetadata metadata, Collection<MediaCategory> categories, Collection<String> tags) {
		if (mediaRepository.exists(pictureUri)) {
			throw new DuplicateKeyException("The picture " + pictureUri + " has already been claimed");
		}
		AlertMedia entity = new AlertMedia(pictureUri, profileId, title, metadata, categories, tags);
		updateLocation(entity, location);
		mediaRepository.save(entity);
		statisticService.updateProfileStatistic(profileId, ProfileStatisticUpdate.INCREMENT_PICTURE_COUNT);
		updateTagsStatistic(Collections.<String>emptyList(), tags);
		return entity.toMediaInfo();
	}

	private void updateLocation(AlertMedia entity, GeoAddress location) {
		if (location != null) {
			String geohash = geocoderService.encodeLatLon(location.getLatitude(), location.getLongitude(), AlertMedia.GEOHASH_PRECISION);
			entity.updateLocation(location.getLatitude(), location.getLongitude(), geohash, location.getLocality(), location.getCountry());
		}
	}
	
	@Transactional(rollbackFor={Throwable.class})
	@Override
	public MediaInfo createVideoAlert(URI videoUri, UUID profileId, String title, GeoAddress location, VideoMetadata metadata, Collection<MediaCategory> categories, Collection<String> tags) {
		if (mediaRepository.exists(videoUri)) {
			throw new DuplicateKeyException("The media " + videoUri + " has already been claimed");
		}
		AlertMedia entity = new AlertMedia(videoUri, profileId, title, metadata, categories, tags);
		updateLocation(entity, location);
		mediaRepository.save(entity);
		statisticService.updateProfileStatistic(profileId, ProfileStatisticUpdate.INCREMENT_VIDEO_COUNT);
		updateTagsStatistic(Collections.<String>emptyList(), tags);
		return entity.toMediaInfo();
	}
	
	private void updateTagsStatistic(Collection<String> oldTags, Collection<String> newTags) {
		for (String oldTag : oldTags) {
			if (!newTags.contains(oldTag)) {
				tagService.updateTagStatistic(oldTag, TagStatisticUpdate.DECREMENT_USE_COUNT);
			}
		}
		for (String newTag : newTags) {
			if (!oldTags.contains(newTag)) {
				tagService.updateTagStatistic(newTag, TagStatisticUpdate.INCREMENT_USE_COUNT);
			}
		}
	}
	
	@Transactional(rollbackFor={Throwable.class})
	@Override
	public MediaInfo updateAlert(URI mediaUri, UUID profileId, String title, String description, GeoAddress location, String cameraMaker, String cameraModel, DateTime pictureTimestamp, Collection<MediaCategory> categories, Collection<String> tags) {
		AlertMedia media = mediaRepository.findById(mediaUri);
		if (media == null) {
			throw new DataMissingException("No media with URI " + mediaUri);
		}
		if (!media.isOwnedBy(profileId)) {
			throw new IllegalArgumentException("The current user does not owned the media " + mediaUri);
		}
		updateTagsStatistic(media.getTags(), tags);
		//updateTagsStatistic(Collections.<String>emptyList(), tags);
		media.update(title, description, cameraMaker, cameraModel, pictureTimestamp, categories, tags);
		updateLocation(media, location);
		return mediaRepository.save(media).toMediaInfo();
	}

	private static QueryResult<MediaInfo> toQueryResult(Page<AlertMedia> page) {
		ArrayList<MediaInfo> pageContent = new ArrayList<>(page.getSize());
		for (AlertMedia media : page) {
			pageContent.add(media.toMediaInfo());
		}
		return new QueryResult<>(pageContent, page.getNumber(), page.getTotalPages());
	}
	
	private PageRequest createPageRequest(int pageNumber, int pageSize, Sort sort) {
		if (pageSize > maxPageSize) {
			throw new IllegalArgumentException("Page size is limited to " + maxPageSize);
		}
		PageRequest pageRequest = new PageRequest(pageNumber, pageSize, sort);
		return pageRequest;
	}
	
	private Collection<MediaType> toCollection(MediaType type) {
		if (type == null) {
			return EnumSet.allOf(MediaType.class);
		}
		return EnumSet.of(type);
	}
	
	@Override
	public QueryResult<MediaInfo> searchMedia(MediaType type, GeoArea area, String keywords, long maxAge, int pageNumber, int pageSize) {
		keywords = SolrUtils.escapeSolrCharacters(keywords);
		PageRequest pageRequest = createPageRequest(pageNumber, pageSize, null);
		if (area != null && StringUtils.isNotBlank(keywords)) {
			Point location = new Point(area.getLatitude(), area.getLongitude());
			Distance maxDistance = new Distance(area.getRadius());
			return toQueryResult(mediaRepository.findWithinAreaWithKeywords(toCollection(type), location, maxDistance, keywords, maxAge / DateUtils.MILLIS_PER_MINUTE,  pageRequest));
		} else if (area != null) {
			Point location = new Point(area.getLatitude(), area.getLongitude());
			Distance maxDistance = new Distance(area.getRadius());
			return toQueryResult(mediaRepository.findWithinArea(toCollection(type), location, maxDistance, maxAge / DateUtils.MILLIS_PER_MINUTE, pageRequest));
		} else if (StringUtils.isNotBlank(keywords)) {
			return toQueryResult(mediaRepository.findWithKeywords(toCollection(type), keywords, maxAge / DateUtils.MILLIS_PER_MINUTE, pageRequest));
		} else {
			return toQueryResult(mediaRepository.findRecent(toCollection(type), maxAge / DateUtils.MILLIS_PER_MINUTE, pageRequest));
		}
	}
	
	@Override
	public QueryResult<MediaInfo> searchMediaInCategory(MediaType type, GeoArea area, String keywords, long maxAge, String category, int pageNumber, int pageSize) {
		keywords = SolrUtils.escapeSolrCharacters(keywords);
		PageRequest pageRequest = createPageRequest(pageNumber, pageSize, null);
		if (area != null && StringUtils.isNotBlank(keywords)) {
			Point location = new Point(area.getLatitude(), area.getLongitude());
			Distance maxDistance = new Distance(area.getRadius());
			// TODO by category
			return toQueryResult(mediaRepository.findWithinAreaWithKeywordsByCategory(toCollection(type), location, maxDistance, keywords, maxAge / DateUtils.MILLIS_PER_MINUTE, category,  pageRequest));
		} else if (area != null) {
			Point location = new Point(area.getLatitude(), area.getLongitude());
			Distance maxDistance = new Distance(area.getRadius());
			// TODO by category
			return toQueryResult(mediaRepository.findWithinAreaByCategory(toCollection(type), location, maxDistance, maxAge / DateUtils.MILLIS_PER_MINUTE, category, pageRequest));
		} else if (StringUtils.isNotBlank(keywords)) {
			return toQueryResult(mediaRepository.findWithKeywordsByCategory(toCollection(type), keywords, maxAge / DateUtils.MILLIS_PER_MINUTE, category, pageRequest));
		} else {
			return toQueryResult(mediaRepository.findRecentByCategory(toCollection(type), maxAge / DateUtils.MILLIS_PER_MINUTE, category, pageRequest));
		}
	}

	private FacetPage<AlertMedia> queryWithGeohashFacet(MediaType type, GeoArea area, String keywords, long maxAge, List<UUID> profileIds) {
		keywords = SolrUtils.escapeSolrCharacters(keywords);
		int precision = Math.min(geocoderService.computeGeoHashLength(area) + 1, 7);
		FacetOptions facetOptions = new FacetOptions("geohash" + precision);
		facetOptions.setFacetLimit(MAX_FACET_RESULTS);
		facetOptions.setFacetMinCount(1);
		PageRequest pageRequest = createPageRequest(0, maxPageSize, null);
		
		ArrayList<Criteria> filters = new ArrayList<>(4);
		Point location = new Point(area.getLatitude(), area.getLongitude());
		Distance distance = new Distance(area.getRadius());
		filters.add(new Criteria("type").in(toCollection(type)));
		filters.add(new Criteria("location").near(location, distance));
		if (!StringUtils.isEmpty(keywords)) {
			filters.add(new Criteria("title").is(keywords).or("tags").is(keywords));
		}
		filters.add(new Criteria("creation").between("NOW/MINUTES-" + maxAge / DateUtils.MILLIS_PER_MINUTE + "MINUTES", "NOW"));
		if (profileIds != null) {
			filters.add(new Criteria("profileId").in(profileIds));
		}
		return mediaRepository.queryForFacetPage(new SimpleStringCriteria("*:*"), filters, facetOptions, pageRequest);
	}
	
	@Override
	public List<GeoStatistic> mapMediaMatchCount(MediaType type, GeoArea area, String keywords, long maxAge, List<UUID> profileIds) {
		Page<FacetFieldEntry> page = queryWithGeohashFacet(type, area, keywords, maxAge, profileIds).getFacetResultPages().iterator().next();
		ArrayList<GeoStatistic> result = new ArrayList<>(page.getNumberOfElements());
		for (FacetFieldEntry entry : page) {
			GeoArea key = geocoderService.decodeGeoHash(entry.getValue());
			long value = entry.getValueCount();
			result.add(new GeoStatistic(key.getLatitude(), key.getLongitude(), key.getRadius(), value));
		}
		return result;
	}
	
	@Override
	public QueryResult<MediaInfo> listMediaByProfile(MediaType type, UUID profileId, int pageNumber, int pageSize) {
		PageRequest pageRequest = createPageRequest(pageNumber, pageSize, new Sort(Direction.DESC, "lastUpdate"));
		return toQueryResult(mediaRepository.findByProfileId(toCollection(type), profileId, pageRequest));
	}
	
	@Transactional(rollbackFor={Throwable.class})
	@Override
	public MediaInfo viewMediaDetail(URI mediaUri) {
		AlertMedia media = mediaRepository.lockById(mediaUri);
		if (media == null) {
			throw new DataMissingException("No media with URI " + mediaUri);
		}
		if (sessionService.addViewedUri(mediaUri)) {
			media.increaseHitCount();
			mediaRepository.save(media);
			statisticService.updateProfileStatistic(media.getProfileId(), ProfileStatisticUpdate.INCREMENT_HIT_COUNT);
		}
		return media.toMediaInfo();
	}
	
	@Override
	public MediaInfo getMediaInfo(URI mediaUri) {
		AlertMedia media = mediaRepository.findById(mediaUri);
		if (media == null) {
			throw new DataMissingException("No media with URI " + mediaUri);
		}
		return media.toMediaInfo();
	}
	
	@Transactional(rollbackFor={Throwable.class})
	@Override
	public void deleteMedia(URI mediaUri, UUID profileId) {
		AlertMedia media = mediaRepository.lockById(mediaUri);
		if (media == null) {
			throw new DataMissingException("No media with URI " + mediaUri);
		}
		if (!media.isOwnedBy(profileId)) {
			throw new IllegalArgumentException("The current user does not owned the media " + mediaUri);
		}
		mediaRepository.delete(mediaUri);
		statisticService.updateProfileStatistic(media.getProfileId(), ProfileStatisticUpdate.DECREMENT_PICTURE_COUNT);
	}
	
	@Transactional(rollbackFor={Throwable.class})
	@Override
	public MediaInfo updateLikeDislike(URI mediaUri, ApprovalModifier oldModifier, ApprovalModifier newModifier) {
		AlertMedia media = mediaRepository.lockById(mediaUri);
		if (media == null) {
			throw new DataMissingException("No media with URI " + mediaUri);
		}
		int likeDelta = ApprovalModifier.computeLikeDelta(oldModifier, newModifier);
		int dislikeDelta = ApprovalModifier.computeDislikeDelta(oldModifier, newModifier);
		media.updateLikeDislikeCount(likeDelta, dislikeDelta);
		mediaRepository.save(media);
		if (likeDelta > 0) {
			statisticService.updateProfileStatistic(media.getProfileId(), ProfileStatisticUpdate.INCREMENT_LIKE_COUNT);
		} else if (likeDelta < 0) {
			statisticService.updateProfileStatistic(media.getProfileId(), ProfileStatisticUpdate.DECREMENT_LIKE_COUNT);
		}
		if (dislikeDelta > 0) {
			statisticService.updateProfileStatistic(media.getProfileId(), ProfileStatisticUpdate.INCREMENT_DISLIKE_COUNT);
		} else if (dislikeDelta < 0) {
			statisticService.updateProfileStatistic(media.getProfileId(), ProfileStatisticUpdate.DECREMENT_DISLIKE_COUNT);
		}
		return media.toMediaInfo();
	}
	
	@Transactional(rollbackFor={Throwable.class})
	@Override
	public MediaInfo increaseCommentCount(URI mediaUri) {
		AlertMedia media = mediaRepository.lockById(mediaUri);
		if (media == null) {
			throw new DataMissingException("No media with URI " + mediaUri);
		}
		media.increaseCommentCount();
		mediaRepository.save(media);
		return media.toMediaInfo();
	}
	
	@Override
	public List<String> findKeywordSuggestions(MediaType type, String partial) {
		// TODO use media type
		return mediaRepository.findSuggestion(partial);
	}
}
