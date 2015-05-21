package com.bravson.socialalert.services;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import javax.annotation.Resource;
import javax.validation.ValidationException;

import org.apache.commons.io.FileUtils;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

import com.bravson.socialalert.app.exceptions.DataMissingException;
import com.bravson.socialalert.app.services.MediaStorageService;
import com.bravson.socialalert.infrastructure.SimpleServiceTest;

public class MediaStorageServiceTest extends SimpleServiceTest {

	@Resource
	private MediaStorageService service;
	
	@Value("${media.temp.dir}")
	private File tempDir;
	
	@Value("${media.base.dir}")
	private File baseDir;
	
	@Value("${picture.thumbnail.prefix}")
	private String thumbnailPrefix;
	
	@Value("${picture.preview.prefix}")
	private String previewPrefix;
	
	@Before
	public void init() throws IOException {
		FileUtils.cleanDirectory(tempDir);
		FileUtils.cleanDirectory(baseDir);
		authenticate("test", "USER");
	}
	
	@Test
	public void storeValidPicture() throws IOException {
		File file = new File("src/test/resources/media/IMG_0397.JPG");
		URI uri = service.storePicture(FileUtils.openInputStream(file), (int) file.length());
		assertNotNull(uri);
		assertEquals("7e9a5a5bd5e64171c176ac6c7b32d685.jpg", uri.getPath());
		File outputFile = new File(tempDir, uri.getPath());
		assertTrue(outputFile.exists());
		assertTrue(FileUtils.contentEquals(file, outputFile));
		File thumbnailFile = new File(tempDir, thumbnailPrefix + uri.getPath());
		assertFalse(thumbnailFile.exists());
		File previewFile = new File(tempDir, previewPrefix + uri.getPath());
		assertFalse(previewFile.exists());
	}
	
	@Test
	public void storeInvalidPicture() throws IOException {
		File file = new File("src/test/resources/media/invalid.JPG");
		try {
			service.storePicture(FileUtils.openInputStream(file), (int) file.length());
			fail();
		} catch (HttpClientErrorException e) {
			Assert.assertEquals(HttpStatus.UNSUPPORTED_MEDIA_TYPE, e.getStatusCode());
		}
	}
	
	@Test
	public void storeWithUnspecifiedLength() throws IOException {
		try {
			service.storePicture(new ByteArrayInputStream(new byte[0]), -1);
			fail();
		} catch (HttpClientErrorException e) {
			Assert.assertEquals(HttpStatus.LENGTH_REQUIRED, e.getStatusCode());
		}
	}
	
	@Test
	public void storeVeryLargeFile() throws IOException {
		try {
			service.storePicture(new ByteArrayInputStream(new byte[0]), 1000 * 1000 * 100);
			fail();
		} catch (HttpClientErrorException e) {
			Assert.assertEquals(HttpStatus.PAYLOAD_TOO_LARGE, e.getStatusCode());
		}
	}
	
	@Test
	public void storePictureWithIncorrectContentLength() throws IOException {
		// TODO check if this could happen in reality
		File file = new File("src/test/resources/media/IMG_0397.JPG");
		URI uri = service.storePicture(FileUtils.openInputStream(file), 100);
		assertNotNull(uri);
	}

	@Test(expected=ValidationException.class)
	public void storeNullStream() throws IOException {
		service.storePicture(null, 0);
	}
	
	@Test
	public void storeValidRemotePicture() throws MalformedURLException, IOException {
		URI uri = service.storeRemotePicture(new URL("http://www.w3.org/MarkUp/Test/xhtml-print/20050519/tests/jpeg420exif.jpg"));
		assertNotNull(uri);
	}
	
	@Test
	public void storeInvalidRemotePicture() throws MalformedURLException, IOException {
		try {
			service.storeRemotePicture(new URL("http://www.eclipse.org/eclipse.org-common/themes/Nova/images/eclipse.png"));
			fail();
		} catch (HttpClientErrorException e) {
			Assert.assertEquals(HttpStatus.UNSUPPORTED_MEDIA_TYPE, e.getStatusCode());
		}
	}
	
	@Test(expected=IOException.class)
	public void storeMissingRemotePicture() throws MalformedURLException, IOException {
		service.storeRemotePicture(new URL("http://www.eclipse.org/test999.png"));
	}
	
	@Test
	public void resolveTempMedia() throws IOException {
		URI tempUri = URI.create("7e9a5a5bd5e64171c176ac6c7b32d685.jpg");
		File tempFile = new File(tempDir, tempUri.getPath());
		tempFile.createNewFile();
		File resolvedFile = service.resolveMediaUri(tempUri);
		assertEquals(tempFile, resolvedFile);
	}
	
