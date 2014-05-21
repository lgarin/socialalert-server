package com.bravson.socialalert.app.infrastructure;

import java.util.Map;

import org.apache.solr.client.solrj.request.QueryRequest;
import org.apache.solr.common.params.MapSolrParams;

public class SolrCustomRequest extends QueryRequest {

	private static final long serialVersionUID = 1L;

	private String path;
	
	public SolrCustomRequest(String path, Map<String,String> params) {
		super(new MapSolrParams(params));
		this.path = path;
	}
	
	@Override
	public String getPath() {
		return path;
	}

}
