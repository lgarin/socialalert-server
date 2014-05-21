package com.bravson.socialalert.app.services;

import java.util.UUID;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import org.springframework.validation.annotation.Validated;

import com.bravson.socialalert.app.domain.ProfileStatisticUpdate;
import com.bravson.socialalert.common.domain.QueryResult;
import com.bravson.socialalert.common.domain.ProfileStatisticInfo;

@Validated
public interface ProfileStatisticService {

	void updateProfileStatistic(@NotNull UUID profileId, @NotNull ProfileStatisticUpdate increment);

	ProfileStatisticInfo getProfileStatistic(@NotNull UUID profileId);
	
	QueryResult<ProfileStatisticInfo> getTopCreators(@Min(0) int pageNumber, @Min(1) int pageSize);
}
