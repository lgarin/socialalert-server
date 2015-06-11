package com.bravson.socialalert.services;

import java.net.URI;
import java.util.Arrays;
import java.util.UUID;

import javax.annotation.Resource;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;

import com.bravson.socialalert.app.entities.AlertComment;
import com.bravson.socialalert.app.exceptions.DataMissingException;
import com.bravson.socialalert.app.services.AlertCommentService;
import com.bravson.socialalert.common.domain.ActivityInfo;
import com.bravson.socialalert.common.domain.ApprovalModifier;
import com.bravson.socialalert.common.domain.CommentInfo;
import com.bravson.socialalert.common.domain.MediaInfo;
import com.bravson.socialalert.common.domain.QueryResult;
import com.bravson.socialalert.infrastructure.DataServiceTest;

public class AlertCommentServiceTest extends DataServiceTest {

	@Resource
	private AlertCommentService service;

	@Before
	public void setUp() throws Exception {
		fullImport(AlertComment.class);
	}
	
	@Test
	public void createNewComment() {
		UUID profileId = UUID.fromString("12345678-e0e6-11e2-a28f-0800200c9a77");
		URI mediaUri = URI.create("20130716/f317f3c7918c83ff6ec24aabb6c017fd.jpg");
		CommentInfo comment = service.addComment(mediaUri, profileId, "Hello");
		assertNotNull(comment);
		assertEquals("Hello", comment.getComment());
		assertEquals(profileId, comment.getProfileId());
		assertEquals(mediaUri, comment.getMediaUri());
		assertNull(comment.getCreator());
		assertNotNull(comment.getCreation());
	}
	
	@Test
	public void searchFirstByMediaUri() {
		URI mediaUri = URI.create("20130712/7e9a5a5bd5e64171c176ac6c7b32d685.jpg");
		QueryResult<CommentInfo> result = service.searchCommentByMediaUri(mediaUri, 0, 1);
		assertNotNull(result);
		assertEquals(2, result.getPageCount());
		assertEquals(0, result.getPageNumber());
		assertNotNull(result.getContent());
		assertEquals(1, result.getContent().size());
		CommentInfo comment = result.getContent().get(0);
		assertNotNull(comment);
		assertEquals("Hello 2", comment.getComment());
	}
	
	@Test
	public void searchSecondByMediaUri() {
		URI mediaUri = URI.create("20130712/7e9a5a5bd5e64171c176ac6c7b32d685.jpg");
		QueryResult<CommentInfo> result = service.searchCommentByMediaUri(mediaUri, 1, 1);
		assertNotNull(result);
		assertEquals(2, result.getPageCount());
		assertEquals(1, result.getPageNumber());
		assertNotNull(result.getContent());
		assertEquals(1, result.getContent().size());
		CommentInfo comment = result.getContent().get(0);
		assertNotNull(comment);
		assertEquals("Hello 1", comment.getComment());
	}
	
	@Test
	public void populateComments() {
		ActivityInfo activity1 = new ActivityInfo();
		activity1.setCommentId(UUID.fromString("b95472c0-e0e6-11e2-a28f-0800200c9a22"));
		ActivityInfo activity2 = new ActivityInfo();
		activity2.setCommentId(UUID.fromString("c95472c0-e0e6-11e2-a28f-0800200c9a33"));
		ActivityInfo activity3 = new ActivityInfo();
		service.populateComments(Arrays.asList(activity1, activity2, activity3));
		assertEquals("Hello 1", activity1.getMessage());
		assertEquals("Hello 2", activity2.getMessage());
		assertNull(activity3.getMessage());
	}
	
	@Test
	public void getExistingComment() {
		CommentInfo comment = service.getCommentInfo(UUID.fromString("b95472c0-e0e6-11e2-a28f-0800200c9a22"));
		assertNotNull(comment);
		assertEquals("Hello 1", comment.getComment());
	}
	
	@Test(expected=DataMissingException.class)
	public void getNonExistingComment() {
		service.getCommentInfo(UUID.fromString("095472c0-e0e6-11e2-a28f-0800200c9a22"));
	}
	
	@Test
	public void revertApproval() {
		CommentInfo info = service.updateApproval(UUID.fromString("b95472c0-e0e6-11e2-a28f-0800200c9a22"), ApprovalModifier.LIKE, null);
		assertNotNull(info);
		assertEquals(0, info.getApprovalCount());
	}
	
	@Test
	public void confirmApproval() {
		CommentInfo info = service.updateApproval(UUID.fromString("b95472c0-e0e6-11e2-a28f-0800200c9a22"), ApprovalModifier.LIKE, ApprovalModifier.LIKE);
		assertNotNull(info);
		assertEquals(1, info.getApprovalCount());
	}
	
	@Test
	public void disapproveApproval() {
		CommentInfo info = service.updateApproval(UUID.fromString("b95472c0-e0e6-11e2-a28f-0800200c9a22"), ApprovalModifier.LIKE, ApprovalModifier.DISLIKE);
		assertNotNull(info);
		assertEquals(-1, info.getApprovalCount());
	}
}
