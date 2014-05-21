package com.bravson.socialalert.app.utilities;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.bravson.socialalert.app.entities.ApplicationUser;

public class SecurityUtils {

	public static ApplicationUser findPrincipal(Authentication auth) {
		if (auth != null && auth.getPrincipal() instanceof ApplicationUser) {
			return (ApplicationUser) auth.getPrincipal();
		}
		return null;
	}

	public static ApplicationUser findAuthenticatedPrincipal() {
		return findPrincipal(SecurityContextHolder.getContext().getAuthentication());
	}

}
