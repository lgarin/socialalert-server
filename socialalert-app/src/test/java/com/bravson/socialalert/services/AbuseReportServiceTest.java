package com.bravson.socialalert.services;

import java.net.URI;
import java.util.UUID;

import javax.annotation.Resource;

import org.junit.Before;
import org.junit.Test;

import com.bravson.socialalert.app.entities.AbuseReport;
import com.bravson.socialalert.app.entities.AlertComment;
import com.bravson.socialalert.app.entities.AlertMedia;
import com.bravson.socialalert.app.exceptions.DataMissingException;
import com.bravson.socialalert.app.services.AbuseReportService;
import com.bravson.socialalert.common.domain.AbuseInfo;
import com.bravson.socialalert.common.domain.AbuseReason;
import com.bravson.socialalert.common.domain.AbuseStatus;
import com.bravson.socialalert.infrastructure.DataServiceTest;

public class AbuseReportServiceTest extends DataServiceTest {

	@Resource
	private AbuseReportService service;

	@Before
	public void setUp() throws Exception {
		fullImport(AlertMedia.class);
		fullImport(AlertComment.class);
		fullImport(AbuseReport.class);
	}
	
	@Test
	public void createMediaReport() {
		UUID profileId = UUID.fromString("12345678-e0e6-11e2-a28f-0800200c9a77");
		URI mediaUri = URI.create("20130716/f317f3c7918c83ff6ec24aabb6c017fd.jpg");
		String country = "Switzerland";
		AbuseInfo abuse = service.reportAbusiveMedia(mediaUri, profileId, country, AbuseReason.VIOLENCE);
		assertNotNull(abuse);
		assertEquals(profileId, abuse.getProfileId());
		assertEquals(mediaUri, abuse.getMediaUri());
		assertNull(abuse.getCommentId());
		assertEquals(AbuseReason.VIOLENCE, abuse.getReason());
		assertEquals(AbuseStatus.NEW, abuse.getStatus());
		assertEquals(country, abuse.getCountry());
		assertNull(abuse.getMessage());
		assertNull(abuse.getCreator());
		assertNotNull(abuse.getTimestamp());
	}
	
	@Test(expected=DataMissingException.class)
	public void createReportForInvalidMedia() {
		UUID profileId = UUID.fromString("12345678-e0e6-11e2-a28f-0800200c9a77");
		URI mediaUri = URI.create("xyz.jpg");
		String country = "Switzerland";
		service.reportAbusiveMedia(mediaUri, profileId, country, AbuseReason.VIOLENCE);
	}
	
	@Test
	public void createCommentReport() {
		UUID profileId = UUID.fromString("12345678-e0e6-11e2-a28f-0800200c9a77");
		UUID commentId = UUID.fromString("c95472c0-e0e6-11e2-a28f-0800200c9a33");
		String country = "Switzerland";
		AbuseInfo abuse = service.reportAbusiveComment(commentId, profileId, country, AbuseReason.VIOLENCE);
		assertNotNull(abuse);
		assertEquals(profileId, abuse.getProfileId());
		assertNull(abuse.getMediaUri());
		assertEquals(commentId, abuse.getCommentId());
		assertEquals(AbuseReason.VIOLENCE, abuse.getReason());
		assertEquals(AbuseStatus.NEW, abuse.getStatus());
		assertEquals(country, abuse.getCountry());
		assertEquals("Hello 2", abuse.getMessage());
		assertNull(abuse.getCreator());
		assertNotNull(abuse.getTimestamp());
	}
	
	@Test(expected=DataMissingException.class)
	public void createReportForInvalidComment() {
		UUID profileId = UUID.fromString("12345678-e0e6-11e2-a28f-0800200c9a77");
		UUID commentId = UUID.fromString("c95472c0-e0e6-11e2-a28f-999999999999");
		String country = "Switzerland";
		service.reportAbusiveComment(commentId, profileId, country, AbuseReason.VIOLENCE);
	}
}
