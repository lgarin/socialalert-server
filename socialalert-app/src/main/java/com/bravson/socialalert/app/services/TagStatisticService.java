package com.bravson.socialalert.app.services;

import java.util.List;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.validation.annotation.Validated;

import com.bravson.socialalert.app.domain.TagStatisticUpdate;
import com.bravson.socialalert.common.domain.TagInfo;

@Validated
public interface TagStatisticService {

	void updateTagStatistic(@NotEmpty String tag, @NotNull TagStatisticUpdate increment);
	
	List<TagInfo> getTopSearchedTags(@Min(1) int count);
	
	List<TagInfo> getTopUsedTags(@Min(1) int count);
	
	TagInfo getTagInfo(@NotEmpty String tag);
}
