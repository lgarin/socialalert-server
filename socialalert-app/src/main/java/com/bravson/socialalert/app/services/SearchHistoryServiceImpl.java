package com.bravson.socialalert.app.services;

import java.util.UUID;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.bravson.socialalert.app.entities.SearchHistory;
import com.bravson.socialalert.app.repositories.SearchHistoryRepository;
import com.bravson.socialalert.common.domain.GeoArea;

@Service
public class SearchHistoryServiceImpl implements SearchHistoryService {

	@Resource
	private SearchHistoryRepository searchRepository;
	
	@Override
	public void addSearch(UUID profileId, String keywords, GeoArea area) {
		if (keywords != null || area != null) {
			searchRepository.save(new SearchHistory(profileId, keywords, area));
		}
	}
}
