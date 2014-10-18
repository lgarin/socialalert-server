package com.bravson.socialalert.facades;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.annotation.Resource;

import org.junit.Test;

import com.bravson.socialalert.common.domain.AbuseInfo;
import com.bravson.socialalert.common.domain.AbuseStatus;
import com.bravson.socialalert.common.facade.ControlFacade;
import com.bravson.socialalert.infrastructure.DataServiceTest;

public class ControlFacadeTest extends DataServiceTest {
	
	@Resource
	private ControlFacade facade;
	
	
	@Test
	public void listValidEnumValues() throws IOException {
		List<String> result = facade.listValidValues(AbuseStatus.class.getSimpleName());
		assertEquals(Arrays.asList("NEW", "PROCESSING", "CLOSED"), result);
	}
	
	@Test
	public void listNonEnumValues() throws IOException {
		List<String> result = facade.listValidValues(AbuseInfo.class.getSimpleName());
		assertEquals(Collections.emptyList(), result);
	}
	
	@Test
	public void listValuesOfUnknownEnum() throws IOException {
		List<String> result = facade.listValidValues("xyz");
		assertEquals(Collections.emptyList(), result);
	}
}
