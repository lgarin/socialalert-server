package com.bravson.socialalert.app.entities;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import org.apache.solr.client.solrj.beans.Field;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.springframework.data.annotation.Id;
import org.springframework.data.solr.core.mapping.SolrDocument;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.bravson.socialalert.app.infrastructure.VersionedEntity;
import com.bravson.socialalert.common.domain.UserConstants;
import com.bravson.socialalert.common.domain.UserInfo;
import com.bravson.socialalert.common.domain.UserRole;
import com.bravson.socialalert.common.domain.UserState;

@SolrDocument(solrCoreName="ApplicationUser")
public class ApplicationUser extends VersionedEntity implements UserDetails {

	private static final long serialVersionUID = 1L;

	@Id
    @Field
    private String username;
	
	@Field
	private String nickname;
    
	@Field
    private String password;
    
	@Field
    private Set<UserRole> roles;
    
	@Field
    private UserState state;
    
	@Field
	@DateTimeFormat
    private DateTime lastLoginSuccess;
    
	@Field
	@DateTimeFormat
    private DateTime lastLoginFailure;
    
	@Field
    private int loginFailureCount;
	
	@Field
	private UUID profileId;
    
    protected ApplicationUser() {
    	
    }
    
    public ApplicationUser(String username, String nickname, String password) {
		this.username = username;
		this.nickname = nickname;
		this.password = password;
		this.roles = EnumSet.of(UserRole.GUEST);
		this.state = UserState.UNVERIFIED;
	}

	public String getUsername() {
		return username;
	}
	
	public String getNickname() {
		return nickname;
	}
	
	@Override
	public String getPassword() {
		return password;
	}
	
	public boolean hasRole(UserRole role) {
		return roles.contains(role);
	}
	
	public UserState getState() {
		return state;
	}
	
	public DateTime getCreation() {
		return creation;
	}
	
	public DateTime getLastUpdate() {
		return lastUpdate;
	}

	public DateTime getLastLoginSuccess() {
		return lastLoginSuccess;
	}

	public DateTime getLastLoginFailure() {
		return lastLoginFailure;
	}

	public int getLoginFailureCount() {
		return loginFailureCount;
	}
	
	public UUID getProfileId() {
		return profileId;
	}

	private static GrantedAuthority toAuthority(UserRole role) {
		return new SimpleGrantedAuthority("ROLE_" + role.name());
	}
	
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		ArrayList<GrantedAuthority> result = new ArrayList<>(roles.size());
		for (UserRole role : roles) {
			result.add(toAuthority(role));
		}
		return result;
	}
	
	@Override
	public boolean isAccountNonExpired() {
		// TODO lastLogin older than 1 year
		return true;
	}
	
	@Override
	public boolean isAccountNonLocked() {
		return state != UserState.LOCKED;
	}
	
	@Override
	public boolean isCredentialsNonExpired() {
		return state != UserState.PASSWORD_EXPIRED;
	}
	
	@Override
	public boolean isEnabled() {
		// TODO
		return true;
	}

	public UserInfo toUserInfo() {
		UserInfo info = new UserInfo();
		info.setNickname(nickname);
		info.setEmail(username);
		info.setCreation(creation);
		info.setLastLoginSuccess(lastLoginSuccess);
		info.setLastLoginFailure(lastLoginFailure);
		info.setLoginFailureCount(loginFailureCount);
		info.setLastUpdate(lastUpdate);
		info.setRoles(EnumSet.copyOf(roles));
		info.setState(state);
		info.setProfileId(profileId);
		return info;
	}

	public void updateLastLoginSuccess() {
		lastLoginSuccess = DateTime.now(DateTimeZone.UTC);
		touch();
	}
	
	public void updateLastLoginFailure() {
		lastLoginFailure = DateTime.now(DateTimeZone.UTC);
		touch();
		loginFailureCount++;
		if (loginFailureCount >= UserConstants.MAX_LOGIN_RETRY) {
			state = UserState.LOCKED;
		}
	}
	
	public void clearLoginFailures() {
		lastLoginFailure = null;
		loginFailureCount = 0;
		touch();
	}
	
	public boolean hasLoginFailure() {
		return loginFailureCount > 0;
	}

	public void changePassword(String newPassword) {
		touch();
		this.password = newPassword;
		if (state == UserState.LOCKED) {
			state = roles.contains(UserRole.GUEST) ? UserState.UNVERIFIED : UserState.ACTIVE;
		}
	}

	public void activate(UserProfile profile) {
		profileId = profile.getId();
		touch();
		if (state == UserState.UNVERIFIED) {
			state = UserState.ACTIVE;
		}
		if (roles.contains(UserRole.GUEST)) {
			roles.remove(UserRole.GUEST);
			roles.add(UserRole.USER);
		}
	}
	
	public void unlock() {
		touch();
		if (state == UserState.LOCKED) {
			state = roles.contains(UserRole.GUEST) ? UserState.UNVERIFIED : UserState.ACTIVE;
		}
	}

	public boolean isUnsafePassword(String newPassword) {
		return Objects.equals(username, newPassword) || Objects.equals(nickname, newPassword);
	}
}
