package com.bravson.socialalert.app.servlets;

import java.io.IOException;
import java.net.URI;
import java.util.Collections;

import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.HttpRequestHandler;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpStatusCodeException;

import com.bravson.socialalert.app.services.MediaStorageService;

@Component
public class UploadServlet implements HttpRequestHandler {
	
	//private static final String MP4_MEDIA_TYPE = "video/mp4";
	private static final String JPG_MEDIA_TYPE = MediaType.IMAGE_JPEG_VALUE;
	
	@Resource
	private MediaStorageService storageService;
	
	@Override
	public void handleRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if (!request.getMethod().equalsIgnoreCase(RequestMethod.POST.name())) {
			throw new HttpRequestMethodNotSupportedException(request.getMethod(), Collections.singleton(RequestMethod.POST.name()));
		}
		
		try {
			if (JPG_MEDIA_TYPE.equals(request.getContentType())) {
				URI uri = storageService.storePicture(request.getInputStream(), request.getContentLength());
				request.getInputStream().close();
				// TODO absolute path?
				response.setHeader("Location", uri.getPath());
				response.setStatus(HttpStatus.CREATED.value());
			} else {
				throw new HttpClientErrorException(HttpStatus.UNSUPPORTED_MEDIA_TYPE, request.getContentType() + " is not supported");
			}
		} catch (HttpStatusCodeException e) {
			response.sendError(e.getStatusCode().value(), e.getMessage());
		}
	}
}
