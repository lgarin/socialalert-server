package com.bravson.socialalert.app.services;

import java.net.URI;
import java.util.UUID;

import javax.validation.constraints.NotNull;

import org.springframework.validation.annotation.Validated;

import com.bravson.socialalert.common.domain.AbuseInfo;
import com.bravson.socialalert.common.domain.AbuseReason;

@Validated
public interface AbuseReportService {

	public AbuseInfo reportAbusiveMedia(@NotNull URI mediaUri, @NotNull UUID profileId, @NotNull String country, @NotNull AbuseReason reason);
	
	public AbuseInfo reportAbusiveComment(@NotNull UUID commentId, @NotNull UUID profileId, @NotNull String country, @NotNull AbuseReason reason);
}
