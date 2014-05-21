package com.bravson.socialalert.app.infrastructure;

import java.io.File;
import java.io.IOException;
import java.util.Collections;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.resource.ResourceHttpRequestHandler;

public abstract class FileHttpRequestHandler extends ResourceHttpRequestHandler {

	@Override
	public void afterPropertiesSet() throws Exception {
		setLocations(Collections.<Resource>singletonList(null));
		super.afterPropertiesSet();
	}

	@Override
	protected Resource getResource(HttpServletRequest request) {
		String relativePath = StringUtils.substringAfterLast(request.getRequestURI(), request.getServletPath());
		try {
			File file = getFile(relativePath);
			return file != null ? new FileSystemResource(file) : null;
		} catch (IOException e) {
			return null;
		}
	}
	
	protected abstract File getFile(String relativePath) throws IOException;
}