	@Test
	public void resolveFinalMedia() throws IOException {
		URI finalUri = URI.create("20130701/7e9a5a5bd5e64171c176ac6c7b32d685.jpg");
		File destDir = new File(baseDir, "20130701");
		destDir.mkdir();
		File finalFile = new File(baseDir, finalUri.getPath());
		finalFile.createNewFile();
		File resolvedFile = service.resolveMediaUri(finalUri);
		assertEquals(finalFile, resolvedFile);
	}
	
	@Test
	public void resolveNonExistingMedia() {
		URI invalidUri = URI.create("7e9a5a5bd5e64171c176ac6c7b32d685.jpg");
		File resolvedFile = service.resolveMediaUri(invalidUri);
		assertNull(resolvedFile);
	}
	
	@Test
	public void resolveTempThumbnail() throws IOException {
		URI tempUri = URI.create("7e9a5a5bd5e64171c176ac6c7b32d685.jpg");
		File tempFile = new File(tempDir, tempUri.getPath());
		tempFile.createNewFile();
		File thumbFile = new File(tempDir, thumbnailPrefix + tempUri.getPath());
		thumbFile.createNewFile();
		File resolvedFile = service.resolveThumbnailUri(tempUri);
		assertEquals(thumbFile, resolvedFile);
	}
	
	@Test
	public void resolveMissingTempThumbnail() throws IOException {
		URI tempUri = URI.create("7e9a5a5bd5e64171c176ac6c7b32d685.jpg");
		File sourceFile = new File("src/test/resources/media/IMG_0397.JPG");
		File tempFile = new File(tempDir, tempUri.getPath());
		FileUtils.copyFile(sourceFile, tempFile);
		File thumbFile = new File(tempDir, thumbnailPrefix + tempFile.getName());
		File resolvedFile = service.resolveThumbnailUri(tempUri);
		assertEquals(thumbFile, resolvedFile);
		assertTrue(thumbFile.exists());
	}
	
	@Test
	public void resolveFinalThumbnail() throws IOException {
		URI finalUri = URI.create("20130701/7e9a5a5bd5e64171c176ac6c7b32d685.jpg");
		File destDir = new File(baseDir, "20130701");
		destDir.mkdir();
		File finalFile = new File(destDir, "7e9a5a5bd5e64171c176ac6c7b32d685.jpg");
		finalFile.createNewFile();
		File thumbFile = new File(destDir, thumbnailPrefix + finalFile.getName());
		thumbFile.createNewFile();
		File resolvedFile = service.resolveThumbnailUri(finalUri);
		assertEquals(thumbFile, resolvedFile);
	}
	
	@Test
	public void resolveMissingFinalThumbnail() throws IOException {
		URI finalUri = URI.create("20130701/7e9a5a5bd5e64171c176ac6c7b32d685.jpg");
		File destDir = new File(baseDir, "20130701");
		destDir.mkdir();
		File sourceFile = new File("src/test/resources/media/IMG_0397.JPG");
		File finalFile = new File(destDir, "7e9a5a5bd5e64171c176ac6c7b32d685.jpg");
		FileUtils.copyFile(sourceFile, finalFile);
		File thumbFile = new File(destDir, thumbnailPrefix + finalFile.getName());
		File resolvedFile = service.resolveThumbnailUri(finalUri);
		assertEquals(thumbFile, resolvedFile);
		assertTrue(thumbFile.exists());
	}
	
	@Test
	public void resolveNonExistingThumbnail() throws IOException {
		URI invalidUri = URI.create("7e9a5a5bd5e64171c176ac6c7b32d685.jpg");
		File resolvedFile = service.resolveThumbnailUri(invalidUri);
		assertNull(resolvedFile);
	}
	
	@Test
	public void resolveTempPreview() throws IOException {
		URI tempUri = URI.create("7e9a5a5bd5e64171c176ac6c7b32d685.jpg");
		File tempFile = new File(tempDir, tempUri.getPath());
		tempFile.createNewFile();
		File previewFile = new File(tempDir, previewPrefix + tempUri.getPath());
		previewFile.createNewFile();
		File resolvedFile = service.resolvePreviewUri(tempUri);
		assertEquals(previewFile, resolvedFile);
	}
	
