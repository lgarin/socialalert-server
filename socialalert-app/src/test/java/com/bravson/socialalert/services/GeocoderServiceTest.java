package com.bravson.socialalert.services;

import java.util.List;

import javax.annotation.Resource;

import org.junit.Test;

import com.bravson.socialalert.app.entities.AlertMedia;
import com.bravson.socialalert.app.services.GeocoderService;
import com.bravson.socialalert.common.domain.GeoAddress;
import com.bravson.socialalert.common.domain.GeoArea;
import com.bravson.socialalert.infrastructure.SimpleServiceTest;

public class GeocoderServiceTest extends SimpleServiceTest {

	@Resource
	private GeocoderService service;

	@Test
	public void lookupHomeAddress() {
		List<GeoAddress> result = service.findLocation("Terrassenrain 6, 3072 Ostermundigen", "CH", "FR");
		assertNotNull(result);
		assertEquals(1, result.size());
		assertEquals("Terrassenrain 6, 3072 Ostermundigen, Suisse", result.get(0).getFormattedAddress());
		assertEquals(7.50, result.get(0).getLongitude(), 0.01);
		assertEquals(46.95, result.get(0).getLatitude(), 0.01);
		assertEquals("Ostermundigen", result.get(0).getLocality());
		assertEquals("Suisse", result.get(0).getCountry());
	}
	
	@Test
	public void lookupHomeAddressWithDefaultLanguage() {
		List<GeoAddress> result = service.findLocation("Terrassenrain 6, 3072 Ostermundigen", "CH", null);
		assertNotNull(result);
		assertEquals(1, result.size());
		assertEquals("Terrassenrain 6, 3072 Ostermundigen, Switzerland", result.get(0).getFormattedAddress());
		assertEquals(7.50, result.get(0).getLongitude(), 0.01);
		assertEquals(46.95, result.get(0).getLatitude(), 0.01);
		assertEquals("Ostermundigen", result.get(0).getLocality());
		assertEquals("Switzerland", result.get(0).getCountry());
	}
	
	@Test
	public void lookupInvalidAddress() {
		List<GeoAddress> result = service.findLocation("fdsfs 12d, 9999 fsfsd", "CH", "FR");
		assertNotNull(result);
		assertEquals(0, result.size());
	}
	
	@Test
	public void lookupInexactAddress() {
		List<GeoAddress> result = service.findLocation("Monbijoux, Bern", null, "FR");
		assertNotNull(result);
		assertEquals(3, result.size());
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
