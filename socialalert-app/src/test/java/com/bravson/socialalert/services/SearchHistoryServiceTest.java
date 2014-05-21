package com.bravson.socialalert.services;

import java.util.UUID;

import javax.annotation.Resource;

import org.junit.Test;

import com.bravson.socialalert.app.services.SearchHistoryService;
import com.bravson.socialalert.common.domain.GeoArea;
import com.bravson.socialalert.infrastructure.DataServiceTest;

public class SearchHistoryServiceTest extends DataServiceTest {

	@Resource
	private SearchHistoryService service;
	
	@Test
	public void addSearchWithKeywords() {
		UUID profileId = UUID.fromString("a95472c0-e0e6-11e2-a28f-0800200c9a77");
		service.addSearch(profileId, "test keyword", null);
	}
	
	@Test
	public void addSearchWithArea() {
		UUID profileId = UUID.fromString("a95472c0-e0e6-11e2-a28f-0800200c9a77");
		service.addSearch(profileId, null, new GeoArea(46.68, 7.86, 10.0));
	}
	
	@Test
	public void addSearchWithKeywordsAndArea() {
		UUID profileId = UUID.fromString("a95472c0-e0e6-11e2-a28f-0800200c9a77");
		service.addSearch(profileId, "test keyword", new GeoArea(46.68, 7.86, 10.0));
	}
}
