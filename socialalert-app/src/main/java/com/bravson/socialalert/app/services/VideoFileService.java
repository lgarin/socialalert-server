package com.bravson.socialalert.app.services;

import java.io.File;
import java.io.IOException;

import javax.validation.constraints.NotNull;

import org.springframework.validation.annotation.Validated;

import com.bravson.socialalert.app.domain.VideoMetadata;

@Validated
public interface VideoFileService {

	public File createThumbnail(@NotNull File sourceFile) throws IOException;
	
	public File createPreview(@NotNull File sourceFile) throws IOException;
	
	public VideoMetadata parseMetadata(@NotNull File sourceFile) throws IOException;

	public File watermark(File sourceFile) throws IOException;
}
