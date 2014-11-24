package com.bravson.socialalert.app.services;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;
import org.joda.time.DateTime;
import org.springframework.validation.annotation.Validated;

import com.bravson.socialalert.common.domain.ActivityCount;
import com.bravson.socialalert.common.domain.ActivityInfo;
import com.bravson.socialalert.common.domain.ActivityType;
import com.bravson.socialalert.common.domain.QueryResult;

@Validated
public interface AlertActivityService {

	public ActivityInfo addActivity(@NotNull URI mediaUri, @NotNull UUID profileId, @NotNull ActivityType activityType, UUID commentId);

	public QueryResult<ActivityInfo> searchActivityBySourceProfileId(@NotEmpty List<UUID> profileIdList, @Min(0) int pageNumber, @Min(1) int pageSize);
	
	public List<ActivityCount> getRecentActivityStatistic(@NotNull List<UUID> profileIdList, @NotNull DateTime lastCheck); 
}
