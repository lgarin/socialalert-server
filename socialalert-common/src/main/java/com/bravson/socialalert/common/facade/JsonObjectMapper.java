package com.bravson.socialalert.common.facade;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;

public class JsonObjectMapper extends ObjectMapper {
	
	private static final long serialVersionUID = 1L;

	public JsonObjectMapper() {
		registerModule(new JodaModule());
	}

}
