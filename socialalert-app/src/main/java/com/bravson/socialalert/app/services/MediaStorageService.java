package com.bravson.socialalert.app.services;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;

import javax.validation.constraints.NotNull;

import org.joda.time.DateTime;
import org.springframework.validation.annotation.Validated;

@Validated
public interface MediaStorageService {

	public URI storePicture(@NotNull InputStream inputStream, int contentLength) throws IOException;
	
	public URI storeRemotePicture(@NotNull URL sourceUrl) throws IOException;
	
	public File resolveMediaUri(@NotNull URI uri);
	
	public File resolveThumbnailUri(@NotNull URI uri) throws IOException;
	
	public File resolvePreviewUri(@NotNull URI uri) throws IOException;
	
	public URI buildFinalMediaUri(@NotNull URI tempUri, @NotNull DateTime claimDate);
	
	public URI archiveMedia(@NotNull URI tempUri, @NotNull URI finalUri);

	public void deleteMedia(@NotNull URI uri);

}
