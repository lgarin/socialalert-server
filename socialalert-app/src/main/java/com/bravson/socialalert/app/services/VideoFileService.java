package com.bravson.socialalert.app.services;

import java.io.File;
import java.io.IOException;

import javax.validation.constraints.NotNull;

import org.springframework.validation.annotation.Validated;

import com.bravson.socialalert.app.domain.VideoMetadata;
import com.drew.imaging.jpeg.JpegProcessingException;

@Validated
public interface VideoFileService {

	public File createJpegThumbnail(@NotNull File sourceFile) throws IOException;
	
	public File createJpegPreview(@NotNull File sourceFile) throws IOException;
	
	public File createJpegImage(@NotNull File sourceFile) throws IOException;

	public VideoMetadata parseMp4Metadata(@NotNull File sourceFile) throws IOException;
}
