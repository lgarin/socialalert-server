package com.bravson.socialalert.app.services;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.bravson.socialalert.app.domain.DeleteMediaTaskPayload;
import com.bravson.socialalert.app.infrastructure.BackgroundTask;

@Service
public class DeleteMediaTask implements BackgroundTask<DeleteMediaTaskPayload> {

	@Resource
	private MediaStorageService storageService;
	
	@Override
	public void execute(DeleteMediaTaskPayload payload) {
		storageService.deleteMedia(payload.mediaUri);
	}
}
