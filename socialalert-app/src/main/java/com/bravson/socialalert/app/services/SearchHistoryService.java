package com.bravson.socialalert.app.services;

import java.util.UUID;

import javax.validation.constraints.NotNull;

import org.springframework.validation.annotation.Validated;

import com.bravson.socialalert.common.domain.GeoArea;

@Validated
public interface SearchHistoryService {

	public void addSearch(@NotNull UUID profileId, String keywords, GeoArea area);
}
