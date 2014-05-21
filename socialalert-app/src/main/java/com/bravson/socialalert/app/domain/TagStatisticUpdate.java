package com.bravson.socialalert.app.domain;

import org.springframework.data.solr.core.query.SimpleUpdateField;
import org.springframework.data.solr.core.query.UpdateAction;
import org.springframework.data.solr.core.query.UpdateField;

public enum TagStatisticUpdate {

	INCREMENT_SEARCH_COUNT("searchCount", 1),
	INCREMENT_USE_COUNT("useCount", 1),
	DECREMENT_USE_COUNT("useCount", -1);
	
	private final SimpleUpdateField updateField;
	
	private TagStatisticUpdate(String fieldName, int delta) {
		updateField = new SimpleUpdateField(fieldName, delta, UpdateAction.INC);
	}
	
	public UpdateField getUpdateField() {
		return updateField;
	}
}
