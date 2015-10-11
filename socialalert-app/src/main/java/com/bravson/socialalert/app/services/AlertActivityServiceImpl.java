package com.bravson.socialalert.app.services;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.solr.core.query.result.FacetFieldEntry;
import org.springframework.stereotype.Service;

import com.bravson.socialalert.app.entities.AlertActivity;
import com.bravson.socialalert.app.entities.UserProfile;
import com.bravson.socialalert.app.repositories.AlertActivityRepository;
import com.bravson.socialalert.app.repositories.ApplicationEventRepository;
import com.bravson.socialalert.common.domain.ActivityCount;
import com.bravson.socialalert.common.domain.ActivityInfo;
import com.bravson.socialalert.common.domain.ActivityType;
import com.bravson.socialalert.common.domain.MediaInfo;
import com.bravson.socialalert.common.domain.QueryResult;

@Service
public class AlertActivityServiceImpl implements AlertActivityService {

	@Resource
	private AlertActivityRepository activityRepository;
	
	@Resource
	private AlertMediaService mediaService;
	
	@Autowired
	private UserProfileService profileService;
	
	@Autowired(required=false)
	private HttpServletRequest httpRequest;
	
	@Value("${query.max.result}")
	private int maxPageSize;
	
	private PageRequest createPageRequest(int pageNumber, int pageSize) {
		if (pageSize > maxPageSize) {
			throw new IllegalArgumentException("Page size is limited to " + maxPageSize);
		}
		PageRequest pageRequest = new PageRequest(pageNumber, pageSize, Direction.DESC, "creation");
		return pageRequest;
	}
	
	@Override
	public ActivityInfo addActivity(URI mediaUri, UUID profileId, ActivityType activityType, UUID commentId) {
		MediaInfo picture = mediaService.getMediaInfo(mediaUri);
		UserProfile userProfile = profileService.getProfileById(profileId);
		String ipAddress = httpRequest != null ? httpRequest.getRemoteAddr() : null;
		return activityRepository.save(new AlertActivity(mediaUri, picture.getProfileId(), profileId, activityType, commentId, userProfile.getCountry(), ipAddress)).toActivityInfo();
	}
	
	private static QueryResult<ActivityInfo> toQueryResult(Page<AlertActivity> page) {
		ArrayList<ActivityInfo> pageContent = new ArrayList<>(page.getSize());
		for (AlertActivity activity : page) {
			pageContent.add(activity.toActivityInfo());
		}
		return new QueryResult<>(pageContent, page.getNumber(), page.getTotalPages());
	}
	
	@Override
	public QueryResult<ActivityInfo> searchActivityBySourceProfileId(List<UUID> profileIdList, int pageNumber, int pageSize) {
		return toQueryResult(activityRepository.findBySourceIdList(profileIdList, createPageRequest(pageNumber, pageSize)));
	}
	
	@Override
	public List<ActivityCount> getRecentActivityStatistic(List<UUID> profileIdList, DateTime lastCheck) {
		if (profileIdList.isEmpty()) {
			return Collections.emptyList();
		}
		Page<FacetFieldEntry> page = activityRepository.groupRecentActivity(lastCheck, profileIdList, createPageRequest(0, maxPageSize)).getFacetResultPages().iterator().next();
		ArrayList<ActivityCount> result = new ArrayList<>(page.getNumberOfElements());
		for (FacetFieldEntry entry : page) {
			ActivityType activityType = ActivityType.valueOf(entry.getValue());
			long value = entry.getValueCount();
			result.add(new ActivityCount(activityType, value));
		}
		return result;
	}
}
