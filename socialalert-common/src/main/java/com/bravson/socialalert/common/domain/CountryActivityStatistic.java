package com.bravson.socialalert.common.domain;

public class CountryActivityStatistic {

	private String country;
	
	private long count;

	public CountryActivityStatistic() {
	}
	
	public CountryActivityStatistic(String country, long count) {
		this.country = country;
		this.count = count;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public long getCount() {
		return count;
	}

	public void setCount(long count) {
		this.count = count;
	}
	
}
