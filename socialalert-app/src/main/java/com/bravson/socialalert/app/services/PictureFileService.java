package com.bravson.socialalert.app.services;

import java.io.File;
import java.io.IOException;

import javax.validation.constraints.NotNull;

import org.springframework.validation.annotation.Validated;

import com.bravson.socialalert.app.domain.PictureMetadata;
import com.drew.imaging.jpeg.JpegProcessingException;

@Validated
public interface PictureFileService {

	public File createJpegThumbnail(@NotNull File sourceFile) throws IOException;
	
	public File createJpegPreview(@NotNull File sourceFile) throws IOException;

	public PictureMetadata parseJpegMetadata(@NotNull File sourceFile) throws JpegProcessingException, IOException;

}
