package com.bravson.socialalert.app.services;

import java.net.URI;

import javax.validation.constraints.NotNull;

import org.springframework.validation.annotation.Validated;

@Validated
public interface UserSessionService {

	public boolean addViewedUri(@NotNull URI uri);

	void clearAll();
}
