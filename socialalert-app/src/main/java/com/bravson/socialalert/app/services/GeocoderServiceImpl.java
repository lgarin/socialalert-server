package com.bravson.socialalert.app.services;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Service;

import com.bravson.socialalert.common.domain.GeoAddress;
import com.bravson.socialalert.common.domain.GeoArea;
import com.google.code.geocoder.Geocoder;
import com.google.code.geocoder.model.GeocodeResponse;
import com.google.code.geocoder.model.GeocoderAddressComponent;
import com.google.code.geocoder.model.GeocoderRequest;
import com.google.code.geocoder.model.GeocoderResult;
import com.google.code.geocoder.model.LatLng;
import com.spatial4j.core.context.SpatialContext;
import com.spatial4j.core.distance.DistanceUtils;
import com.spatial4j.core.io.GeohashUtils;
import com.spatial4j.core.shape.Point;
import com.spatial4j.core.shape.Rectangle;

@Service
public class GeocoderServiceImpl implements GeocoderService {

	private Geocoder geocoder = new Geocoder();
	
	public List<GeoAddress> findLocation(String address, String region, String preferredLanguage) {
		GeocoderRequest request = new GeocoderRequest(address, preferredLanguage);
		request.setRegion(region);
		GeocodeResponse response = geocoder.geocode(request);
		switch (response.getStatus()) {
		case ZERO_RESULTS:
			return Collections.emptyList();
		case OK:
			return toGeoAddresses(response.getResults());
		default:
			throw new RuntimeException("Status : " + response.getStatus().name());
		}
	}


	private List<GeoAddress> toGeoAddresses(List<GeocoderResult> addresses) {
		ArrayList<GeoAddress> result = new ArrayList<>(addresses.size());
		for (GeocoderResult address : addresses) {
			LatLng location = address.getGeometry().getLocation();
			String country = findAddressComponent(address.getAddressComponents(), Arrays.asList("country", "political"));
			String locality = findAddressComponent(address.getAddressComponents(), Arrays.asList("locality", "political"));
			result.add(new GeoAddress(location.getLat().doubleValue(), location.getLng().doubleValue(), address.getFormattedAddress(), locality, country));
		}
		return result;
	}

	private String findAddressComponent(List<GeocoderAddressComponent> addressComponents, List<String> types) {
		for (GeocoderAddressComponent component : addressComponents) {
			if (component.getTypes().containsAll(types)) {
				return component.getLongName();
			}
		}
		return null;
	}


	public String encodeLatLon(Double latitude, Double longitude, int precision) {
		if (latitude == null || longitude == null) {
			return null;
		}
		return GeohashUtils.encodeLatLon(latitude, longitude, precision);
	}
	
	@Override
	public GeoArea decodeGeoHash(String geoHash) {
		Rectangle rect = GeohashUtils.decodeBoundary(geoHash, SpatialContext.GEO);
		Point center = rect.getCenter();
		double[] degrees = GeohashUtils.lookupDegreesSizeForHashLen(geoHash.length());
		double degree = Math.max(degrees[0], degrees[1]);
		double radius = DistanceUtils.toRadians(degree) * DistanceUtils.EARTH_MEAN_RADIUS_KM / 2D;
		//double radius = DistanceUtils.degrees2Dist(Math.sqrt(rect.getArea(SpatialContext.GEO)), DistanceUtils.EARTH_MEAN_RADIUS_KM);
		return new GeoArea(center.getY(), center.getX(), radius);
	}
	
	public int computeGeoHashLength(GeoArea area) {
		double degree = DistanceUtils.dist2Degrees(area.getRadius(), DistanceUtils.EARTH_MEAN_RADIUS_KM);
		//Rectangle rect = SpatialContext.GEO.makeCircle(area.getLongitude(), area.getLatitude(), degree).getBoundingBox();
		//return GeohashUtils.lookupHashLenForWidthHeight(rect.getWidth(), rect.getHeight());
		return GeohashUtils.lookupHashLenForWidthHeight(degree, degree);
	}
}
