package com.bravson.socialalert.app.services;

import java.net.URI;
import java.util.UUID;

import javax.validation.constraints.NotNull;

import org.springframework.validation.annotation.Validated;

@Validated
public interface UserSessionService {

	boolean addViewedUri(@NotNull URI uri);
	
	boolean addRepostedUri(@NotNull URI uri);
	
	boolean addRepostedComment(@NotNull UUID commentId);

	void clearAll();
}
