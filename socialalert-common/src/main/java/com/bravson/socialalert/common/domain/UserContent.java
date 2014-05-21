package com.bravson.socialalert.common.domain;

import java.util.UUID;

public interface UserContent {

	public UUID getProfileId();
	
	public String getCreator();

	public void setCreator(String creator);
	
	public boolean isOnline();
	
	public void setOnline(boolean online);
}
