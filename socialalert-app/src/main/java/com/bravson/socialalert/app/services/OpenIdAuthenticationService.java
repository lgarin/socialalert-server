package com.bravson.socialalert.app.services;

import java.net.URL;

import javax.validation.constraints.NotNull;

import org.springframework.validation.annotation.Validated;

import com.bravson.socialalert.app.domain.ExternalProfileInfo;

@Validated
public interface OpenIdAuthenticationService {

	public URL beginOpenIdConsumption(@NotNull URL identityUrl, @NotNull URL returnToUrl);
	public ExternalProfileInfo endOpenIdConsumption(@NotNull URL receivingUrl);
}
