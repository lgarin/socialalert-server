package com.bravson.socialalert.app.services;

import java.net.URL;

import javax.validation.constraints.NotNull;

import org.springframework.validation.annotation.Validated;

import com.bravson.socialalert.app.domain.ExternalProfileInfo;

@Validated
public interface OAuthAuthenicationService {

	public URL beginOAuthConsumption(@NotNull String providerId, @NotNull URL successUrl);
	public ExternalProfileInfo endOAuthConsumption(@NotNull URL receivingUrl);
}
