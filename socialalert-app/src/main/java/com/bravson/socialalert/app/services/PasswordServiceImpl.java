package com.bravson.socialalert.app.services;

import javax.annotation.Resource;

import org.springframework.security.authentication.dao.SaltSource;
import org.springframework.security.authentication.encoding.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.bravson.socialalert.app.entities.ApplicationUser;

@SuppressWarnings("deprecation")
@Service
public class PasswordServiceImpl implements PasswordService {

	@Resource
	private SaltSource saltSource;
	
	@Resource
	private PasswordEncoder passwordEncoder;

	@Override
	public boolean isPasswordValid(ApplicationUser user, String rawPassword) {
		Object salt = saltSource.getSalt(user);
		return passwordEncoder.isPasswordValid(user.getPassword(), rawPassword, salt);
	}

	@Override
	public String encodePassword(ApplicationUser user, String rawPassword) {
		Object salt = saltSource.getSalt(user);
		return passwordEncoder.encodePassword(rawPassword, salt);
	}
}
