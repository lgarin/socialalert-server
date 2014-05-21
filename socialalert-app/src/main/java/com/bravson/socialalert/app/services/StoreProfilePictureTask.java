package com.bravson.socialalert.app.services;

import java.io.IOException;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.bravson.socialalert.app.domain.StoreProfilePictureTaskPayload;
import com.bravson.socialalert.app.infrastructure.BackgroundTask;
import com.bravson.socialalert.app.tasks.QueuedTaskScheduler;

@Service
public class StoreProfilePictureTask implements BackgroundTask<StoreProfilePictureTaskPayload> {

	private Logger logger = Logger.getLogger(getClass());
	
	@Resource
	private UserProfileService profileService;
	
	@Resource
	private QueuedTaskScheduler taskScheduler;
	
	@Value("${profile.picture.retryInterval}")
	private long retryInterval;
	
	public void execute(StoreProfilePictureTaskPayload payload) {
		try {
			profileService.downloadProfilePicture(payload.profileId, payload.image);
		} catch (IOException e) {
			StoreProfilePictureTaskPayload retryPayload = payload.createRetry(retryInterval);
			if (retryPayload != null) {
				taskScheduler.scheduleTask(retryPayload);
			} else {
				logger.info("Failed profile picture download", e);
			}
		}
	}
}
