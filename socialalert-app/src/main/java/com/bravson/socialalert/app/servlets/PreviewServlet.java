package com.bravson.socialalert.app.servlets;

import java.io.File;
import java.io.IOException;
import java.net.URI;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import com.bravson.socialalert.app.infrastructure.FileHttpRequestHandler;
import com.bravson.socialalert.app.services.MediaStorageService;
import com.bravson.socialalert.common.domain.MediaConstants;

@Component
public class PreviewServlet extends FileHttpRequestHandler implements MediaConstants {

	@Autowired
	private MediaStorageService storageService;
	
	@Override
	protected File getFile(String relativePath) throws IOException {
		return storageService.resolvePreviewUri(URI.create(relativePath));
	}
	
	@Override
	protected MediaType getMediaType(Resource resource) {
		if (resource.getFilename().endsWith(JPG_EXTENSION)) {
			return MediaType.valueOf(JPG_MEDIA_TYPE);
		} else if (resource.getFilename().endsWith(MP4_EXTENSION)) {
			return MediaType.valueOf(MP4_MEDIA_TYPE);
		} else if (resource.getFilename().endsWith(MOV_EXTENSION)) {
			return MediaType.valueOf(MP4_MEDIA_TYPE);
		}
		return null;
	}
}
