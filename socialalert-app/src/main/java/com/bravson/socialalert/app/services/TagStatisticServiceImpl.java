package com.bravson.socialalert.app.services;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bravson.socialalert.app.domain.TagStatisticUpdate;
import com.bravson.socialalert.app.entities.TagStatistic;
import com.bravson.socialalert.app.repositories.TagStatisticRepository;
import com.bravson.socialalert.common.domain.TagInfo;

@Service
public class TagStatisticServiceImpl implements TagStatisticService {

	@Resource
	private TagStatisticRepository statisticRepository;

	@Override
	@Transactional(rollbackFor={Throwable.class})
	public void updateTagStatistic(String tag, TagStatisticUpdate increment) {
		statisticRepository.partialUpdate(tag, Collections.singletonList(increment.getUpdateField()));
	}
	
	@Override
	public List<TagInfo> getTopSearchedTags(int count) {
		return toList(statisticRepository.findAll(new PageRequest(0, count, new Sort(Direction.DESC, "searchCount"))));
	}
	
	@Override
	public List<TagInfo> getTopUsedTags(int count) {
		return toList(statisticRepository.findAll(new PageRequest(0, count, new Sort(Direction.DESC, "useCount"))));
	}
	
	private List<TagInfo> toList(Page<TagStatistic> page) {
		List<TagInfo> result = new ArrayList<>(page.getNumberOfElements());
		for (TagStatistic tag : page.getContent()) {
			result.add(tag.toTagInfo());
		}
		return result;
	}
	
	@Override
	public TagInfo getTagInfo(String tag) {
		TagStatistic entity = statisticRepository.findById(tag);
		if (entity == null) {
			TagInfo result = new TagInfo();
			result.setTag(tag);
			return result;
		}
		return entity.toTagInfo();
	}
}
