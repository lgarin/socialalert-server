package com.bravson.socialalert.common.domain;

import java.util.List;

public class QueryResult<T> {

	private List<T> content;
	
	private int pageNumber;
	
	private int pageCount;
	
	public QueryResult() {
	}
	
	public QueryResult(List<T> content, int pageNumber, int pageCount) {
		this.content = content;
		this.pageNumber = pageNumber;
		this.pageCount = pageCount;
	}

	public int getPageCount() {
		return pageCount;
	}
	
	public int getPageNumber() {
		return pageNumber;
	}
	
	public List<T> getContent() {
		return content;
	}
}
