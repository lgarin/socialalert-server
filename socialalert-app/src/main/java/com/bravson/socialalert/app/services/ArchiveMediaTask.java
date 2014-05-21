package com.bravson.socialalert.app.services;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.bravson.socialalert.app.domain.ArchiveMediaTaskPayload;
import com.bravson.socialalert.app.infrastructure.BackgroundTask;

@Service
public class ArchiveMediaTask implements BackgroundTask<ArchiveMediaTaskPayload> {

	@Resource
	private MediaStorageService storageService;
	
	@Override
	public void execute(ArchiveMediaTaskPayload payload) {
		storageService.archiveMedia(payload.tempUri, payload.finalUri);
	}
}
