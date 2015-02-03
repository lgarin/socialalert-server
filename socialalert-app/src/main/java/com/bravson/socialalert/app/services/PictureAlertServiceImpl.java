package com.bravson.socialalert.app.services;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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
import com.bravson.socialalert.app.entities.PictureAlert;
import com.bravson.socialalert.app.exceptions.DataMissingException;
import com.bravson.socialalert.app.repositories.PictureAlertRepository;
import com.bravson.socialalert.app.utilities.SolrUtils;
import com.bravson.socialalert.common.domain.ApprovalModifier;
import com.bravson.socialalert.common.domain.GeoAddress;
import com.bravson.socialalert.common.domain.GeoArea;
import com.bravson.socialalert.common.domain.GeoStatistic;
import com.bravson.socialalert.common.domain.MediaCategory;
import com.bravson.socialalert.common.domain.PictureInfo;
import com.bravson.socialalert.common.domain.QueryResult;

@Service
public class PictureAlertServiceImpl implements PictureAlertService {

	public static final int MAX_FACET_RESULTS = 1000;
	
	@Resource
	private PictureAlertRepository pictureRepository;
	
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
	public PictureInfo createAlert(URI pictureUri, UUID profileId, String title, GeoAddress location, PictureMetadata metadata, Collection<MediaCategory> categories, Collection<String> tags) {
		if (pictureRepository.exists(pictureUri)) {
			throw new DuplicateKeyException("The picture " + pictureUri + " has already been claimed");
		}
		PictureAlert entity = new PictureAlert(pictureUri, profileId, title, metadata, categories, tags);
		if (location != null) {
			String geohash = geocoderService.encodeLatLon(location.getLatitude(), location.getLongitude(), PictureAlert.GEOHASH_PRECISION);
			entity.updateLocation(location.getLatitude(), location.getLongitude(), geohash, location.getLocality(), location.getCountry());
		}
		pictureRepository.save(entity);
		statisticService.updateProfileStatistic(profileId, ProfileStatisticUpdate.INCREMENT_PICTURE_COUNT);
		updateTagsStatistic(Collections.<String>emptyList(), tags);
		return entity.toPictureInfo();
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
	public PictureInfo updateAlert(URI pictureUri, UUID profileId, String title, String description, GeoAddress location, String cameraMaker, String cameraModel, DateTime pictureTimestamp, Collection<MediaCategory> categories, Collection<String> tags) {
		PictureAlert pictureAlert = pictureRepository.findById(pictureUri);
		if (pictureAlert == null) {
			throw new DataMissingException("No picture with URI " + pictureUri);
		}
		if (!pictureAlert.isOwnedBy(profileId)) {
			throw new IllegalArgumentException("The current user does not owned the picture " + pictureUri);
		}
		updateTagsStatistic(pictureAlert.getTags(), tags);
		//updateTagsStatistic(Collections.<String>emptyList(), tags);
		pictureAlert.update(title, description, cameraMaker, cameraModel, pictureTimestamp, categories, tags);
		if (location != null) {
			String geohash = geocoderService.encodeLatLon(location.getLatitude(), location.getLongitude(), PictureAlert.GEOHASH_PRECISION);
			pictureAlert.updateLocation(location.getLatitude(), location.getLongitude(), geohash, location.getLocality(), location.getCountry());
		}
		return pictureRepository.save(pictureAlert).toPictureInfo();
	}

	private static QueryResult<PictureInfo> toQueryResult(Page<PictureAlert> page) {
		ArrayList<PictureInfo> pageContent = new ArrayList<>(page.getSize());
		for (PictureAlert alert : page) {
			pageContent.add(alert.toPictureInfo());
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
	
	@Override
	public QueryResult<PictureInfo> searchPictures(GeoArea area, String keywords, long maxAge, int pageNumber, int pageSize) {
		keywords = SolrUtils.escapeSolrCharacters(keywords);
		PageRequest pageRequest = createPageRequest(pageNumber, pageSize, null);
		if (area != null && StringUtils.isNotBlank(keywords)) {
			Point location = new Point(area.getLatitude(), area.getLongitude());
			Distance maxDistance = new Distance(area.getRadius());
			return toQueryResult(pictureRepository.findWithinAreaWithKeywords(location, maxDistance, keywords, maxAge / DateUtils.MILLIS_PER_MINUTE,  pageRequest));
		} else if (area != null) {
			Point location = new Point(area.getLatitude(), area.getLongitude());
			Distance maxDistance = new Distance(area.getRadius());
			return toQueryResult(pictureRepository.findWithinArea(location, maxDistance, maxAge / DateUtils.MILLIS_PER_MINUTE, pageRequest));
		} else if (StringUtils.isNotBlank(keywords)) {
			return toQueryResult(pictureRepository.findWithKeywords(keywords, maxAge / DateUtils.MILLIS_PER_MINUTE, pageRequest));
		} else {
			return toQueryResult(pictureRepository.findRecent(maxAge / DateUtils.MILLIS_PER_MINUTE, pageRequest));
		}
	}
	
	@Override
	public QueryResult<PictureInfo> searchPicturesInCategory(GeoArea area, String keywords, long maxAge, String category, int pageNumber, int pageSize) {
		keywords = SolrUtils.escapeSolrCharacters(keywords);
		PageRequest pageRequest = createPageRequest(pageNumber, pageSize, null);
		if (area != null && StringUtils.isNotBlank(keywords)) {
			Point location = new Point(area.getLatitude(), area.getLongitude());
			Distance maxDistance = new Distance(area.getRadius());
			// TODO by category
			return toQueryResult(pictureRepository.findWithinAreaWithKeywordsByCategory(location, maxDistance, keywords, maxAge / DateUtils.MILLIS_PER_MINUTE, category,  pageRequest));
		} else if (area != null) {
			Point location = new Point(area.getLatitude(), area.getLongitude());
			Distance maxDistance = new Distance(area.getRadius());
			// TODO by category
			return toQueryResult(pictureRepository.findWithinAreaByCategory(location, maxDistance, maxAge / DateUtils.MILLIS_PER_MINUTE, category, pageRequest));
		} else if (StringUtils.isNotBlank(keywords)) {
			return toQueryResult(pictureRepository.findWithKeywordsByCategory(keywords, maxAge / DateUtils.MILLIS_PER_MINUTE, category, pageRequest));
		} else {
			return toQueryResult(pictureRepository.findRecentByCategory(maxAge / DateUtils.MILLIS_PER_MINUTE, category, pageRequest));
		}
	}

	private FacetPage<PictureAlert> queryWithGeohashFacet(GeoArea area, String keywords, long maxAge, List<UUID> profileIds) {
		keywords = SolrUtils.escapeSolrCharacters(keywords);
		int precision = Math.min(geocoderService.computeGeoHashLength(area) + 1, 7);
		FacetOptions facetOptions = new FacetOptions("geohash" + precision);
		facetOptions.setFacetLimit(MAX_FACET_RESULTS);
		facetOptions.setFacetMinCount(1);
		PageRequest pageRequest = createPageRequest(0, maxPageSize, null);
		
		ArrayList<Criteria> filters = new ArrayList<>(4);
		Point location = new Point(area.getLatitude(), area.getLongitude());
		Distance distance = new Distance(area.getRadius());
		filters.add(new Criteria("pictureLocation").near(location, distance));
		if (!StringUtils.isEmpty(keywords)) {
			filters.add(new Criteria("title").is(keywords).or("tags").is(keywords));
		}
		filters.add(new Criteria("creation").between("NOW/MINUTES-" + maxAge / DateUtils.MILLIS_PER_MINUTE + "MINUTES", "NOW"));
		if (profileIds != null) {
			filters.add(new Criteria("profileId").in(profileIds));
		}
		return pictureRepository.queryForFacetPage(new SimpleStringCriteria("*:*"), filters, facetOptions, pageRequest);
	}
	
	@Override
	public List<GeoStatistic> mapPictureMatchCount(GeoArea area, String keywords, long maxAge, List<UUID> profileIds) {
		Page<FacetFieldEntry> page = queryWithGeohashFacet(area, keywords, maxAge, profileIds).getFacetResultPages().iterator().next();
		ArrayList<GeoStatistic> result = new ArrayList<>(page.getNumberOfElements());
		for (FacetFieldEntry entry : page) {
			GeoArea key = geocoderService.decodeGeoHash(entry.getValue());
			long value = entry.getValueCount();
			result.add(new GeoStatistic(key.getLatitude(), key.getLongitude(), key.getRadius(), value));
		}
		return result;
	}
	
	@Override
	public QueryResult<PictureInfo> listPicturesByProfile(UUID profileId, int pageNumber, int pageSize) {
		PageRequest pageRequest = createPageRequest(pageNumber, pageSize, new Sort(Direction.DESC, "lastUpdate"));
		return toQueryResult(pictureRepository.findByProfileId(profileId, pageRequest));
	}
	
	@Transactional(rollbackFor={Throwable.class})
	@Override
	public PictureInfo viewPictureDetail(URI pictureUri) {
		PictureAlert pictureAlert = pictureRepository.lockById(pictureUri);
		if (pictureAlert == null) {
			throw new DataMissingException("No picture with URI " + pictureUri);
		}
		if (sessionService.addViewedUri(pictureUri)) {
			pictureAlert.increaseHitCount();
			pictureRepository.save(pictureAlert);
			statisticService.updateProfileStatistic(pictureAlert.getProfileId(), ProfileStatisticUpdate.INCREMENT_HIT_COUNT);
		}
		return pictureAlert.toPictureInfo();
	}
	
	@Override
	public PictureInfo getPictureInfo(URI pictureUri) {
		PictureAlert pictureAlert = pictureRepository.findById(pictureUri);
		if (pictureAlert == null) {
			throw new DataMissingException("No picture with URI " + pictureUri);
		}
		return pictureAlert.toPictureInfo();
	}
	
	@Transactional(rollbackFor={Throwable.class})
	@Override
	public void deletePicture(URI pictureUri, UUID profileId) {
		PictureAlert pictureAlert = pictureRepository.lockById(pictureUri);
		if (pictureAlert == null) {
			throw new DataMissingException("No picture with URI " + pictureUri);
		}
		if (!pictureAlert.isOwnedBy(profileId)) {
			throw new IllegalArgumentException("The current user does not owned the picture " + pictureUri);
		}
		pictureRepository.delete(pictureUri);
		statisticService.updateProfileStatistic(pictureAlert.getProfileId(), ProfileStatisticUpdate.DECREMENT_PICTURE_COUNT);
	}
	
	@Transactional(rollbackFor={Throwable.class})
	@Override
	public PictureInfo updateLikeDislike(URI pictureUri, ApprovalModifier oldModifier, ApprovalModifier newModifier) {
		PictureAlert pictureAlert = pictureRepository.lockById(pictureUri);
		if (pictureAlert == null) {
			throw new DataMissingException("No picture with URI " + pictureUri);
		}
		int likeDelta = ApprovalModifier.computeLikeDelta(oldModifier, newModifier);
		int dislikeDelta = ApprovalModifier.computeDislikeDelta(oldModifier, newModifier);
		pictureAlert.updateLikeDislikeCount(likeDelta, dislikeDelta);
		pictureRepository.save(pictureAlert);
		if (likeDelta > 0) {
			statisticService.updateProfileStatistic(pictureAlert.getProfileId(), ProfileStatisticUpdate.INCREMENT_LIKE_COUNT);
		} else if (likeDelta < 0) {
			statisticService.updateProfileStatistic(pictureAlert.getProfileId(), ProfileStatisticUpdate.DECREMENT_LIKE_COUNT);
		}
		if (dislikeDelta > 0) {
			statisticService.updateProfileStatistic(pictureAlert.getProfileId(), ProfileStatisticUpdate.INCREMENT_DISLIKE_COUNT);
		} else if (dislikeDelta < 0) {
			statisticService.updateProfileStatistic(pictureAlert.getProfileId(), ProfileStatisticUpdate.DECREMENT_DISLIKE_COUNT);
		}
		return pictureAlert.toPictureInfo();
	}
	
	@Transactional(rollbackFor={Throwable.class})
	@Override
	public PictureInfo increaseCommentCount(URI pictureUri) {
		PictureAlert pictureAlert = pictureRepository.lockById(pictureUri);
		if (pictureAlert == null) {
			throw new DataMissingException("No picture with URI " + pictureUri);
		}
		pictureAlert.increaseCommentCount();
		pictureRepository.save(pictureAlert);
		return pictureAlert.toPictureInfo();
	}
	
	@Override
	public List<String> findKeywordSuggestions(String partial) {
		return pictureRepository.findSuggestion(partial);
	}
}
