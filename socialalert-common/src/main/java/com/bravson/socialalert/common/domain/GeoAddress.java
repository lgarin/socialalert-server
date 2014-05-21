package com.bravson.socialalert.common.domain;

public class GeoAddress {

	private Double latitude;
	private Double longitude;
	private String formattedAddress;
	private String locality;
	private String country;
	
	public GeoAddress() {
	}

	public GeoAddress(Double latitude, Double longitude, String formattedAddress, String locality, String country) {
		this.latitude = latitude;
		this.longitude = longitude;
		this.formattedAddress = formattedAddress;
		this.locality = locality;
		this.country = country;
	}

	public Double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public Double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public String getFormattedAddress() {
		return formattedAddress;
	}

	public void setFormattedAddress(String formattedAddress) {
		this.formattedAddress = formattedAddress;
	}

	public String getLocality() {
		return locality;
	}

	public void setLocality(String locality) {
		this.locality = locality;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}
}
