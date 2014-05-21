package com.bravson.socialalert.services;

import java.util.List;

import javax.annotation.Resource;

import org.junit.Before;
import org.junit.Test;

import com.bravson.socialalert.app.domain.TagStatisticUpdate;
import com.bravson.socialalert.app.entities.TagStatistic;
import com.bravson.socialalert.app.services.TagStatisticService;
import com.bravson.socialalert.common.domain.TagInfo;
import com.bravson.socialalert.infrastructure.DataServiceTest;

public class TagStatisticServiceTest extends DataServiceTest {

	@Resource
	private TagStatisticService service;

	@Before
	public void setUp() throws Exception {
		fullImport(TagStatistic.class);
	}
	
	@Test
	public void readTopSearchedTag() {
		List<TagInfo> result = service.getTopSearchedTags(1);
		assertEquals(1, result.size());
		assertEquals("sport", result.get(0).getTag());
	}
	
	@Test
	public void readTopUsedTag() {
		List<TagInfo> result = service.getTopUsedTags(1);
		assertEquals(1, result.size());
		assertEquals("mountain", result.get(0).getTag());
	}
	
	@Test
	public void updateExistingStatistic() {
		String tag = "mountain";
		service.updateTagStatistic(tag, TagStatisticUpdate.DECREMENT_USE_COUNT);
		TagInfo info = service.getTagInfo(tag);
		assertEquals(tag, info.getTag());
		assertEquals(1, info.getUseCount());
		assertEquals(10, info.getSearchCount());
	}
	
	@Test
	public void updateNonExistingStatistic() {
		String tag = "test";
		service.updateTagStatistic(tag, TagStatisticUpdate.INCREMENT_USE_COUNT);
		TagInfo info = service.getTagInfo(tag);
		assertEquals(tag, info.getTag());
		assertEquals(1, info.getUseCount());
		assertEquals(0, info.getSearchCount());
	}
	
	@Test
	public void getNonExistingTag() {
		String tag = "test";
		TagInfo info = service.getTagInfo(tag);
		assertEquals(tag, info.getTag());
		assertEquals(0, info.getUseCount());
		assertEquals(0, info.getSearchCount());
	}
	
	@Test
	public void getExistingTag() {
		String tag = "mountain";
		TagInfo info = service.getTagInfo(tag);
		assertEquals(tag, info.getTag());
		assertEquals(2, info.getUseCount());
		assertEquals(10, info.getSearchCount());
	}
}

