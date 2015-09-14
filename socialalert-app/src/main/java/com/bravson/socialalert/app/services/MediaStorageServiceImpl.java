package com.bravson.socialalert.app.services;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;

import javax.annotation.Resource;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import com.bravson.socialalert.app.exceptions.DataMissingException;
import com.bravson.socialalert.app.exceptions.SystemExeption;
import com.bravson.socialalert.common.domain.MediaConstants;

@Service
public class MediaStorageServiceImpl implements MediaStorageService, MediaConstants {

	@Value("${media.max.size}")
	private int maxSize;
	
	@Value("${media.temp.dir}")
	private String tempDir;
	
	@Value("${media.base.dir}")
	private String baseDir;
	
	@Value("${picture.thumbnail.prefix}")
	private String thumbnailPrefix;
	
	@Value("${picture.preview.prefix}")
	private String previewPrefix;
	
	@Resource
	private PictureFileService pictureFileService;
	
	@Resource
	private VideoFileService videoFileService;
	
	// TODO authorization should be done at the servlet level
	//@PreAuthorize("hasRole('USER')")
	@Override
	public URI storePicture(InputStream inputStream, int contentLength) throws IOException {
		
		File outputFile = storeMedia(inputStream, contentLength, JPG_EXTENSION);
		
		try {
			pictureFileService.parseJpegMetadata(outputFile);
		} catch (Exception e) {
			throw new HttpClientErrorException(HttpStatus.UNSUPPORTED_MEDIA_TYPE, e.getMessage());
		}
		
		return URI.create(outputFile.getName());
	}
	
	// TODO authorization should be done at the servlet level
	//@PreAuthorize("hasRole('USER')")
	@Override
	public URI storeVideo(InputStream inputStream, int contentLength, String format) throws IOException {
		
		File outputFile = storeMedia(inputStream, contentLength, format);
		
		try {
			videoFileService.parseMetadata(outputFile);
		} catch (Exception e) {
			throw new HttpClientErrorException(HttpStatus.UNSUPPORTED_MEDIA_TYPE, e.getMessage());
		}
		
		return URI.create(outputFile.getName());
	}

	private File storeMedia(InputStream inputStream, int contentLength, String extension) throws IOException, FileNotFoundException {
		if (contentLength < 0) {
			throw new HttpClientErrorException(HttpStatus.LENGTH_REQUIRED, "Content-Length must be specified");
		} else if (contentLength > maxSize) {
			throw new HttpClientErrorException(HttpStatus.PAYLOAD_TOO_LARGE, "Maximum upload size exceeded");
		}
		
		File tempFile = createTemporaryFile(inputStream, FilenameUtils.EXTENSION_SEPARATOR_STR + extension);
		String hash = computeHash(tempFile);
		
		File outputFile = new File(tempDir, hash + FilenameUtils.EXTENSION_SEPARATOR_STR + extension);
		if (outputFile.isFile()) {
			outputFile.delete();
		}
		tempFile.renameTo(outputFile);
		return outputFile;
	}
	
	@Override
	public URI storeRemotePicture(URL sourceUrl) throws IOException {
		URLConnection connection = sourceUrl.openConnection();
		connection.connect();
		try (InputStream is = connection.getInputStream()) {
			return storePicture(is, connection.getContentLength());
		}
	}

	private String computeHash(File tempFile) throws IOException, FileNotFoundException {
		try (FileInputStream fileInputStream = new FileInputStream(tempFile)) {
			return DigestUtils.md5Hex(fileInputStream);
		}
	}

	private File createTemporaryFile(InputStream inputStream, String extension) throws IOException {
		File tempFile = File.createTempFile("upload", extension);
		FileUtils.copyInputStreamToFile(inputStream, tempFile);
		return tempFile;
	}
	
	@Override
	public File resolveMediaUri(URI uri) {
		return resolveMediaPath(uri.getPath());
	}
	
