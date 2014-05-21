package com.bravson.socialalert.common.domain;

public class ActivityCount {

	private ActivityType type;
	
	private long count;

	public ActivityCount() {
	}
	
	public ActivityCount(ActivityType type, long count) {
		this.type = type;
		this.count = count;
	}

	public ActivityType getType() {
		return type;
	}

	public void setType(ActivityType type) {
		this.type = type;
	}

	public long getCount() {
		return count;
	}

	public void setCount(long count) {
		this.count = count;
	}
}