	@Test
	public void resolveMissingTempPreview() throws IOException {
		URI tempUri = URI.create("7e9a5a5bd5e64171c176ac6c7b32d685.jpg");
		File sourceFile = new File("src/test/resources/media/IMG_0397.JPG");
		File tempFile = new File(tempDir, tempUri.getPath());
		FileUtils.copyFile(sourceFile, tempFile);
		File previewFile = new File(baseDir, previewPrefix + tempFile.getName());
		File resolvedFile = service.resolvePreviewUri(tempUri);
		assertEquals(previewFile, resolvedFile);
		assertTrue(previewFile.exists());
	}
	
	@Test
	public void resolveFinalPreview() throws IOException {
		URI finalUri = URI.create("20130701/7e9a5a5bd5e64171c176ac6c7b32d685.jpg");
		File destDir = new File(baseDir, "20130701");
		destDir.mkdir();
		File finalFile = new File(destDir, "7e9a5a5bd5e64171c176ac6c7b32d685.jpg");
		finalFile.createNewFile();
		File previewFile = new File(destDir, previewPrefix + finalFile.getName());
		previewFile.createNewFile();
		File resolvedFile = service.resolvePreviewUri(finalUri);
		assertEquals(previewFile, resolvedFile);
	}
	
	@Test
	public void resolveMissingFinalPreview() throws IOException {
		URI finalUri = URI.create("20130701/7e9a5a5bd5e64171c176ac6c7b32d685.jpg");
		File destDir = new File(baseDir, "20130701");
		destDir.mkdir();
		File sourceFile = new File("src/test/resources/media/IMG_0397.JPG");
		File finalFile = new File(destDir, "7e9a5a5bd5e64171c176ac6c7b32d685.jpg");
		FileUtils.copyFile(sourceFile, finalFile);
		File previewFile = new File(destDir, previewPrefix + finalFile.getName());
		File resolvedFile = service.resolvePreviewUri(finalUri);
		assertEquals(previewFile, resolvedFile);
		assertTrue(previewFile.exists());
	}
	
	@Test
	public void resolveNonExistingPreview() throws IOException {
		URI invalidUri = URI.create("7e9a5a5bd5e64171c176ac6c7b32d685.jpg");
		File resolvedFile = service.resolvePreviewUri(invalidUri);
		assertNull(resolvedFile);
	}
	
	@Test
	public void buildFinalUri() {
		URI tempUri = URI.create("7e9a5a5bd5e64171c176ac6c7b32d685.jpg");
		DateTime claimDate = new DateTime(2013, 7, 1, 0, 0, 0);
		URI finalUri = service.buildFinalMediaUri(tempUri, claimDate);
		assertEquals(URI.create("20130701/7e9a5a5bd5e64171c176ac6c7b32d685.jpg"), finalUri);
	}
	
	@Test(expected=DataMissingException.class)
	public void claimInvalidUri() {
		URI tempUri = URI.create("7e9a5a5bd5e64171c176ac6c7b32d685.jpg");
		DateTime claimDate = new DateTime(2013, 7, 1, 0, 0, 0);
		URI finalUri = service.buildFinalMediaUri(tempUri, claimDate);
		service.archiveMedia(tempUri, finalUri);
	}
	
	@Test
	public void claimValidUri() throws IOException {
		URI tempUri = URI.create("7e9a5a5bd5e64171c176ac6c7b32d685.jpg");
		File tempFile = new File(tempDir, tempUri.getPath());
		tempFile.createNewFile();
		File thumbnail = new File(tempDir, thumbnailPrefix + tempUri.getPath());
		thumbnail.createNewFile();
		File preview = new File(tempDir, previewPrefix + tempUri.getPath());
		preview.createNewFile();
		DateTime claimDate = new DateTime(2013, 7, 1, 0, 0, 0);
		URI finalUri = service.buildFinalMediaUri(tempUri, claimDate);
		URI finalUri2 = service.archiveMedia(tempUri, finalUri);
		assertEquals(finalUri, finalUri2);
		File finalFile = new File(baseDir, finalUri.getPath());
		assertTrue(finalFile.isFile());
	}
	
	@Test(expected=DataMissingException.class)
	public void claimValidUriTwice() throws IOException {
		URI tempUri = URI.create("7e9a5a5bd5e64171c176ac6c7b32d685.jpg");
		File tempFile = new File(tempDir, tempUri.getPath());
		tempFile.createNewFile();
		File thumbnail = new File(tempDir, thumbnailPrefix + tempUri.getPath());
		thumbnail.createNewFile();
		DateTime claimDate = new DateTime(2013, 7, 1, 0, 0, 0);
		URI finalUri = service.buildFinalMediaUri(tempUri, claimDate);
		URI finalUri2 = service.archiveMedia(tempUri, finalUri);
		assertEquals(finalUri, finalUri2);
		File finalFile = new File(baseDir, finalUri.getPath());
		assertTrue(finalFile.exists() && finalFile.isFile());
		service.archiveMedia(tempUri, finalUri);
	}
	
