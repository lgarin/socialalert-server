package com.bravson.socialalert.app.services;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.validation.annotation.Validated;

import com.bravson.socialalert.common.domain.GeoArea;

@Validated
public interface GeocoderService {

	public String queryCountry(@NotEmpty String ipAddress);
	
	public String encodeLatLon(Double latitude, Double longitude, @Min(0) @Max(24) int precision);
	
	public GeoArea decodeGeoHash(@NotEmpty String geoHash);
	
	public int computeGeoHashLength(@NotNull GeoArea area);
}
