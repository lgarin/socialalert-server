package com.bravson.socialalert.app.services;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.bravson.socialalert.common.domain.GeoArea;
import com.spatial4j.core.context.SpatialContext;
import com.spatial4j.core.distance.DistanceUtils;
import com.spatial4j.core.io.GeohashUtils;
import com.spatial4j.core.shape.Point;
import com.spatial4j.core.shape.Rectangle;

@Service
public class GeocoderServiceImpl implements GeocoderService {

	@Resource
	private RestTemplate restTemplate;
	
	public String queryCountry(String ipAddress) {
		return restTemplate.getForObject("http://ipinfo.io/" + ipAddress + "/country", String.class).trim();
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
		double degree = (rect.getHeight() + rect.getWidth()) / 4.0;
		double radius = DistanceUtils.degrees2Dist(degree, DistanceUtils.EARTH_MEAN_RADIUS_KM);
		//double[] degrees = GeohashUtils.lookupDegreesSizeForHashLen(geoHash.length());
		//double degree = Math.max(degrees[0], degrees[1]);
		//double radius = DistanceUtils.toRadians(degree) * DistanceUtils.EARTH_MEAN_RADIUS_KM / 2D;
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