	@Test
	public void overwriteArchivedMedia() throws IOException {
		URI tempUri = URI.create("7e9a5a5bd5e64171c176ac6c7b32d685.jpg");
		File tempFile = new File(tempDir, tempUri.getPath());
		tempFile.createNewFile();
		DateTime claimDate = new DateTime(2013, 7, 1, 0, 0, 0);
		URI finalUri = service.buildFinalMediaUri(tempUri, claimDate);
		URI finalUri2 = service.archiveMedia(tempUri, finalUri);
		assertEquals(finalUri, finalUri2);
		assertFalse(tempFile.exists());
		
		File tempFile2 = new File(tempDir, tempUri.getPath());
		tempFile2.createNewFile();
		File thumbnail = new File(tempDir, thumbnailPrefix + tempUri.getPath());
		thumbnail.createNewFile();
		URI finalUri3 = service.archiveMedia(tempUri, finalUri);
		assertEquals(finalUri, finalUri3);
		assertFalse(tempFile2.exists());
	}
	
	@Test
	public void claimNewFile() throws IOException {
		File file = new File("src/test/resources/media/IMG_0397.JPG");
		URI tempUri = service.storePicture(FileUtils.openInputStream(file), (int) file.length());
		DateTime claimDate = new DateTime(2013, 7, 1, 0, 0, 0);
		URI finalUri = service.buildFinalMediaUri(tempUri, claimDate);
		URI finalUri2 = service.archiveMedia(tempUri, finalUri);
		assertEquals(finalUri, finalUri2);
		File finalFile = service.resolveMediaUri(finalUri);
		assertNotNull(finalFile);
		assertTrue(FileUtils.contentEquals(file, finalFile));
	}
	
	@Test(expected=DataMissingException.class)
	public void deleteNonExistingFile() {
		URI uri = URI.create("7e9a5a5bd5e64171c176ac6c7b32d685.jpg");
		service.deleteMedia(uri);
	}
	
	@Test
	public void deleteNewFile() throws IOException {
		File file = new File("src/test/resources/media/IMG_0397.JPG");
		URI tempUri = service.storePicture(FileUtils.openInputStream(file), (int) file.length());
		service.deleteMedia(tempUri);
	}
	
	@Test
	public void storeValidVideo() throws IOException {
		File file = new File("src/test/resources/media/msmpeg4.avi");
		URI uri = service.storeVideo(FileUtils.openInputStream(file), (int) file.length(), "avi");
		assertNotNull(uri);
		assertEquals("431d9a264140516a042d7019726ab3c8.avi", uri.getPath());
		File outputFile = new File(tempDir, uri.getPath());
		assertTrue(outputFile.exists());
		assertTrue(FileUtils.contentEquals(file, outputFile));
	}
	
	@Test
	public void archiveVideoFile() throws IOException {
		File file = new File("src/test/resources/media/msmpeg4.avi");
		URI tempUri = service.storeVideo(FileUtils.openInputStream(file), (int) file.length(), "avi");
		DateTime claimDate = new DateTime(2015, 1, 7, 0, 0, 0);
		URI finalUri = service.buildFinalMediaUri(tempUri, claimDate);
		URI finalUri2 = service.archiveMedia(tempUri, finalUri);
		assertEquals(finalUri, finalUri2);
		File finalFile = service.resolveMediaUri(finalUri);
		assertNotNull(finalFile);
		assertTrue(FileUtils.contentEquals(file, finalFile));
	}
	
	@Test
	@Ignore
	public void resolveArchivedVideoThumbnail() throws IOException {
		File file = new File("src/test/resources/media/msmpeg4.avi");
		URI tempUri = service.storeVideo(FileUtils.openInputStream(file), (int) file.length(), "avi");
		DateTime claimDate = new DateTime(2015, 1, 7, 0, 0, 0);
		URI finalUri = service.buildFinalMediaUri(tempUri, claimDate);
		URI finalUri2 = service.archiveMedia(tempUri, finalUri);
		assertEquals(finalUri, finalUri2);
		File resolvedFile = service.resolveThumbnailUri(finalUri2);
		assertNotNull(resolvedFile);
		assertTrue(resolvedFile.getName().endsWith(".jpg"));
		assertTrue(resolvedFile.getName().startsWith("thumb-"));
		assertTrue(FileUtils.contentEquals(new File("src/test/resources/media/msmpeg4.jpg"), resolvedFile));
	}
}
