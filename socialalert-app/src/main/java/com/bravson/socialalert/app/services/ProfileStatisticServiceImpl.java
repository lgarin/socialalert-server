package com.bravson.socialalert.app.services;

import java.util.ArrayList;
import java.util.Collections;
import java.util.UUID;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bravson.socialalert.app.domain.ProfileStatisticUpdate;
import com.bravson.socialalert.app.entities.ProfileStatistic;
import com.bravson.socialalert.app.repositories.ProfileStatisticRepository;
import com.bravson.socialalert.common.domain.ProfileStatisticInfo;
import com.bravson.socialalert.common.domain.QueryResult;

@Service
public class ProfileStatisticServiceImpl implements ProfileStatisticService {

	@Resource
	private ProfileStatisticRepository statisticRepository;

	@Value("${query.max.result}")
	private int maxPageSize;
	
	private PageRequest createPageRequest(int pageNumber, int pageSize) {
		if (pageSize > maxPageSize) {
			throw new IllegalArgumentException("Page size is limited to " + maxPageSize);
		}
		PageRequest pageRequest = new PageRequest(pageNumber, pageSize);
		return pageRequest;
	}
	
	private static QueryResult<ProfileStatisticInfo> toQueryResult(Page<ProfileStatistic> page) {
		ArrayList<ProfileStatisticInfo> pageContent = new ArrayList<>(page.getSize());
		for (ProfileStatistic statistic : page) {
			pageContent.add(statistic.toStatisticInfo());
		}
		return new QueryResult<>(pageContent, page.getNumber(), page.getTotalPages());
	}
	
	@Override
	@Transactional(rollbackFor={Throwable.class})
	public void updateProfileStatistic(UUID profileId, ProfileStatisticUpdate increment) {
		statisticRepository.partialUpdate(profileId, Collections.singletonList(increment.getUpdateField()));
	}
	
	@Override
	public ProfileStatisticInfo getProfileStatistic(UUID profileId) {
		ProfileStatistic statistic = statisticRepository.findById(profileId);
		if (statistic == null) {
			return new ProfileStatisticInfo();
		}
		return statistic.toStatisticInfo();
	}
	
	public QueryResult<ProfileStatisticInfo> getTopCreators(int pageNumber, int pageSize) {
		return toQueryResult(statisticRepository.findTopCreators(createPageRequest(pageNumber, pageSize)));
	}
}
