package com.bravson.socialalert.app.servlets;

import java.io.File;
import java.net.URI;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import com.bravson.socialalert.app.infrastructure.FileHttpRequestHandler;
import com.bravson.socialalert.app.services.MediaStorageService;

@Component
public class MediaServlet extends FileHttpRequestHandler {

	@Autowired
	private MediaStorageService storageService;
	
	@Override
	protected File getFile(String relativePath) {
		URI uri = URI.create(relativePath);
		// TODO authorization
		return storageService.resolveMediaUri(uri);
	}
	
	@Override
	protected MediaType getMediaType(Resource resource) {
		//TODO video?
		return MediaType.IMAGE_JPEG;
	}
}