	private File resolveMediaPath(String path) {
		File finalFile = new File(baseDir, path);
		if (finalFile.canRead()) {
			return finalFile;
		}
		File tempFile = new File(tempDir, path);
		if (tempFile.canRead()) {
			return tempFile;
		}
		File transitoryFile = new File(tempDir, tempFile.getName());
		if (transitoryFile.canRead()) {
			return transitoryFile;
		}
		return null;
	}
	
	
	@Override
	public File resolveThumbnailUri(URI uri) throws IOException {
		File mediaFile = resolveMediaPath(uri.getPath());
		if (mediaFile == null) {
			return null;
		}
		File thumbnail = new File(mediaFile.getParentFile(), thumbnailPrefix + changeExtension(mediaFile.getName(), JPG_EXTENSION));
		if (thumbnail.canRead()) {
			return thumbnail;
		}
		if (mediaFile.getName().endsWith(JPG_EXTENSION)) {
			return pictureFileService.createJpegThumbnail(mediaFile);
		} else {
			return videoFileService.createThumbnail(mediaFile);
		}
	}
	
	@Override
	public File resolvePreviewUri(URI uri) throws IOException {
		File mediaFile = resolveMediaPath(uri.getPath());
		if (mediaFile == null) {
			return null;
		}
		
		boolean picture = mediaFile.getName().endsWith(JPG_EXTENSION);
		File preview = new File(mediaFile.getParentFile(), previewPrefix + changeExtension(mediaFile.getName(), picture ? JPG_EXTENSION : MP4_EXTENSION));
		if (preview.canRead()) {
			return preview;
		}
		
		if (picture) {
			return pictureFileService.createJpegPreview(mediaFile);
		} else {
			return videoFileService.createPreview(mediaFile);
		}
	}
	
	public URI buildFinalMediaUri(URI tempUri, DateTime claimDate) {
		return URI.create(claimDate.toString("yyyyMMdd") + "/" + tempUri.getPath());
	}
	
	private String changeExtension(String filename, String newExtension) {
		return FilenameUtils.removeExtension(filename) + FilenameUtils.EXTENSION_SEPARATOR_STR + newExtension;
	}
	
	public URI archiveMedia(URI tempUri, URI finalUri) {
		File tempFile = new File(tempDir, tempUri.getPath());
		if (!tempFile.isFile()) {
			throw new DataMissingException("The media " + tempUri + " does not exists");
		}
		File thumbFile = new File(tempDir, thumbnailPrefix + changeExtension(tempUri.getPath(), JPG_EXTENSION));
		boolean picture = tempFile.getName().endsWith(JPG_EXTENSION);
		File previewFile = new File(tempDir, previewPrefix + changeExtension(tempUri.getPath(), picture ? JPG_EXTENSION : MP4_EXTENSION));
	
		File destFile = new File(baseDir, finalUri.getPath());
		File destDir = destFile.getParentFile();
		try {
			FileUtils.forceMkdir(destDir);
			if (destFile.exists()) {
				deleteFile(destFile);
			}
			FileUtils.moveFile(tempFile, destFile);
			if (thumbFile.isFile()) {
				FileUtils.moveToDirectory(thumbFile, destDir, false);
			}
			if (previewFile.isFile()) {
				FileUtils.moveToDirectory(previewFile, destDir, false);
			}
		} catch (IOException e) {
			throw new SystemExeption("Cannot move temp file " + tempUri + " to " + destDir, e);
		}
		return finalUri;
	}

	@Override
	public void deleteMedia(URI uri) {
		File mediaFile = resolveMediaUri(uri);
		if (mediaFile == null) {
			throw new DataMissingException("The media " + uri + " does not exists");
		}
		
		deleteFile(mediaFile);
	}

	private void deleteFile(File mediaFile) {
		if (!FileUtils.deleteQuietly(mediaFile)) {
			throw new SystemExeption("Cannot delete media file " + mediaFile);
		}
		
		File thumbFile = new File(mediaFile.getParentFile(), thumbnailPrefix + changeExtension(mediaFile.getName(), JPG_EXTENSION));
		if (thumbFile != null && thumbFile.canWrite() && !FileUtils.deleteQuietly(thumbFile)) {
			throw new SystemExeption("Cannot delete thumbnail file " + thumbFile);
		}
		
		boolean picture = mediaFile.getName().endsWith(JPG_EXTENSION);
		File previewFile = new File(mediaFile.getParentFile(), previewPrefix + changeExtension(mediaFile.getName(), picture ? JPG_EXTENSION : MP4_EXTENSION));
		if (previewFile != null && previewFile.canWrite() && !FileUtils.deleteQuietly(previewFile)) {
			throw new SystemExeption("Cannot delete preview file " + previewFile);
		}
	}
}
