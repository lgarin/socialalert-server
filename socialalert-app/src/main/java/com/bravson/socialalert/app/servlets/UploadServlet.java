package com.bravson.socialalert.app.servlets;

import java.io.IOException;
import java.net.URI;
import java.util.Collections;

import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.HttpRequestHandler;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpStatusCodeException;

import com.bravson.socialalert.app.domain.PictureMetadata;
import com.bravson.socialalert.app.domain.VideoMetadata;
import com.bravson.socialalert.app.services.MediaStorageService;
import com.bravson.socialalert.common.domain.MediaConstants;

@Component
public class UploadServlet implements HttpRequestHandler, MediaConstants {
	
	@Resource
	private MediaStorageService storageService;
	
	@Override
	public void handleRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if (!request.getMethod().equalsIgnoreCase(RequestMethod.POST.name())) {
			throw new HttpRequestMethodNotSupportedException(request.getMethod(), Collections.singleton(RequestMethod.POST.name()));
		}
		
		try {
			if (JPG_MEDIA_TYPE.equals(request.getContentType())) {
				Pair<URI, PictureMetadata> picture = storageService.storePicture(request.getInputStream(), request.getContentLength());
				request.getInputStream().close();
				writeResponse(response, picture.getKey(), picture.getValue().getLatitude(), picture.getValue().getLatitude(), picture.getValue().getCameraMaker(), picture.getValue().getCameraModel());
				response.setStatus(HttpStatus.CREATED.value());
			} else if (MP4_MEDIA_TYPE.equals(request.getContentType())) {
				Pair<URI, VideoMetadata> video = storageService.storeVideo(request.getInputStream(), request.getContentLength(), MP4_EXTENSION);
				request.getInputStream().close();
				writeResponse(response, video.getKey(), video.getValue().getLatitude(), video.getValue().getLatitude(), video.getValue().getCameraMaker(), video.getValue().getCameraModel());
				response.setStatus(HttpStatus.CREATED.value());
			} else if (MOV_MEDIA_TYPE.equals(request.getContentType())) {
				Pair<URI, VideoMetadata> video = storageService.storeVideo(request.getInputStream(), request.getContentLength(), MOV_EXTENSION);
				request.getInputStream().close();
				writeResponse(response, video.getKey(), video.getValue().getLatitude(), video.getValue().getLatitude(), video.getValue().getCameraMaker(), video.getValue().getCameraModel());
				response.setStatus(HttpStatus.CREATED.value());
			} else {
				throw new HttpClientErrorException(HttpStatus.UNSUPPORTED_MEDIA_TYPE, request.getContentType() + " is not supported");
			}
		} catch (HttpStatusCodeException e) {
			response.sendError(e.getStatusCode().value(), e.getMessage());
		}
	}
	
	private void writeResponse(HttpServletResponse response, URI uri, Double latitude, Double longitude, String cameraMaker, String cameraModel) {
		// TODO absolute path?
		response.setHeader("Location", uri.getPath());
		if (latitude != null && longitude != null) {
			response.setHeader("Latitude", latitude.toString());
			response.setHeader("Longitude", longitude.toString());
		}
		if (cameraMaker != null) {
			response.setHeader("CameraMaker", cameraMaker);
		}
		if (cameraModel != null) {
			response.setHeader("CameraModel", cameraModel);
		}
	}
}
