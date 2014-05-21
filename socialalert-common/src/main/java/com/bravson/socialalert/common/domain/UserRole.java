package com.bravson.socialalert.common.domain;

import java.util.Collections;
import java.util.EnumSet;

import org.apache.commons.lang3.StringUtils;


public enum UserRole {

	USER, ADMINISTRATOR, GUEST, THIRD_PARTY, SYSTEM, BACK_OFFICE, ANONYMOUS;
	
	public static EnumSet<UserRole> parseRoles(String roleList) {
		EnumSet<UserRole> result = EnumSet.noneOf(UserRole.class);
		if (roleList == null) {
			return result;
		}
		for (String token : StringUtils.splitByWholeSeparator(roleList, ", ")) {
			result.add(UserRole.valueOf(token));
		}
		return result;
 	}
	
	public static EnumSet<UserRole> toEnumSet(UserRole... roles) {
		EnumSet<UserRole> result = EnumSet.noneOf(UserRole.class);
		for (UserRole role : roles) {
			result.add(role);
		}
		return result;
	}

	public static boolean checkPermission(UserInfo userInfo, UserRole[] allow, UserRole[] disallow) {
		return checkPermission(userInfo, toEnumSet(allow), toEnumSet(disallow));
	}
	public static boolean checkPermission(UserInfo userInfo, EnumSet<UserRole> allow, EnumSet<UserRole> disallow) {
		EnumSet<UserRole> grants = userInfo != null ? userInfo.getRoles() : EnumSet.of(ANONYMOUS);
		return (disallow.isEmpty() || Collections.disjoint(disallow, grants)) && (allow.isEmpty() || !Collections.disjoint(allow, grants));
    }
}
