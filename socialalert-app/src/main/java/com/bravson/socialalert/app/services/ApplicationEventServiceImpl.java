package com.bravson.socialalert.app.services;

import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bravson.socialalert.app.entities.ApplicationEvent;
import com.bravson.socialalert.app.entities.UserProfile;
import com.bravson.socialalert.app.repositories.ApplicationEventRepository;

@Service
public class ApplicationEventServiceImpl implements ApplicationEventService {

	@Autowired
	private UserProfileService profileService;
	
	@Autowired
	private ApplicationEventRepository eventRepository;
	
	@Autowired(required=false)
	private HttpServletRequest httpRequest;
	
	@Override
	public ApplicationEvent createEvent(UUID profileId, String action, String parameter) {
		UserProfile userProfile = profileService.getProfileById(profileId);
		String ipAddress = httpRequest != null ? httpRequest.getRemoteAddr() : null;
		ApplicationEvent event = new ApplicationEvent(userProfile, ipAddress, action, parameter);
		return eventRepository.save(event);
	}
}
