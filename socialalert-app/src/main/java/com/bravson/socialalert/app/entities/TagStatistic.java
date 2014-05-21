package com.bravson.socialalert.app.entities;

import org.apache.solr.client.solrj.beans.Field;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.solr.core.mapping.SolrDocument;

import com.bravson.socialalert.common.domain.TagInfo;

@SolrDocument(solrCoreName="TagStatistic")
public class TagStatistic {

	@Id
	@Field
	private String tag;
	
	@Field
	@Version
	private long _version_;
	
	@Field
	private int useCount;
	
	@Field
	private int searchCount;
	
	public TagInfo toTagInfo() {
		TagInfo info = new TagInfo();
		info.setTag(tag);
		info.setUseCount(useCount);
		info.setSearchCount(searchCount);
		return info;
	}
}
