package com.bravson.socialalert.app.tasks;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;

import com.bravson.socialalert.app.services.ApplicationUserService;

public class UserBackgroundTaskImpl implements UserBackgroundTask {

	private Logger logger = LoggerFactory.getLogger(getClass());
	
	@Resource
	private ApplicationUserService userService;
	
	@Value("${user.page.size}")
	private int pageSize;

	@Override
	@Scheduled(cron="${user.unlock.cron}")
	public void unlockUsers() {
		while (true) {
			int updateCount = userService.unlockPageOfUsers(pageSize);
			logger.info("Unlocked {} users", updateCount);
			if (updateCount < pageSize) {
				break;
			}
		}
	}
}
