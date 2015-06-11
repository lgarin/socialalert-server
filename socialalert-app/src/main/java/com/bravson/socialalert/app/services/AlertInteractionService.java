package com.bravson.socialalert.app.services;

import java.net.URI;
import java.util.UUID;

import javax.validation.constraints.NotNull;

import org.springframework.validation.annotation.Validated;

import com.bravson.socialalert.common.domain.ApprovalModifier;

@Validated
public interface AlertInteractionService {

	public ApprovalModifier getMediaApprovalModifier(@NotNull URI mediaUri, @NotNull UUID profileId);

	public ApprovalModifier setMediaApprovalModifier(@NotNull URI mediaUri, @NotNull UUID profileId, ApprovalModifier modifier);
	
	public ApprovalModifier getCommentApprovalModifier(@NotNull UUID commentId, @NotNull UUID profileId);

	public ApprovalModifier setCommentApprovalModifier(@NotNull UUID commentId, @NotNull UUID profileId, ApprovalModifier modifier);
}
