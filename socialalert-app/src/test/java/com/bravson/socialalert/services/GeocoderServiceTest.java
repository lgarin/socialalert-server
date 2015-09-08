package com.bravson.socialalert.services;

import javax.annotation.Resource;

import org.junit.Test;
import org.springframework.web.client.HttpClientErrorException;

import com.bravson.socialalert.app.entities.AlertMedia;
import com.bravson.socialalert.app.services.GeocoderService;
import com.bravson.socialalert.common.domain.GeoArea;
import com.bravson.socialalert.infrastructure.SimpleServiceTest;

public class GeocoderServiceTest extends SimpleServiceTest {

	@Resource
	private GeocoderService service;

	@Test
	public void queryKnownIpAddressCountry() {
		String result = service.queryCountry("8.8.8.8");
		assertEquals("US", result);
	}
	
	@Test
	public void queryLocalIpAddressCountry() {
		String result = service.queryCountry("192.168.120.85");
		assertEquals("undefined", result);
	}
	
	@Test(expected=HttpClientErrorException.class)
	public void queryInvalidIpAddressCountry() {
		service.queryCountry("a.b.c");
	}
	
	@Test
	public void computeGeohash() {
		String geohash0 = service.encodeLatLon(46.95, 7.50, 0);
		assertEquals("", geohash0);
		String geohash1 = service.encodeLatLon(46.95, 7.50, 1);
		assertEquals("u", geohash1);
		String geohash2 = service.encodeLatLon(46.95, 7.50, 2);
		assertEquals("u0", geohash2);
		String geohash3 = service.encodeLatLon(46.95, 7.50, 3);
		assertEquals("u0m", geohash3);
		String geohash4 = service.encodeLatLon(46.95, 7.50, 4);
		assertEquals("u0m7", geohash4);
	}
	
	@Test
	public void computeFullGeohash() {
		String geohash = service.encodeLatLon(46.68666666666667, 7.858833333333333, AlertMedia.GEOHASH_PRECISION);
		assertEquals("u0m9dg4", geohash);
	}
	
	@Test
	public void decodeGeohash() {
		/*
		String geohash = service.encodeLatLon(46.95, 7.50, 8);
		double lastRad = 0.0;
		for (int i = 1; i < 8; i++) {
			GeoArea area = service.decodeGeoHash(StringUtils.substring(geohash, 0, i));
			if (area.getRadius() != lastRad) {
				System.out.println("" + area.getRadius() + " -> " + i);
				lastRad = area.getRadius();
			}
		}
		*/
		GeoArea area = service.decodeGeoHash("u0m7");
		assertEquals(46.95, area.getLatitude(), 0.1);
		assertEquals(7.5, area.getLongitude(), 0.1);
		assertEquals(14.7, area.getRadius(), 0.5);
	}
	
	@Test
	public void computeGeohashLength() {
		/*
		int lastLen = 0;
		for (double r = 0.1; r < 5000.0; r += 0.1) {
			GeoArea area = new GeoArea(46.95, 7.5, r);
			int length = service.computeGeoHashLength(area);
			if (lastLen != length) {
				System.out.println("" + r + " -> " + length);
				lastLen = length;
			}
		}
		*/
		GeoArea area = new GeoArea(46.95, 7.5, 23.0);
		int length = service.computeGeoHashLength(area);
		assertEquals(5, length);
	}
}
