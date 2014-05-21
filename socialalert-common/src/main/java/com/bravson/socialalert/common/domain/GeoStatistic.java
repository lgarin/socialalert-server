package com.bravson.socialalert.common.domain;

public class GeoStatistic extends GeoArea {

	private long count;
	
	public GeoStatistic() {
	}

	public GeoStatistic(double latitude, double longitude, double radius, long count) {
		super(latitude, longitude, radius);
		this.count = count;
	}

	public long getCount() {
		return count;
	}

	public void setCount(long count) {
		this.count = count;
	}
}
