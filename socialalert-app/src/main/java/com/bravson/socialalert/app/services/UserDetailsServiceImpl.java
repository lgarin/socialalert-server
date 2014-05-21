package com.bravson.socialalert.app.services;

import javax.annotation.Resource;

import org.springframework.context.ApplicationContext;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.bravson.socialalert.app.entities.ApplicationUser;
import com.bravson.socialalert.app.repositories.ApplicationUserRepository;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

	@Resource
	private ApplicationContext context;

	// lazy init because spring security is intialized before spring data
	private ApplicationUserRepository repository;

	private ApplicationUserRepository getUserRepository() {
		if (repository == null) {
			repository = context.getBean(ApplicationUserRepository.class);
		}
		return repository;
	}

	@Override
	public UserDetails loadUserByUsername(String username)
			throws UsernameNotFoundException {
		ApplicationUser user = getUserRepository().findById(username);
		if (user == null) {
			throw new UsernameNotFoundException("Cannot find user " + username);
		}
		return user;
	}
}
