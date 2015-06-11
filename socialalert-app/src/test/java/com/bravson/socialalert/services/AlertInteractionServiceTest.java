package com.bravson.socialalert.services;

import java.net.URI;
import java.util.UUID;

import javax.annotation.Resource;

import org.junit.Before;
import org.junit.Test;

import com.bravson.socialalert.app.entities.AlertComment;
import com.bravson.socialalert.app.entities.AlertInteraction;
import com.bravson.socialalert.app.services.AlertInteractionService;
import com.bravson.socialalert.common.domain.ApprovalModifier;
import com.bravson.socialalert.infrastructure.DataServiceTest;

public class AlertInteractionServiceTest extends DataServiceTest {

	@Resource
	private AlertInteractionService service;

	@Before
	public void setUp() throws Exception {
		fullImport(AlertInteraction.class);
		fullImport(AlertComment.class);
	}
	
	@Test
	public void createNewMediaApproval() {
		UUID profileId = UUID.fromString("12345678-e0e6-11e2-a28f-0800200c9a77");
		URI mediaUri = URI.create("20130716/f317f3c7918c83ff6ec24aabb6c017fd.jpg");
		ApprovalModifier oldApproval = service.setMediaApprovalModifier(mediaUri, profileId, ApprovalModifier.LIKE);
		assertNull(oldApproval);
		ApprovalModifier approval = service.getMediaApprovalModifier(mediaUri, profileId);
		assertEquals(ApprovalModifier.LIKE, approval);
	}
	
	@Test
	public void resetExistingMediaApproval() {
		UUID profileId = UUID.fromString("e7d166ae-9b3f-4405-be0d-fa1567728593");
		URI mediaUri = URI.create("20130716/f317f3c7918c83ff6ec24aabb6c017fd.jpg");
		ApprovalModifier oldApproval = service.setMediaApprovalModifier(mediaUri, profileId, null);
		assertEquals(ApprovalModifier.LIKE, oldApproval);
		ApprovalModifier newApproval = service.getMediaApprovalModifier(mediaUri, profileId);
		assertNull(newApproval);
	}
	
	@Test
	public void changeExistingMediaApproval() {
		UUID profileId = UUID.fromString("e7d166ae-9b3f-4405-be0d-fa1567728593");
		URI mediaUri = URI.create("20130716/f317f3c7918c83ff6ec24aabb6c017fd.jpg");
		ApprovalModifier oldApproval = service.setMediaApprovalModifier(mediaUri, profileId, ApprovalModifier.DISLIKE);
		assertEquals(ApprovalModifier.LIKE, oldApproval);
		ApprovalModifier newApproval = service.getMediaApprovalModifier(mediaUri, profileId);
		assertEquals(ApprovalModifier.DISLIKE, newApproval);
	}
	
	@Test
	public void initMediaApproval() {
		UUID profileId = UUID.fromString("a95472c0-e0e6-11e2-a28f-0800200c9a77");
		URI mediaUri = URI.create("20130712/7e9a5a5bd5e64171c176ac6c7b32d685.jpg");
		ApprovalModifier oldApproval = service.setMediaApprovalModifier(mediaUri, profileId, ApprovalModifier.DISLIKE);
		assertNull(oldApproval);
		ApprovalModifier newApproval = service.getMediaApprovalModifier(mediaUri, profileId);
		assertEquals(ApprovalModifier.DISLIKE, newApproval);
	}
	
	@Test
	public void changeExistingCommentApproval() {
		UUID profileId = UUID.fromString("e7d166ae-9b3f-4405-be0d-fa1567728593");
		UUID commentId = UUID.fromString("b95472c0-e0e6-11e2-a28f-0800200c9a22");
		ApprovalModifier oldApproval = service.setCommentApprovalModifier(commentId, profileId, ApprovalModifier.DISLIKE);
		assertEquals(ApprovalModifier.LIKE, oldApproval);
		ApprovalModifier newApproval = service.getCommentApprovalModifier(commentId, profileId);
		assertEquals(ApprovalModifier.DISLIKE, newApproval);
	}
	
	@Test
	public void initCommentApproval() {
		UUID profileId = UUID.fromString("a95472c0-e0e6-11e2-a28f-0800200c9a77");
		UUID commentId = UUID.fromString("c95472c0-e0e6-11e2-a28f-0800200c9a33");
		ApprovalModifier oldApproval = service.setCommentApprovalModifier(commentId, profileId, ApprovalModifier.DISLIKE);
		assertNull(oldApproval);
		ApprovalModifier newApproval = service.getCommentApprovalModifier(commentId, profileId);
		assertEquals(ApprovalModifier.DISLIKE, newApproval);
	}
}
