package com.bravson.socialalert.app.services;

import java.net.URI;
import java.util.UUID;

import javax.validation.constraints.NotNull;

import org.springframework.validation.annotation.Validated;

import com.bravson.socialalert.common.domain.ApprovalModifier;

@Validated
public interface AlertInteractionService {

	public ApprovalModifier getApprovalModifier(@NotNull URI mediaUri, @NotNull UUID profileId);

	public ApprovalModifier setApprovalModifier(@NotNull URI mediaUri, @NotNull UUID profileId, ApprovalModifier modifier);
}
