package com.bravson.socialalert.app.services;

import java.net.URI;
import java.util.UUID;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bravson.socialalert.app.entities.AlertInteraction;
import com.bravson.socialalert.app.repositories.AlertInteractionRepository;
import com.bravson.socialalert.common.domain.ApprovalModifier;

@Service
public class AlertInteractionServiceImpl implements AlertInteractionService {

	@Resource
	private AlertInteractionRepository interactionRepository;
	
	@Override
	@Transactional(rollbackFor={Throwable.class})
	public ApprovalModifier setMediaApprovalModifier(URI mediaUri, UUID profileId, ApprovalModifier modifier) {
		AlertInteraction interaction = interactionRepository.lockById(AlertInteraction.buildInteractionId(mediaUri, profileId));
		if (interaction == null && modifier == null) {
			return null;
		}
		if (interaction == null) {
			interaction = new AlertInteraction(mediaUri, profileId);
		}
		ApprovalModifier oldApproval = interaction.getApproval();
		interaction.setApproval(modifier);
		interactionRepository.save(interaction);
		return oldApproval;
	}

	@Override
	public ApprovalModifier getMediaApprovalModifier(URI mediaUri, UUID profileId) {
		AlertInteraction interaction = interactionRepository.findById(AlertInteraction.buildInteractionId(mediaUri, profileId));
		if (interaction == null) {
			return null;
		}
		return interaction.getApproval();
	}
	
	@Override
	@Transactional(rollbackFor={Throwable.class})
	public ApprovalModifier setCommentApprovalModifier(UUID commentId, UUID profileId, ApprovalModifier modifier) {
		AlertInteraction interaction = interactionRepository.lockById(AlertInteraction.buildInteractionId(commentId, profileId));
		if (interaction == null && modifier == null) {
			return null;
		}
		if (interaction == null) {
			interaction = new AlertInteraction(commentId, profileId);
		}
		ApprovalModifier oldApproval = interaction.getApproval();
		interaction.setApproval(modifier);
		interactionRepository.save(interaction);
		return oldApproval;
	}

	@Override
	public ApprovalModifier getCommentApprovalModifier(UUID commentId, UUID profileId) {
		AlertInteraction interaction = interactionRepository.findById(AlertInteraction.buildInteractionId(commentId, profileId));
		if (interaction == null) {
			return null;
		}
		return interaction.getApproval();
	}
}
