package com.bravson.socialalert.common.domain;

import java.util.EnumSet;
import java.util.UUID;

import org.joda.time.DateTime;



public class UserInfo {

    private String email;

    private String nickname;
    
    private EnumSet<UserRole> roles;
    
    private UserState state;
   
    private DateTime creation;
    
    private DateTime lastUpdate;
    
    private DateTime lastLoginSuccess;
    
    private DateTime lastLoginFailure;
    
    private int loginFailureCount;
    
    private UUID profileId;

    public UserInfo() {
    	
    }

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public EnumSet<UserRole> getRoles() {
		return roles;
	}
	
	public boolean hasRole(UserRole role) {
		return roles.contains(role);
	}

	public void setRoles(EnumSet<UserRole> roles) {
		this.roles = roles;
	}

	public UserState getState() {
		return state;
	}

	public void setState(UserState state) {
		this.state = state;
	}

	public DateTime getCreation() {
		return creation;
	}

	public void setCreation(DateTime creation) {
		this.creation = creation;
	}

	public DateTime getLastUpdate() {
		return lastUpdate;
	}

	public void setLastUpdate(DateTime lastUpdate) {
		this.lastUpdate = lastUpdate;
	}

	public DateTime getLastLoginSuccess() {
		return lastLoginSuccess;
	}

	public void setLastLoginSuccess(DateTime lastLoginSuccess) {
		this.lastLoginSuccess = lastLoginSuccess;
	}

	public DateTime getLastLoginFailure() {
		return lastLoginFailure;
	}

	public void setLastLoginFailure(DateTime lastLoginFailure) {
		this.lastLoginFailure = lastLoginFailure;
	}

	public int getLoginFailureCount() {
		return loginFailureCount;
	}

	public void setLoginFailureCount(int loginFailureCount) {
		this.loginFailureCount = loginFailureCount;
	}

	public UUID getProfileId() {
		return profileId;
	}

	public void setProfileId(UUID profileId) {
		this.profileId = profileId;
	}
}
